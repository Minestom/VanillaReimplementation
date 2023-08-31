package net.minestom.vanilla.datapack.worldgen;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.worldgen.math.CubicSpline;
import net.minestom.vanilla.datapack.worldgen.noise.BlendedNoise;
import net.minestom.vanilla.datapack.worldgen.noise.Noise;
import net.minestom.vanilla.datapack.worldgen.noise.NormalNoise;
import net.minestom.vanilla.datapack.worldgen.noise.SimplexNoise;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;

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

    record OldBlendedNoise(BlendedNoise noise) implements DensityFunction {

        // TODO: Do we need this?
        public OldBlendedNoise(double xz_scale,
                               double y_scale,
                               double xz_factor,
                               double y_factor,
                               double smear_scale_multiplier) {
            this(new BlendedNoise(DatapackLoader.loading().random(), xz_scale, y_scale, xz_factor, y_factor, smear_scale_multiplier));
        }

        @Override
        public double compute(Context context) {
            return noise.sample(context.x(), context.y(), context.z());
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

        private final DensityFunction wrapped;

        private int lastQuartX = 0;
        private int lastQuartZ = 0;
        private double lastValue = 0;

        public FlatCache(DensityFunction argument) {
            this.wrapped = argument;
        }

        public double compute(Context context) {
            int quartX = context.blockX() >> 2;
            int quartZ = context.blockZ() >> 2;
            if (this.lastQuartX != quartX || this.lastQuartZ != quartZ) {
                this.lastValue = this.wrapped.compute(DensityFunction.context(quartX << 2, 0, quartZ << 2));
                this.lastQuartX = quartX;
                this.lastQuartZ = quartZ;
            }
            return this.lastValue;
        }

        @Override
        public DensityFunction wrapped() {
            return wrapped;
        }
    }

    // TODO: Add double storage optimization back in
    record Interpolated(DensityFunction argument/*, DoubleStorage storage */) implements Wrapped {

//        public Interpolated(DensityFunction wrapped) {
//            this(wrapped, DoubleStorage.exactCache(DoubleStorage.from(wrapped)));
//        }


        @Override
        public DensityFunction wrapped() {
            return argument();
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
//            return storage.obtain(x, y, z);
            return wrapped().compute(DensityFunction.context(x, y, z));
        }

    }

    class Cache2D implements Wrapped {
        // Only computes the input density once per horizonal position.
        // TODO: Use a deep cache instead of a shallow (only caches the last value) cache.

        private final DensityFunction wrapped;

        private int lastBlockX = 0;
        private int lastBlockZ = 0;
        private double lastValue = 0;

        public Cache2D(DensityFunction wrapped) {
            this.wrapped = wrapped;
        }

        public double compute(Context context) {
            int blockX = context.blockX();
            int blockZ = context.blockZ();
            if (this.lastBlockX != blockX || this.lastBlockZ != blockZ) {
                this.lastValue = this.wrapped.compute(context);
                this.lastBlockX = blockX;
                this.lastBlockZ = blockZ;
            }
            return this.lastValue;
        }

        @Override
        public DensityFunction wrapped() {
            return wrapped;
        }
    }

    class CacheOnce implements Wrapped {

        private final DensityFunction wrapped;

        private int lastHash = 0;
        private double lastValue = 0;

        public CacheOnce(DensityFunction wrapped) {
            this.wrapped = wrapped;
        }

        public double compute(Context context) {
            int blockX = context.blockX();
            int blockY = context.blockY();
            int blockZ = context.blockZ();
            int hash = Objects.hash(blockX, blockY, blockZ);
            if (this.lastHash != hash) {
                this.lastValue = this.wrapped.compute(context);
                this.lastHash = hash;
            }
            return this.lastValue;
        }

        @Override
        public DensityFunction wrapped() {
            return wrapped;
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
            WorldgenRandom random = WorldgenRandom.standard(seed);
            random.consume(17292);
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

    record ShiftedNoise(double xz_scale, double y_scale, DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, NormalNoise noise) implements DensityFunction {
        public double compute(Context context) {
            return this.noise.sample(
                    context.x() * this.xz_scale() + this.shiftX.compute(context),
                    context.y() * this.y_scale() + this.shiftY.compute(context),
                    context.z() * this.xz_scale() + this.shiftZ.compute(context)
            );
        }

        @Override
        public double maxValue() {
            return noise().maxValue;
        }
    }

    record RangeChoice(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange,
                       DensityFunction whenOutOfRange) implements DensityFunction {

        public double compute(Context context) {
            return this.input.compute(context) >= this.minInclusive && this.input.compute(context) < this.maxExclusive
                    ? this.whenInRange.compute(context)
                    : this.whenOutOfRange.compute(context);
        }

        public double minValue() {
            return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue());
        }

        public double maxValue() {
            return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue());
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

    record BlendDensity(DensityFunction input) implements DensityFunction {
        @Override
        public double compute(Context context) {
            // TODO: Not implemented
            throw new UnsupportedOperationException("Not implemented");
        }

        public double minValue() {
            return -Double.MAX_VALUE;
        }

        public double maxValue() {
            return Double.MAX_VALUE;
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

    record Abs(DensityFunction input) implements DensityFunction {

        public double compute(Context context) {
            double density = this.input.compute(context);
            return Math.abs(density);
        }

        public double minValue() {
            // the min value may be higher than 0 if the input's range doesn't include 0
            if (this.input.minValue() <= 0 && this.input.maxValue() >= 0) {
                return 0;
            }
            return Math.min(Math.abs(this.input.minValue()), Math.abs(this.input.maxValue()));
        }

        public double maxValue() {
            return Math.max(Math.abs(this.input.minValue()), Math.abs(this.input.maxValue()));
        }
    }

    record Square(DensityFunction input) implements DensityFunction {

        public double compute(Context context) {
            double density = this.input.compute(context);
            return Util.square(density);
        }

        public double minValue() {
            return Util.square(this.input.minValue());
        }

        public double maxValue() {
            return Util.square(this.input.maxValue());
        }
    }

    record Cube(DensityFunction input) implements DensityFunction {

        public double compute(Context context) {
            double density = this.input.compute(context);
            return Util.cube(density);
        }

        public double minValue() {
            return Util.cube(this.input.minValue());
        }

        public double maxValue() {
            return Util.cube(this.input.maxValue());
        }
    }

    record HalfNegative(DensityFunction input) implements DensityFunction {

        public double compute(Context context) {
            double density = this.input.compute(context);
            return density > 0 ? density : density * 0.5;
        }

        public double minValue() {
            return this.input.minValue() * 0.5;
        }

        public double maxValue() {
            return this.input.maxValue();
        }
    }

    record QuarterNegative(DensityFunction input) implements DensityFunction {

        public double compute(Context context) {
            double density = this.input.compute(context);
            return density > 0 ? density : density * 0.25;
        }

        public double minValue() {
            return this.input.minValue() * 0.25;
        }

        public double maxValue() {
            return this.input.maxValue();
        }
    }

    record Squeeze(DensityFunction input) implements DensityFunction {

        public double compute(Context context) {
            double density = this.input.compute(context);
            double c = Util.clamp(density, -1, 1);
            return c / 2.0 - c * c * c / 24.0;
        }

        public double minValue() {
            return this.input.minValue() / 2.0 - this.input.maxValue() * this.input.maxValue() * this.input.maxValue() / 24.0;
        }

        public double maxValue() {
            return this.input.maxValue() / 2.0 - this.input.minValue() * this.input.minValue() * this.input.minValue() / 24.0;
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

    record Spline(CubicSpline<Context> spline) implements DensityFunction {
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

    record YClampedGradient(double fromY, double toY, double fromValue, double toValue) implements DensityFunction {

        public double compute(Context context) {
            return Util.clampedMap(context.y(), this.fromY, this.toY, this.fromValue, this.toValue);
        }

        public double minValue() {
            return Math.min(this.fromValue, this.toValue);
        }

        public double maxValue() {
            return Math.max(this.fromValue, this.toValue);
        }
    }
}
