package net.minestom.vanilla.generation.densityfunctions;

import net.minestom.vanilla.generation.Holder;
import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.math.CubicSpline;
import net.minestom.vanilla.generation.noise.BlendedNoise;
import net.minestom.vanilla.generation.noise.NormalNoise;
import net.minestom.vanilla.generation.noise.SimplexNoise;
import net.minestom.vanilla.generation.random.WorldgenRandom;
import net.minestom.vanilla.generation.util.DoubleStorage;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

class NativeDensityFunctions {

    abstract static class Transformer implements DensityFunction {
        protected final DensityFunction input;

        public DensityFunction input() {
            return input;
        }

        protected Transformer(DensityFunction input) {
            this.input = input;
        }

        abstract double transform(DensityFunction.Context context, double density);

        public double compute(DensityFunction.Context context) {
            return this.transform(context, input.compute(context));
        }
    }

    static abstract class Wrapper implements DensityFunction {
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

    static abstract class ShiftNoise implements DensityFunction {
        public final Holder<NormalNoise.NoiseParameters> noiseData;

        public Holder<NormalNoise.NoiseParameters> noiseData() {
            return noiseData;
        }

        public final NormalNoise offsetNoise;

        public NormalNoise offsetNoise() {
            return offsetNoise;
        }

        ShiftNoise(
                Holder<NormalNoise.NoiseParameters> noiseData,
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

    static class Constant implements DensityFunction {
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

    record HolderHolder(Holder<DensityFunction> holder) implements DensityFunction {
        public double compute(Context context) {
            return this.holder().value().compute(context);
        }

        @Override
        public double minValue() {
            return this.holder().value().minValue();
        }

        @Override
        public double maxValue() {
            return this.holder().value().maxValue();
        }
    }

    static class ConstantMinMax extends Constant {

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

    record OldBlendedNoise(double xzScale, double yScale,
                           double xzFactor, double yFactor,
                           double smearScaleMultiplier,
                           @Nullable BlendedNoise blendedNoise) implements DensityFunction {

        public OldBlendedNoise(double xzScale, double yScale, double xzFactor, double yFactor, double smearScaleMultiplier) {
            this(xzScale, yScale, xzFactor, yFactor, smearScaleMultiplier, null);
        }

        @Override
        public double compute(Context context) {
            return this.blendedNoise.sample(context.x(), context.y(), context.z());
        }

        @Override
        public double maxValue() {
            return this.blendedNoise.maxValue;
        }
    }

    static class FlatCache extends Wrapper {
        private int lastQuartX = 0;
        private int lastQuartZ = 0;
        private double lastValue = 0;

        public FlatCache(DensityFunction wrapped) {
            super(wrapped);
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

        public DensityFunction mapAll(Mapper mapper) {
            return mapper.map(new NativeDensityFunctions.FlatCache(this.wrapped.mapAll(mapper)));
        }
    }

    static class CacheAllInCell extends Wrapper {
        public CacheAllInCell(DensityFunction wrapped) {
            super(wrapped);
        }

        public double compute(Context context) {
            return this.wrapped.compute(context);
        }

        public DensityFunction mapAll(Mapper mapper) {
            return mapper.map(new CacheAllInCell(this.wrapped.mapAll(mapper)));
        }
    }

    static class Cache2D extends Wrapper {
        private int lastBlockX = 0;
        private int lastBlockZ = 0;
        private double lastValue = 0;

        public Cache2D(DensityFunction wrapped) {
            super(wrapped);
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

        public DensityFunction mapAll(Mapper mapper) {
            return mapper.map(new Cache2D(this.wrapped.mapAll(mapper)));
        }
    }

    static class CacheOnce extends Wrapper {
        private int lastBlockX = 0;
        private int lastBlockY = 0;
        private int lastBlockZ = 0;
        private double lastValue = 0;

        public CacheOnce(DensityFunction wrapped) {
            super(wrapped);
        }

        public double compute(Context context) {
            int blockX = context.blockX();
            int blockY = context.blockY();
            int blockZ = context.blockZ();
            if (this.lastBlockX != blockX || this.lastBlockY != blockY || this.lastBlockZ != blockZ) {
                this.lastValue = this.wrapped.compute(context);
                this.lastBlockX = blockX;
                this.lastBlockY = blockY;
                this.lastBlockZ = blockZ;
            }
            return this.lastValue;
        }

        public DensityFunction mapAll(Mapper mapper) {
            return mapper.map(new CacheOnce(this.wrapped.mapAll(mapper)));
        }
    }

    static class Interpolated extends Wrapper {

        private final DoubleStorage storage;
        private final int cellWidth;
        private final int cellHeight;

        public Interpolated(DensityFunction wrapped) {
            this(wrapped, 4, 4);
        }

        public Interpolated(DensityFunction wrapped, int cellWidth, int cellHeight) {
            super(wrapped);
            this.storage = DoubleStorage.exactCache(DoubleStorage.from(wrapped));
            this.cellWidth = cellWidth;
            this.cellHeight = cellHeight;
        }

        @Override
        public double compute(Context context) {
            int blockX = context.blockX();
            int blockY = context.blockY();
            int blockZ = context.blockZ();
            int w = this.cellWidth;
            int h = this.cellHeight;
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

        @Override
        public DensityFunction mapAll(Mapper mapper) {
            return mapper.map(new Interpolated(this.wrapped.mapAll(mapper), this.cellWidth, this.cellHeight));
        }

        public DensityFunction withCellSize(int cellWidth, int cellHeight) {
            return new Interpolated(this.wrapped, cellWidth, cellHeight);
        }
    }

    static class Noise implements DensityFunction {
        public final double xzScale;

        public double xzScale() {
            return this.xzScale;
        }

        public final double yScale;

        public double yScale() {
            return this.yScale;
        }

        public final Holder<NormalNoise.NoiseParameters> noiseData;

        public Holder<NormalNoise.NoiseParameters> noiseData() {
            return this.noiseData;
        }

        public final NormalNoise noise;

        public NormalNoise noise() {
            return this.noise;
        }

        public Noise(double xzScale, double yScale, Holder<NormalNoise.NoiseParameters> noiseData) {
            this(xzScale, yScale, noiseData, null);
        }

        public Noise(double xzScale, double yScale, Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise noise) {
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

    static class EndIslands implements DensityFunction {
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

    static class WeirdScaledSampler extends Transformer {
        private static final Map<String, Function<Double, Double>> ValueMapper = new HashMap<>();

        static {
            ValueMapper.put("type_1", WeirdScaledSampler::rarityValueMapper1);
            ValueMapper.put("type_2", WeirdScaledSampler::rarityValueMapper2);
        }

        private final Function<Double, Double> mapper;
        private final String rarityValueMapper;

        public String rarityValueMapper() {
            return this.rarityValueMapper;
        }

        private final Holder<NormalNoise.NoiseParameters> noiseData;

        public Holder<NormalNoise.NoiseParameters> noiseData() {
            return this.noiseData;
        }

        private final @Nullable NormalNoise noise;

        public @Nullable NormalNoise noise() {
            return this.noise;
        }

        public WeirdScaledSampler(DensityFunction input, String rarityValueMapper,
                                  Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise noise) {
            super(input);
            this.rarityValueMapper = rarityValueMapper;
            this.mapper = WeirdScaledSampler.ValueMapper.get(rarityValueMapper);
            this.noiseData = noiseData;
            this.noise = noise;
        }

        public WeirdScaledSampler(DensityFunction input, String rarityValueMapper,
                                  Holder<NormalNoise.NoiseParameters> noiseData) {
            this(input, rarityValueMapper, noiseData, null);
        }

        @Override
        public double transform(Context context, double density) {
            if (this.noise == null) {
                return 0;
            }
            double rarity = this.mapper.apply(density);
            return rarity * Math.abs(this.noise.sample(context.x() / rarity, context.y() / rarity, context.z() / rarity));
        }

        @Override
        public DensityFunction mapAll(Mapper mapper) {
            return mapper.map(new WeirdScaledSampler(this.input.mapAll(mapper), this.rarityValueMapper, this.noiseData, this.noise));
        }

        @Override
        public double minValue() {
            return 0;
        }

        @Override
        public double maxValue() {
            return rarityValueMapper.equals("type_1") ? 2 : 3;
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

    static class ShiftedNoise extends Noise {

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
                Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise noise) {
            super(xzScale, yScale, noiseData, noise);
            this.shiftX = shiftX;
            this.shiftY = shiftY;
            this.shiftZ = shiftZ;
        }

        public double compute(Context context) {
            return this.noise.sample(
                    context.x() * this.xzScale + this.shiftX.compute(context),
                    context.y() * this.yScale + this.shiftY.compute(context),
                    context.z() * this.xzScale + this.shiftZ.compute(context)
            );
        }

        public DensityFunction mapAll(Mapper mapper) {
            return mapper.map(new ShiftedNoise(this.shiftX.mapAll(mapper), this.shiftY.mapAll(mapper), this.shiftZ.mapAll(mapper), this.xzScale, this.yScale, this.noiseData, this.noise));
        }
    }

    static class RangeChoice implements DensityFunction {
        public final DensityFunction input;
        public final double minInclusive;
        public final double maxExclusive;
        public final DensityFunction whenInRange;
        public final DensityFunction whenOutOfRange;

        RangeChoice(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange,
                    DensityFunction whenOutOfRange) {
            this.input = input;
            this.minInclusive = minInclusive;
            this.maxExclusive = maxExclusive;
            this.whenInRange = whenInRange;
            this.whenOutOfRange = whenOutOfRange;
        }

        public double compute(Context context) {
            return this.input.compute(context) >= this.minInclusive && this.input.compute(context) < this.maxExclusive
                    ? this.whenInRange.compute(context)
                    : this.whenOutOfRange.compute(context);
        }

        public DensityFunction mapAll(Mapper mapper) {
            return mapper.map(new RangeChoice(this.input.mapAll(mapper), this.minInclusive, this.maxExclusive, this.whenInRange.mapAll(mapper), this.whenOutOfRange.mapAll(mapper)));
        }

        public double minValue() {
            return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue());
        }

        public double maxValue() {
            return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue());
        }
    }

    static class ShiftA extends ShiftNoise {

        ShiftA(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise offsetNoise) {
            super(noiseData, offsetNoise);
        }

        ShiftA(Holder<NormalNoise.NoiseParameters> noiseData) {
            super(noiseData, null);
        }

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

    static class ShiftB extends ShiftNoise {
        ShiftB(Holder<NormalNoise.NoiseParameters> noiseData, NormalNoise offsetNoise) {
            super(noiseData, offsetNoise);
        }

        ShiftB(Holder<NormalNoise.NoiseParameters> noiseData) {
            super(noiseData, null);
        }

        public double compute(Context context) {
            return super.compute(DensityFunction.context(context.z(), context.x(), 0));
        }

        public ShiftNoise withNewNoise(NormalNoise newNoise) {
            return new ShiftB(this.noiseData, newNoise);
        }
    }

    static class Shift extends ShiftNoise {
        Shift(Holder<NormalNoise.NoiseParameters> noiseData, NormalNoise offsetNoise) {
            super(noiseData, offsetNoise);
        }

        Shift(Holder<NormalNoise.NoiseParameters> noiseData) {
            super(noiseData, null);
        }

        public ShiftNoise withNewNoise(NormalNoise newNoise) {
            return new Shift(this.noiseData, newNoise);
        }
    }

    static class BlendDensity extends Transformer {
        BlendDensity(DensityFunction input) {
            super(input);
        }

        public double transform(Context context, double density) {
            return density; // blender not supported
        }

        public DensityFunction mapAll(Mapper mapper) {
            return mapper.map(new BlendDensity(this.input.mapAll(mapper)));
        }

        public double minValue() {
            return -Double.MAX_VALUE;
        }

        public double maxValue() {
            return Double.MAX_VALUE;
        }
    }

    static class Clamp extends Transformer {
        final double min;
        final double max;

        Clamp(DensityFunction input, double min, double max) {
            super(input);
            this.min = min;
            this.max = max;
        }

        public double transform(Context context, double density) {
            return Util.clamp(density, this.min, this.max);
        }

        public DensityFunction mapAll(Mapper mapper) {
            return mapper.map(new Clamp(this.input.mapAll(mapper), this.min, this.max));
        }

        public double minValue() {
            return this.min;
        }

        public double maxValue() {
            return this.max;
        }
    }

    static class Mapped extends Transformer {
        private static final Map<String, Function<Double, Double>> MappedTypes = new HashMap<>();

        static {
            MappedTypes.put("abs", Math::abs);
            MappedTypes.put("square", d -> d * d);
            MappedTypes.put("cube", d -> d * d * d);
            MappedTypes.put("half_negative", d -> d > 0 ? d : d * 0.5);
            MappedTypes.put("quarter_negative", d -> d > 0 ? d : d * 0.25);
            MappedTypes.put("squeeze", d -> {
                double c = Util.clamp(d, -1, 1);
                return c / 2 - c * c * c / 24;
            });
        }

        private final Function<Double, Double> transformer;
        private final String type;
        private final Double min;
        private final Double max;

        Mapped(String type, DensityFunction input, @Nullable Double min, @Nullable Double max) {
            super(input);
            this.type = type;
            this.transformer = Mapped.MappedTypes.get(type);
            this.min = min;
            this.max = max;
        }

        Mapped(String type, DensityFunction input) {
            this(type, input, null, null);
        }

        public double transform(Context context, double density) {
            return this.transformer.apply(density);
        }

        public DensityFunction mapAll(Mapper mapper) {
            return mapper.map(new Mapped(this.type, this.input.mapAll(mapper)));
        }

        public double minValue() {
            return this.min == null ? -Double.MAX_VALUE : this.min;
        }

        public double maxValue() {
            return this.max == null ? Double.MAX_VALUE : this.max;
        }

        public Mapped withMinMax() {
            double minInput = this.input.minValue();
            double min = this.transformer.apply(minInput);
            double max = this.transformer.apply(this.input.maxValue());
            if (this.type.equals("abs") || this.type.equals("square")) {
                max = Math.max(min, max);
                min = Math.max(0, minInput);
            }
            return new Mapped(this.type, this.input, min, max);
        }
    }

    static class Ap2 implements DensityFunction {
        final String type;
        final DensityFunction argument1;
        final DensityFunction argument2;
        final Double min;
        final Double max;

        public Ap2(String type, DensityFunction argument1, DensityFunction argument2) {
            this(type, argument1, argument2, null, null);
        }

        Ap2(String type, DensityFunction argument1, DensityFunction argument2, Double min, Double max) {
            this.type = type;
            this.argument1 = argument1;
            this.argument2 = argument2;
            this.min = min;
            this.max = max;
        }

        public double compute(Context context) {
            double a = this.argument1.compute(context);
            return switch (this.type) {
                case "add" -> a + this.argument2.compute(context);
                case "mul" -> a == 0 ? 0 : a * this.argument2.compute(context);
                case "min" -> a < this.argument2.minValue() ? a : Math.min(a, this.argument2.compute(context));
                case "max" -> a > this.argument2.maxValue() ? a : Math.max(a, this.argument2.compute(context));
                default -> 0;
            };
        }

        public DensityFunction mapAll(Mapper mapper) {
            return mapper.map(new Ap2(this.type, this.argument1.mapAll(mapper), this.argument2.mapAll(mapper)));
        }

        public double minValue() {
            return this.min == null ? -Double.MAX_VALUE : this.min;
        }

        public double maxValue() {
            return this.max == null ? Double.MAX_VALUE : this.max;
        }

//    public withMinMax() {
//			const min1 = this.argument1.minValue()
//			const min2 = this.argument2.minValue()
//			const max1 = this.argument1.maxValue()
//			const max2 = this.argument2.maxValue()
//            if ((this.type === 'min' || this.type === 'max') && (min1 >= max2 || min2 >= max1)) {
//                console.warn(`Creating a ${this.type} function between two non-overlapping inputs`)
//            }
//            let min, max
//            switch (this.type) {
//                case 'add':
//                    min = min1 + min2
//                    max = max1 + max2
//                    break
//                case 'mul':
//                    min = min1 > 0 && min2 > 0 ? (min1 * min2) || 0
//                            : max1 < 0 && max2 < 0 ? (max1 * max2) || 0
//                            : Math.min((min1 * max2) || 0, (min2 * max1) || 0)
//                    max = min1 > 0 && min2 > 0 ? (max1 * max2) || 0
//                            : max1 < 0 && max2 < 0 ? (min1 * min2) || 0
//                            : Math.max((min1 * min2) || 0, (max1 * max2) || 0)
//                    break
//                case 'min':
//                    min = Math.min(min1, min2)
//                    max = Math.min(max1, max2)
//                    break
//                case 'max':
//                    min = Math.max(min1, min2)
//                    max = Math.max(max1, max2)
//                    break
//            }
//            return new Ap2(this.type, this.argument1, this.argument2, min, max)
//        }

        public DensityFunction withMinMax() {
            double min1 = this.argument1.minValue();
            double min2 = this.argument2.minValue();
            double max1 = this.argument1.maxValue();
            double max2 = this.argument2.maxValue();
            if ((this.type.equals("min") || this.type.equals("max")) && (min1 >= max2 || min2 >= max1)) {
                System.out.println("Creating a " + this.type + " function between two non-overlapping inputs");
            }
            Double min = null;
            Double max = null;
            switch (this.type) {
                case "add" -> {
                    min = min1 + min2;
                    max = max1 + max2;
                }
                case "mul" -> {
                    min = min1 > 0 && min2 > 0 ? (min1 * min2)
                            : max1 < 0 && max2 < 0 ? (max1 * max2)
                            : Math.min((min1 * max2), (min2 * max1));
                    max = min1 > 0 && min2 > 0 ? (max1 * max2)
                            : max1 < 0 && max2 < 0 ? (min1 * min2)
                            : Math.max((min1 * min2), (max1 * max2));
                }
                case "min" -> {
                    min = Math.min(min1, min2);
                    max = Math.min(max1, max2);
                }
                case "max" -> {
                    min = Math.max(min1, min2);
                    max = Math.max(max1, max2);
                }
            }
            return new Ap2(this.type, this.argument1, this.argument2, min, max);
        }
    }

    record Spline(CubicSpline<Context> spline) implements DensityFunction {

        public double compute(Context context) {
            return this.spline.compute(context);
        }

        public DensityFunction mapAll(Mapper mapper) {
            CubicSpline<Context> newCubicSpline = this.spline.mapAll((fn) -> {
                if (fn instanceof DensityFunction densityFunction) {
                    return densityFunction.mapAll(mapper);
                }
                return fn;
            });
            newCubicSpline.calculateMinMax();
            return mapper.map(new Spline(newCubicSpline));
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
