package net.minestom.vanilla.datapack.worldgen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.worldgen.math.CubicSpline;
import net.minestom.vanilla.datapack.worldgen.noise.BlendedNoise;
import net.minestom.vanilla.datapack.worldgen.noise.NormalNoise;
import net.minestom.vanilla.datapack.worldgen.noise.SimplexNoise;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

interface DensityFunctions {
    interface Visitor {
        Function<DensityFunction, DensityFunction> map();
    }

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

    // case "blend_alpha" -> new ConstantMinMax(1, 0, 1);
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

    // case "blend_offset" -> new ConstantMinMax(0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
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

    // case "beardifier" -> new ConstantMinMax(0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
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

    // case "old_blended_noise" -> new OldBlendedNoise(
    //                    root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
    //                    root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
    //                    root.get("xz_factor") == null ? 80 : root.get("xz_factor").getAsDouble(),
    //                    root.get("y_factor") == null ? 160 : root.get("y_factor").getAsDouble(),
    //                    root.get("smear_scale_multiplier") == null ? 8 : root.get("smear_scale_multiplier").getAsDouble()
    //            );
    record OldBlendedNoise(BlendedNoise noise) implements DensityFunction {

        public OldBlendedNoise(double xzScale,
                               double yScale,
                               double xzFactor,
                               double yFactor,
                               double smearScaleMultiplier) {
            this(new BlendedNoise(DatapackLoader.loadingRandom(), xzScale, yScale, xzFactor, yFactor, smearScaleMultiplier));
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

    // case "flat_cache" -> new FlatCache(inputParser.apply(root.get("argument")));
    class FlatCache implements Wrapped {

        private final DensityFunction wrapped;

        private int lastQuartX = 0;
        private int lastQuartZ = 0;
        private double lastValue = 0;

        public FlatCache(DensityFunction wrapped) {
            this.wrapped = wrapped;
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

    // case "interpolated" -> new Interpolated(inputParser.apply(root.get("argument")));
    record Interpolated(DensityFunction wrapped, DoubleStorage storage) implements Wrapped {

        public Interpolated(DensityFunction wrapped) {
            this(wrapped, DoubleStorage.exactCache(DoubleStorage.from(wrapped)));
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
            return storage.obtain(x, y, z);
        }

    }

    // case "cache_2d" -> new Cache2D(inputParser.apply(root.get("argument")));
    class Cache2D implements Wrapped {

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

    // case "cache_once" -> new CacheOnce(inputParser.apply(root.get("argument")));
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

    // case "cache_all_in_cell" -> new CacheAllInCell(inputParser.apply(root.get("argument")));
    record CacheAllInCell(DensityFunction wrapped) implements Wrapped {
        public double compute(Context context) {
            // TODO: Implement
            return this.wrapped.compute(context);
        }
    }

    // case "noise" -> new Noise(
    //                    root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
    //                    root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
    //                    NoiseParser.apply(root.get("noise"))
    //            );
    class Noise implements DensityFunction {
        private final double xzScale;
        private final double yScale;
        private final NormalNoise.NoiseParameters noiseData;
        private NormalNoise noise = null;

        public Noise(double xzScale, double yScale, NormalNoise.NoiseParameters noiseData) {
            this.xzScale = xzScale;
            this.yScale = yScale;
            this.noiseData = noiseData;
        }

        @Override
        public double compute(Context context) {
            if (this.noise == null) {
                int nativeHash = System.identityHashCode(this);
                WorldgenRandom random = WorldgenRandom.standard(nativeHash);
                this.noise = new NormalNoise(random, this.noiseData);
            }
            return this.noise.sample(context.x() * this.xzScale, context.y() * this.yScale, context.z() * this.xzScale);
        }

        @Override
        public double maxValue() {
            return this.noise == null ? 2 : this.noise.maxValue;
        }
    }

    // case "end_islands" -> new EndIslands();
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
                    double f2 = 100 - Math.sqrt(x3 * x3 + z3 * z3) * f1;
                    double f3 = Util.clamp(f2, -100, 80);
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

    // case "weird_scaled_sampler" -> new WeirdScaledSampler(
    //                    inputParser.apply(root.get("input")),
    //                    root.get("rarity_value_mapper").getAsString(),
    //                    NoiseParser.apply(root.get("noise"))
    //            );
    record WeirdScaledSampler(DensityFunction input, String rarity_value_mapper, NormalNoise.NoiseParameters noiseData) implements DensityFunction {
        private static final Map<String, Double2DoubleFunction> VALUE_MAPPER = new HashMap<>();

        static {
            VALUE_MAPPER.put("type_1", WeirdScaledSampler::rarityValueMapper1);
            VALUE_MAPPER.put("type_2", WeirdScaledSampler::rarityValueMapper2);
        }

        @Override
        public double compute(Context context) {
            double value = input.compute(context);
            // TODO: Implement this
//            if (this.noise == null) {
//                return 0;
//            }
//            double rarity = this.mapper.apply(density);
//            return rarity * Math.abs(this.noise.sample(context.x() / rarity, context.y() / rarity, context.z() / rarity));
            return value;
        }

        @Override
        public double minValue() {
            return 0;
        }

        @Override
        public double maxValue() {
            return "type_1".equals(rarity_value_mapper) ? 2 : 3;
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











    class Constant implements DensityFunction {
        public static final Constant ZERO = new Constant(0);
        public static Constant ONE = new Constant(1);

        protected final double value;

        public Constant(double value) {
            this.value = value;
        }

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

    record DatapackDefined(NamespaceID namespaceID) implements DensityFunction {
        public double compute(Context context) {
            // TODO: Implement this
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public double minValue() {
            return 0;
        }

        @Override
        public double maxValue() {
            return 1;
        }
    }

//    public class ConstantMinMax implements DensityFunction.Constant {
//        constructor(
//                value: number,
//                private readonly min: number,
//                private readonly max: number
//        ){
//            super(value)
//        }
//
//        public minValue() {
//            return this.min
//        }
//
//        public maxValue() {
//            return this.max
//        }
//    }

    class ConstantMinMax extends Constant {

        final double min;
        final double max;

        public ConstantMinMax(double value, double min, double max) {
            super(value);
            this.min = min;
            this.max = max;
        }

        @Override
        public double minValue() {
            return this.min;
        }

        @Override
        public double maxValue() {
            return this.max;
        }
    }

//    abstract class Wrapper implements DensityFunction {
//        constructor(
//                protected readonly wrapped: DensityFunction,
//                ) {
//            super()
//        }
//        public minValue() {
//            return this.wrapped.minValue()
//        }
//        public maxValue() {
//            return this.wrapped.maxValue()
//        }
//    }

    abstract class Wrapper implements DensityFunction {
        protected final DensityFunction wrapped;

        public Wrapper(DensityFunction wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public double minValue() {
            return this.wrapped.minValue();
        }

        @Override
        public double maxValue() {
            return this.wrapped.maxValue();
        }
    }

    class NoiseRoot implements DensityFunction {
        public final double xzScale;
        public double xzScale() {
            return this.xzScale;
        }
        public final double yScale;
        public double yScale() {
            return this.yScale;
        }
        public final NormalNoise.NoiseParameters noiseData;
        public NormalNoise.NoiseParameters noiseData() {
            return this.noiseData;
        }
        public final NormalNoise noise;

        public NormalNoise noise() {
            return this.noise;
        }
        public NoiseRoot(double xzScale, double yScale, NormalNoise.NoiseParameters noiseData) {
            this(xzScale, yScale, noiseData, null);
        }
        public NoiseRoot(double xzScale, double yScale, NormalNoise.NoiseParameters noiseData, @Nullable NormalNoise noise) {
            this.noiseData = noiseData;
            this.xzScale = xzScale;
            this.yScale = yScale;
            this.noise = noise;
        }
        @Override
        public double compute(Context context) {
            if (this.noise == null) return 0;
            return this.noise.sample(context.x() * this.xzScale, context.y() * this.yScale, context.z() * this.xzScale);
        }
        @Override
        public double maxValue() {
            return this.noise == null ? 2 : this.noise.maxValue;
        }
    }

    class ShiftedNoise extends NoiseRoot {

        public final DensityFunction shiftX;

        public DensityFunction shiftX() {
            return shiftX;
        }

        public final DensityFunction shiftZ;

        public DensityFunction shiftZ() {
            return shiftZ;
        }

        public final DensityFunction shiftY;

        public DensityFunction shiftY() {
            return shiftY;
        }

        public ShiftedNoise(
                DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ,
                double xzScale, double yScale,
                NormalNoise.NoiseParameters noiseData, @Nullable NormalNoise noise) {
            super(xzScale, yScale, noiseData, noise);
            this.shiftX = shiftX;
            this.shiftY = shiftY;
            this.shiftZ = shiftZ;
        }

        //        public compute(context: Context) {
//			const xx = context.x * this.xzScale + this.shiftX.compute(context)
//			const yy = context.y * this.yScale + this.shiftY.compute(context)
//			const zz = context.z * this.xzScale + this.shiftZ.compute(context)
//            return this.noise?.sample(xx, yy, zz) ?? 0
//        }
        public double compute(Context context) {
            return this.noise.sample(
                    context.x() * this.xzScale + this.shiftX.compute(context),
                    context.y() * this.yScale + this.shiftY.compute(context),
                    context.z() * this.xzScale + this.shiftZ.compute(context)
            );
        }

    }

    class RangeChoice implements DensityFunction {
        public DensityFunction input;
        public double minInclusive;
        public double maxExclusive;
        public DensityFunction whenInRange;
        public DensityFunction whenOutOfRange;

        RangeChoice(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange,
                    DensityFunction whenOutOfRange) {
            this.input = input;
            this.minInclusive = minInclusive;
            this.maxExclusive = maxExclusive;
            this.whenInRange = whenInRange;
            this.whenOutOfRange = whenOutOfRange;
        }
//        public compute(context: Context) {
//			const x = this.input.compute(context)
//            return (this.minInclusive <= x && x < this.maxExclusive)
//                    ? this.whenInRange.compute(context)
//                    : this.whenOutOfRange.compute(context)
//        }

        public double compute(Context context) {
            return this.input.compute(context) >= this.minInclusive && this.input.compute(context) < this.maxExclusive
                    ? this.whenInRange.compute(context)
                    : this.whenOutOfRange.compute(context);
        }

//        public mapAll(visitor: Visitor) {
//            return visitor.map(new RangeChoice(this.input.mapAll(visitor), this.minInclusive, this.maxExclusive, this.whenInRange.mapAll(visitor), this.whenOutOfRange.mapAll(visitor)))
//        }

        //        public minValue() {
//            return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue())
//        }
//        public maxValue() {
//            return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue())
//        }

        public double minValue() {
            return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue());
        }

        public double maxValue() {
            return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue());
        }
    }

    abstract class ShiftNoise implements DensityFunction {
        public final NormalNoise.NoiseParameters noiseData;

        public NormalNoise.NoiseParameters noiseData() {
            return noiseData;
        }

        public final NormalNoise offsetNoise;

        public NormalNoise offsetNoise() {
            return offsetNoise;
        }

        ShiftNoise(
                NormalNoise.NoiseParameters noiseData,
                @Nullable NormalNoise offsetNoise) {
            this.noiseData = noiseData;
            this.offsetNoise = offsetNoise;
        }

        //        public compute(context: Context) {
//            return this.offsetNoise?.sample(context.x * 0.25, context.y * 0.25, context.z * 0.25) ?? 0
//        }
        public double compute(Context context) {
            return this.offsetNoise.sample(context.x() * 0.25, context.y() * 0.25, context.z() * 0.25);
        }

        //        public maxValue() {
//            return (this.offsetNoise?.maxValue ?? 2) * 4
//        }
        public double maxValue() {
            return this.offsetNoise.maxValue * 4;
        }

        //        public abstract withNewNoise(noise: NormalNoise): ShiftNoise
        public abstract ShiftNoise withNewNoise(NormalNoise noise);
    }

    class ShiftA extends ShiftNoise {

        ShiftA(NormalNoise.NoiseParameters noiseData, @Nullable NormalNoise offsetNoise) {
            super(noiseData, offsetNoise);
        }

        ShiftA(NormalNoise.NoiseParameters noiseData) {
            super(noiseData, null);
        }

        //        public compute(context: Context) {
//            return super.compute(DensityFunction.context(context.x, 0, context.z))
//        }
        public double compute(Context context) {
            return super.compute(DensityFunction.context(context.x(), 0, context.z()));
        }

        //        public withNewNoise(newNoise: NormalNoise) {
//            return new ShiftA(this.noiseData, newNoise)
//        }
        public ShiftNoise withNewNoise(NormalNoise newNoise) {
            return new ShiftA(this.noiseData, newNoise);
        }
    }

    //    public class ShiftB extends ShiftNoise {
//        constructor(
//                noiseData: Holder<NoiseParameters>,
//                offsetNoise?: NormalNoise,
//                ) {
//            super(noiseData, offsetNoise)
//        }
//        public compute(context: Context) {
//            return super.compute(DensityFunction.context(context.z, context.x, 0))
//        }
//        public withNewNoise(newNoise: NormalNoise) {
//            return new ShiftB(this.noiseData, newNoise)
//        }
//    }
    class ShiftB extends ShiftNoise {
        ShiftB(NormalNoise.NoiseParameters noiseData, NormalNoise offsetNoise) {
            super(noiseData, offsetNoise);
        }

        ShiftB(NormalNoise.NoiseParameters noiseData) {
            super(noiseData, null);
        }

        public double compute(Context context) {
            return super.compute(DensityFunction.context(context.z(), context.x(), 0));
        }

        public ShiftNoise withNewNoise(NormalNoise newNoise) {
            return new ShiftB(this.noiseData, newNoise);
        }
    }

    //    public class Shift extends ShiftNoise {
//        constructor(
//                noiseData: Holder<NoiseParameters>,
//                offsetNoise?: NormalNoise,
//                ) {
//            super(noiseData, offsetNoise)
//        }
//        public withNewNoise(newNoise: NormalNoise) {
//            return new Shift(this.noiseData, newNoise)
//        }
//    }
    class Shift extends ShiftNoise {
        Shift(NormalNoise.NoiseParameters noiseData, NormalNoise offsetNoise) {
            super(noiseData, offsetNoise);
        }

        Shift(NormalNoise.NoiseParameters noiseData) {
            super(noiseData, null);
        }

        public ShiftNoise withNewNoise(NormalNoise newNoise) {
            return new Shift(this.noiseData, newNoise);
        }
    }

    interface Transformer extends DensityFunction {

        DensityFunction input();

        double transform(DensityFunction.Context context, double density);

        @Override
        default double compute(DensityFunction.Context context) {
            return this.transform(context, input().compute(context));
        }
    }

    //    public class BlendDensity extends Transformer {
//        constructor(
//                input: DensityFunction,
//                ) {
//            super(input)
//        }
//        public transform(context: Context, density: number) {
//            return density // blender not supported
//        }
//        public mapAll(visitor: Visitor): DensityFunction {
//            return visitor.map(new BlendDensity(this.input.mapAll(visitor)))
//        }
//        public minValue() {
//            return -Infinity
//        }
//        public maxValue() {
//            return Infinity
//        }
//    }
    record BlendDensity(DensityFunction input) implements Transformer {

        public double transform(Context context, double density) {
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

    record Clamp(double min, double max, DensityFunction input) implements Transformer {

        public double transform(Context context, double density) {
            return Util.clamp(density, this.min, this.max);
        }

        public double minValue() {
            return this.min;
        }

        public double maxValue() {
            return this.max;
        }
    }

    record Abs(DensityFunction input) implements Transformer {

        public double transform(Context context, double density) {
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

    record Square(DensityFunction input) implements Transformer {

        public double transform(Context context, double density) {
            return Util.square(density);
        }

        public double minValue() {
            return Util.square(this.input.minValue());
        }

        public double maxValue() {
            return Util.square(this.input.maxValue());
        }
    }

    record Cube(DensityFunction input) implements Transformer {

        public double transform(Context context, double density) {
            return Util.cube(density);
        }

        public double minValue() {
            return Util.cube(this.input.minValue());
        }

        public double maxValue() {
            return Util.cube(this.input.maxValue());
        }
    }

    record HalfNegative(DensityFunction input) implements Transformer {

        public double transform(Context context, double density) {
            return density > 0 ? density : density * 0.5;
        }

        public double minValue() {
            return this.input.minValue() * 0.5;
        }

        public double maxValue() {
            return this.input.maxValue();
        }
    }

    record QuarterNegative(DensityFunction input) implements Transformer {

        public double transform(Context context, double density) {
            return density > 0 ? density : density * 0.25;
        }

        public double minValue() {
            return this.input.minValue() * 0.25;
        }

        public double maxValue() {
            return this.input.maxValue();
        }
    }

    record Squeeze(DensityFunction input) implements Transformer {

        public double transform(Context context, double density) {
            double c = Util.clamp(density, -1, 1);
            return c / 2 - c * c * c / 24;
        }

        public double minValue() {
            return this.input.minValue() / 2 - this.input.maxValue() * this.input.maxValue() * this.input.maxValue() / 24;
        }

        public double maxValue() {
            return this.input.maxValue() / 2 - this.input.minValue() * this.input.minValue() * this.input.minValue() / 24;
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
