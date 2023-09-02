package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonReader;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.worldgen.math.CubicSpline;
import net.minestom.vanilla.datapack.worldgen.noise.BlendedNoise;
import net.minestom.vanilla.datapack.worldgen.noise.Noise;
import net.minestom.vanilla.datapack.worldgen.noise.SimplexNoise;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;
import net.minestom.vanilla.datapack.worldgen.storage.DoubleStorage;
import net.minestom.vanilla.datapack.worldgen.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.function.DoubleSupplier;

interface DensityFunctions {

    class ContextImpl implements DensityFunction.Context {
        public double x;
        public double y;
        public double z;

        @Override
        public double x() {
            return x;
        }

        @Override
        public double y() {
            return y;
        }

        @Override
        public double z() {
            return z;
        }
    }

    // blend_alpha goes from 0 ("use old terrain") to 1 ("use new terrain")
    record BlendAlpha() implements DensityFunction {

        @Override
        public double compute(Context context) {
            return 1;
        }

        @Override
        public double maxValue() {
            return 1;
        }

        @Override
        public double minValue() {
            return 0;
        }
    }

    // blend_offset and blend_density are the offset and density values to use for the old terrain.
    record BlendOffset() implements DensityFunction {

        @Override
        public double compute(Context context) {
            return 0;
        }

        @Override
        public double maxValue() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public double minValue() {
            return Double.NEGATIVE_INFINITY;
        }
    }

    record Beardifier() implements DensityFunction {

        @Override
        public double compute(Context context) {
            return 0;
        }

        @Override
        public double maxValue() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public double minValue() {
            return Double.NEGATIVE_INFINITY;
        }
    }

    class OldBlendedNoise implements DensityFunction {

        private final BlendedNoise noise;

        private OldBlendedNoise(Params params) {
            this.noise = new BlendedNoise(DatapackLoader.loading().random(), params.xz_scale(), params.y_scale(), params.xz_factor(), params.y_factor(), params.smear_scale_multiplier());
        }

        public record Params(double xz_scale, double y_scale, double xz_factor, double y_factor, double smear_scale_multiplier) {
        }

        public static OldBlendedNoise fromJson(JsonReader reader) throws IOException {
            Params params = DatapackLoader.moshi(Params.class).apply(reader);
            return new OldBlendedNoise(params);
        }

        @Override
        public double compute(Context context) {
            return noise.sample(context.x(), context.y(), context.z());
        }

        @Override
        public double maxValue() {
            return noise.minValue();
        }

        @Override
        public double minValue() {
            return noise.maxValue();
        }
    }

    interface Wrapped extends DensityFunction {
        DensityFunction wrapped();

        @Override
        default double minValue() {
            return wrapped().minValue();
        }

        @Override
        default double maxValue() {
            return wrapped().maxValue();
        }
    }

    class FlatCache implements Wrapped {

        private final DensityFunction argument;

        private int lastQuartX = 0;
        private int lastQuartZ = 0;
        private double lastValue = 0;

        public FlatCache(DensityFunction argument) {
            this.argument = argument;
        }

        public double compute(Context context) {
            int quartX = context.blockX() >> 2;
            int quartZ = context.blockZ() >> 2;
            if (this.lastQuartX != quartX || this.lastQuartZ != quartZ) {
                this.lastValue = this.argument.compute(DensityFunction.context(quartX << 2, 0, quartZ << 2));
                this.lastQuartX = quartX;
                this.lastQuartZ = quartZ;
            }
            return this.lastValue;
        }

        @Override
        public DensityFunction wrapped() {
            return argument;
        }
    }

    class Interpolated implements Wrapped {
        private final DensityFunction argument;

        @Json(ignore = true)
        private @Nullable DoubleStorage cache;

        public Interpolated(DensityFunction argument) {
            this.argument = argument;
        }

        @Override
        public DensityFunction wrapped() {
            return argument();
        }

        private DoubleStorage cache() {
            if (cache == null) {
                cache = DoubleStorage.threadLocal(() -> DoubleStorage.from(argument).cache());
            }
            return cache;
        }

        @Override
        public double compute(Context context) {
            int blockX = context.blockX();
            int blockY = context.blockY();
            int blockZ = context.blockZ();
            int w = 4;
            int h = 4;
            double x = ((blockX % w + w) % w) / (double) w;
            double y = ((blockY % h + h) % h) / (double) h;
            double z = ((blockZ % w + w) % w) / (double) w;
            int firstX = Math.floorDiv(blockX, w) * w;
            int firstY = Math.floorDiv(blockY, h) * h;
            int firstZ = Math.floorDiv(blockZ, w) * w;
            DoubleSupplier noise000 = () -> this.computeCorner(firstX, firstY, firstZ);
            DoubleSupplier noise001 = () -> this.computeCorner(firstX, firstY, firstZ + w);
            DoubleSupplier noise010 = () -> this.computeCorner(firstX, firstY + h, firstZ);
            DoubleSupplier noise011 = () -> this.computeCorner(firstX, firstY + h, firstZ + w);
            DoubleSupplier noise100 = () -> this.computeCorner(firstX + w, firstY, firstZ);
            DoubleSupplier noise101 = () -> this.computeCorner(firstX + w, firstY, firstZ + w);
            DoubleSupplier noise110 = () -> this.computeCorner(firstX + w, firstY + h, firstZ);
            DoubleSupplier noise111 = () -> this.computeCorner(firstX + w, firstY + h, firstZ + w);
            return Util.lazyLerp3(x, y, z, noise000, noise100, noise010, noise110, noise001, noise101, noise011, noise111);
        }

        private double computeCorner(int x, int y, int z) {
            return cache().obtain(x, y, z);
        }

        public DensityFunction argument() {
            return argument;
        }
    }

    class Cache2D implements Wrapped {
        // Only computes the input density once per horizonal position.

        private final DensityFunction argument;

        @Json(ignore = true)
        private DoubleStorage cache;

        public Cache2D(DensityFunction argument) {
            this.argument = argument;
        }

        private DoubleStorage cache() {
            if (cache == null) {
                cache = DoubleStorage.threadLocal(() -> DoubleStorage.from(argument).cache2d());
            }
            return cache;
        }

        public double compute(Context context) {
            int blockX = context.blockX();
            int blockY = context.blockY();
            int blockZ = context.blockZ();
            return cache().obtain(blockX, blockY, blockZ);
        }

        @Override
        public DensityFunction wrapped() {
            return argument;
        }
    }

    class CacheOnce implements Wrapped {

        private final DensityFunction argument;

        private int lastHash = 0;
        private double lastValue = 0;

        public CacheOnce(DensityFunction argument) {
            this.argument = argument;
        }

        public double compute(Context context) {
            int blockX = context.blockX();
            int blockY = context.blockY();
            int blockZ = context.blockZ();
            int hash = Objects.hash(blockX, blockY, blockZ);
            if (this.lastHash != hash) {
                this.lastValue = this.argument.compute(context);
                this.lastHash = hash;
            }
            return this.lastValue;
        }

        @Override
        public DensityFunction wrapped() {
            return argument;
        }
    }

    record CacheAllInCell(DensityFunction wrapped) implements Wrapped {
        // Used by the game onto final_density and should not be referenced in data packs.
        // TODO: I have no clue what this means or what it should do
        public double compute(Context context) {
            // TODO: Implement
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    record NoiseRoot(double xz_scale, double y_scale, Noise noise) implements DensityFunction {

        @Override
        public double compute(Context context) {
            return this.noise.sample(context.x() * this.xz_scale(), context.y() * this.y_scale(), context.z() * this.xz_scale());
        }

        @Override
        public double maxValue() {
            return this.noise.maxValue();
        }

        @Override
        public double minValue() {
            return this.noise.minValue();
        }
    }

    class EndIslands implements DensityFunction {
        private final SimplexNoise islandNoise;

        public EndIslands() {
            this(0);
        }

        public EndIslands(long seed) {
            WorldgenRandom random = WorldgenRandom.legacy(seed);
            random.consumeInt(17292);
            this.islandNoise = new SimplexNoise(random);
        }

        private double getHeightValue(int x, int z) {
            int x0 = Math.floorDiv(x, 2);
            int z0 = Math.floorDiv(z, 2);
            int x1 = x % 2;
            int z1 = z % 2;
            double f = Util.clamp(100 - Math.sqrt(x * x + z * z), -100, 80);

            for (int i = -12; i <= 12; ++i) {
                for (int j = -12; j <= 12; ++j) {
                    int x2 = x0 + i;
                    int z2 = z0 + j;
                    if (x2 * x2 + z2 * z2 <= 4096 || this.islandNoise.sample2D(x2, z2) >= -0.9) {
                        continue;
                    }
                    int f1 = (Math.abs(x2) * 3439 + Math.abs(z2) * 147) % 13 + 9;
                    int x3 = x1 + i * 2;
                    int z3 = z1 + j * 2;
                    double f2 = 100.0 - Math.sqrt(x3 * x3 + z3 * z3) * f1;
                    double f3 = Util.clamp(f2, -100.0, 80.0);
                    f = Math.max(f, f3);
                }
            }

            return f;
        }

        @Override
        public double compute(Context context) {
            return (this.getHeightValue(Math.floorDiv(context.blockX(), 8), Math.floorDiv(context.blockZ(), 8)) - 8) / 128;
        }

        @Override
        public double minValue() {
            return -0.84375;
        }

        @Override
        public double maxValue() {
            return 0.5625;
        }
    }

    record WeirdScaledSampler(DensityFunction input, RarityValueMapper rarity_value_mapper, Noise noise) implements DensityFunction {

        public enum RarityValueMapper {
            type_1(WeirdScaledSampler::rarityValueMapper1, 2),
            type_2(WeirdScaledSampler::rarityValueMapper2, 3);

            private final Double2DoubleFunction mapper;
            private final double maxValue;

            RarityValueMapper(Double2DoubleFunction mapper, double maxValue) {
                this.mapper = mapper;
                this.maxValue = maxValue;
            }

            public Double2DoubleFunction mapper() {
                return mapper;
            }

            public double maxValue() {
                return maxValue;
            }
        }

        @Override
        public double compute(Context context) {
            double rarity = rarity_value_mapper().mapper().apply(input.compute(context));
            return rarity * Math.abs(this.noise.sample(context.x() / rarity, context.y() / rarity, context.z() / rarity));
        }

        @Override
        public double minValue() {
            return 0;
        }

        @Override
        public double maxValue() {
            return rarity_value_mapper().maxValue();
        }

        private static double rarityValueMapper1(double value) {
            if (value < -0.5) {
                return 0.75;
            } else if (value < 0) {
                return 1;
            } else if (value < 0.5) {
                return 1.5;
            } else {
                return 2;
            }
        }

        private static double rarityValueMapper2(double value) {
            if (value < -0.75) {
                return 0.5;
            } else if (value < -0.5) {
                return 0.75;
            } else if (value < 0.5) {
                return 1;
            } else if (value < 0.75) {
                return 2;
            } else {
                return 3;
            }
        }
    }

    record Constant(double value) implements DensityFunction {
        public static final Constant ZERO = new Constant(0);
        public static Constant ONE = new Constant(1);

        @Override
        public double compute(Context context) {
            return value;
        }

        public double minValue() {
            return value;
        }

        public double maxValue() {
            return value;
        }
    }

    record ShiftedNoise(double xz_scale, double y_scale, DensityFunction shift_x, DensityFunction shift_y, DensityFunction shift_z, Noise noise) implements DensityFunction {
        public double compute(Context context) {
            return this.noise.sample(
                    context.x() * this.xz_scale() + this.shift_x.compute(context),
                    context.y() * this.y_scale() + this.shift_y.compute(context),
                    context.z() * this.xz_scale() + this.shift_z.compute(context)
            );
        }

        @Override
        public double maxValue() {
            return noise().maxValue();
        }

        @Override
        public double minValue() {
            return noise().minValue();
        }
    }

    record RangeChoice(DensityFunction input, double min_inclusive, double max_exclusive, DensityFunction when_in_range,
                       DensityFunction when_out_of_range) implements DensityFunction {

        public double compute(Context context) {
            return this.input.compute(context) >= this.min_inclusive && this.input.compute(context) < this.max_exclusive
                    ? this.when_in_range.compute(context)
                    : this.when_out_of_range.compute(context);
        }

        public double minValue() {
            return Math.min(this.when_in_range.minValue(), this.when_out_of_range.minValue());
        }

        public double maxValue() {
            return Math.max(this.when_in_range.maxValue(), this.when_out_of_range.maxValue());
        }
    }

    record ShiftA(Noise argument) implements DensityFunction {
        // Samples a noise at (x/4, 0, z/4), then multiplies it by 4.
        public double compute(Context context) {
            double shiftedX = context.x() * 0.25;
            double shiftedZ = context.z() * 0.25;
            return argument.sample(shiftedX, 0, shiftedZ) * 4.0;
        }

        @Override
        public double minValue() {
            return argument.minValue() * 4.0;
        }

        @Override
        public double maxValue() {
            return argument.maxValue() * 4.0;
        }
    }

    record ShiftB(Noise argument) implements DensityFunction {
        // Samples a noise at (z/4, x/4, 0), then multiplies it by 4.
        public double compute(Context context) {
            double shiftedX = context.x() * 0.25;
            double shiftedZ = context.z() * 0.25;
            return argument.sample(shiftedZ, shiftedX, 0) * 4.0;
        }

        @Override
        public double minValue() {
            return argument.minValue() * 4.0;
        }

        @Override
        public double maxValue() {
            return argument.maxValue() * 4.0;
        }
    }

    record Shift(Noise argument) implements DensityFunction {
        // Samples a noise at (x/4, y/4, z/4), then multiplies it by 4.
        @Override
        public double compute(Context context) {
            double shiftedX = context.x() * 0.25;
            double shiftedY = context.y() * 0.25;
            double shiftedZ = context.z() * 0.25;
            return argument.sample(shiftedX, shiftedY, shiftedZ) * 4.0;
        }

        @Override
        public double maxValue() {
            return argument.maxValue() * 4.0;
        }

        @Override
        public double minValue() {
            return argument.minValue() * 4.0;
        }
    }

    record BlendDensity(DensityFunction argument) implements DensityFunction {
        @Override
        public double compute(Context context) {
            // TODO: Actually implement this.
            return argument.compute(context);
        }

        public double minValue() {
            return argument.minValue();
        }

        public double maxValue() {
            return argument.maxValue();
        }
    }

    record Clamp(double min, double max, DensityFunction input) implements DensityFunction {

        public double compute(Context context) {
            double density = this.input.compute(context);
            return Util.clamp(density, this.min, this.max);
        }

        public double minValue() {
            return this.min;
        }

        public double maxValue() {
            return this.max;
        }
    }

    record Abs(DensityFunction argument) implements DensityFunction {

        public double compute(Context context) {
            double density = this.argument.compute(context);
            return Math.abs(density);
        }

        public double minValue() {
            // the min value may be higher than 0 if the input's range doesn't include 0
            if (this.argument.minValue() <= 0 && this.argument.maxValue() >= 0) {
                return 0;
            }
            return Math.min(Math.abs(this.argument.minValue()), Math.abs(this.argument.maxValue()));
        }

        public double maxValue() {
            return Math.max(Math.abs(this.argument.minValue()), Math.abs(this.argument.maxValue()));
        }
    }

    record Square(DensityFunction argument) implements DensityFunction {

        public double compute(Context context) {
            double density = this.argument.compute(context);
            return Util.square(density);
        }

        public double minValue() {
            return Util.square(this.argument.minValue());
        }

        public double maxValue() {
            return Util.square(this.argument.maxValue());
        }
    }

    record Cube(DensityFunction argument) implements DensityFunction {

        public double compute(Context context) {
            double density = this.argument.compute(context);
            return Util.cube(density);
        }

        public double minValue() {
            return Util.cube(this.argument.minValue());
        }

        public double maxValue() {
            return Util.cube(this.argument.maxValue());
        }
    }

    record HalfNegative(DensityFunction argument) implements DensityFunction {

        public double compute(Context context) {
            double density = this.argument.compute(context);
            return density > 0 ? density : density * 0.5;
        }

        public double minValue() {
            return this.argument.minValue() * 0.5;
        }

        public double maxValue() {
            return this.argument.maxValue() * 0.5;
        }
    }

    record QuarterNegative(DensityFunction argument) implements DensityFunction {

        public double compute(Context context) {
            double density = this.argument.compute(context);
            return density > 0 ? density : density * 0.25;
        }

        public double minValue() {
            return this.argument.minValue() * 0.25;
        }

        public double maxValue() {
            return this.argument.maxValue();
        }
    }

    record Squeeze(DensityFunction argument) implements DensityFunction {

        public double compute(Context context) {
            double density = this.argument.compute(context);
            double c = Util.clamp(density, -1, 1);
            return c / 2.0 - c * c * c / 24.0;
        }

        public double minValue() {
            return this.argument.minValue() / 2.0 - this.argument.maxValue() * this.argument.maxValue() * this.argument.maxValue() / 24.0;
        }

        public double maxValue() {
            return this.argument.maxValue() / 2.0 - this.argument.minValue() * this.argument.minValue() * this.argument.minValue() / 24.0;
        }
    }

    record Add(DensityFunction argument1, DensityFunction argument2) implements DensityFunction {
        @Override
        public double compute(Context context) {
            return this.argument1.compute(context) + this.argument2.compute(context);
        }

        @Override
        public double minValue() {
            return this.argument1.minValue() + this.argument2.minValue();
        }

        @Override
        public double maxValue() {
            return this.argument1.maxValue() + this.argument2.maxValue();
        }
    }

    record Mul(DensityFunction argument1, DensityFunction argument2) implements DensityFunction {
        @Override
        public double compute(Context context) {
            return this.argument1.compute(context) * this.argument2.compute(context);
        }

        @Override
        public double minValue() {
            return this.argument1.minValue() * this.argument2.minValue();
        }

        @Override
        public double maxValue() {
            return this.argument1.maxValue() * this.argument2.maxValue();
        }
    }

    record Min(DensityFunction argument1, DensityFunction argument2) implements DensityFunction {
        @Override
        public double compute(Context context) {
            return Math.min(this.argument1.compute(context), this.argument2.compute(context));
        }

        @Override
        public double minValue() {
            return Math.min(this.argument1.minValue(), this.argument2.minValue());
        }

        @Override
        public double maxValue() {
            return Math.min(this.argument1.maxValue(), this.argument2.maxValue());
        }
    }

    record Max(DensityFunction argument1, DensityFunction argument2) implements DensityFunction {
        @Override
        public double compute(Context context) {
            return Math.max(this.argument1.compute(context), this.argument2.compute(context));
        }

        @Override
        public double minValue() {
            return Math.max(this.argument1.minValue(), this.argument2.minValue());
        }

        @Override
        public double maxValue() {
            return Math.max(this.argument1.maxValue(), this.argument2.maxValue());
        }
    }

    record Spline(CubicSpline spline) implements DensityFunction {
        public double compute(Context context) {
            return this.spline.compute(context);
        }

        public double minValue() {
            return this.spline.min();
        }

        public double maxValue() {
            return this.spline.max();
        }
    }

    record YClampedGradient(double from_y, double to_y, double from_value, double to_value) implements DensityFunction {

        public double compute(Context context) {
            return Util.clampedMap(context.y(), this.from_y, this.to_y, this.from_value, this.to_value);
        }

        public double minValue() {
            return Math.min(this.from_value, this.to_value);
        }

        public double maxValue() {
            return Math.max(this.from_value, this.to_value);
        }
    }
}
