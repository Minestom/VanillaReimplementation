package net.minestom.vanilla.generation.densityfunctions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.Holder;
import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.WorldgenRegistries;
import net.minestom.vanilla.generation.math.CubicSpline;
import net.minestom.vanilla.generation.noise.BlendedNoise;
import net.minestom.vanilla.generation.noise.NormalNoise;
import net.minestom.vanilla.generation.noise.SimplexNoise;
import net.minestom.vanilla.generation.random.WorldgenRandom;
import net.minestom.vanilla.generation.util.DoubleStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

public interface DensityFunctions {
    interface Visitor {
        Function<DensityFunction, DensityFunction> map();
    }

    interface Transformer extends DensityFunction {

        @NotNull DensityFunction input();

        double transform(@NotNull Point point, double density);

        @Override
        default double compute(Point point) {
            return transform(point, input().compute(point));
        }
    }

    Function<Object, Holder<NormalNoise.NoiseParameters>> NoiseParser = Holder.parser(WorldgenRegistries.NOISE, NormalNoise.NoiseParameters::fromJson);

    static DensityFunction fromJson(Object obj) {
        return DensityFunctions.fromJson(obj, null);
    }

    static DensityFunction fromJson(Object obj, Function<Object, DensityFunction> inputParser) {
        if (inputParser == null) {
            inputParser = DensityFunctions::fromJson;
        }
        if (obj instanceof String str) {
            Double strDouble = Util.parseDouble(str);
            if (strDouble != null) {
                return new Constant(strDouble);
            }
            if (WorldgenRegistries.DENSITY_FUNCTION.get(NamespaceID.from(str)) != null) {
                return WorldgenRegistries.DENSITY_FUNCTION.get(NamespaceID.from(str));
            }
            return fromJson(Util.jsonObject(str));
        }
        if (obj instanceof Number number) {
            return new Constant(number.doubleValue());
        }

//		const root = Json.readObject(obj) ?? {}
        JsonObject root;
        {
            JsonElement element = new Gson().fromJson(obj.toString(), JsonElement.class);
            if (element instanceof JsonObject json) {
                root = json;
            } else if (Util.jsonIsString(element)) {
                return new DensityFunctions.HolderHolder(Holder.reference(WorldgenRegistries.DENSITY_FUNCTION,
                        NamespaceID.from(Util.jsonToString(element))));
            } else if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    return new Constant(primitive.getAsDouble());
                }
                throw new RuntimeException("Unknown density function: " + obj);
            } else {
                throw new RuntimeException("Unknown density function: " + obj);
            }
        }

        String type;
        {
            var value = root.get("type");
            if (value == null) {
                return Constant.ZERO;
            }
            type = value.getAsString().replaceFirst("minecraft:", "");
        }

        return switch (type) {
            case "blend_alpha" -> new ConstantMinMax(1, 0, 1);
            case "blend_offset" -> new ConstantMinMax(0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            case "beardifier" -> new ConstantMinMax(0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            case "old_blended_noise" -> new OldBlendedNoise(
                    root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
                    root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
                    root.get("xz_factor") == null ? 80 : root.get("xz_factor").getAsDouble(),
                    root.get("y_factor") == null ? 160 : root.get("y_factor").getAsDouble(),
                    root.get("smear_scale_multiplier") == null ? 8 : root.get("smear_scale_multiplier").getAsDouble()
            );
            case "flat_cache" -> new FlatCache(inputParser.apply(root.get("argument")));
            case "interpolated" -> new Interpolated(inputParser.apply(root.get("argument")));
            case "cache_2d" -> new Cache2D(inputParser.apply(root.get("argument")));
            case "cache_once" -> new CacheOnce(inputParser.apply(root.get("argument")));
            case "cache_all_in_cell" -> new CacheAllInCell(inputParser.apply(root.get("argument")));
            case "noise" -> new Noise(
                    root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
                    root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
                    NoiseParser.apply(root.get("noise"))
            );
            case "end_islands" -> new EndIslands();
            case "weird_scaled_sampler" -> new WeirdScaledSampler(
                    inputParser.apply(root.get("input")),
                    root.get("rarity_value_mapper").getAsString(),
                    NoiseParser.apply(root.get("noise"))
            );
            case "shifted_noise" -> new ShiftedNoise(
                    inputParser.apply(root.get("shift_x")),
                    inputParser.apply(root.get("shift_y")),
                    inputParser.apply(root.get("shift_z")),
                    root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
                    root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
                    NoiseParser.apply(root.get("noise")),
                    null
            );
            case "range_choice" -> new RangeChoice(
                    inputParser.apply(root.get("input")),
                    root.get("min_inclusive") == null ? 0 : root.get("min_inclusive").getAsDouble(),
                    root.get("max_exclusive") == null ? 1 : root.get("max_exclusive").getAsDouble(),
                    inputParser.apply(root.get("when_in_range")),
                    inputParser.apply(root.get("when_out_of_range"))
            );
            case "shift_a" -> new ShiftA(NoiseParser.apply(root.get("argument")));
            case "shift_b" -> new ShiftB(NoiseParser.apply(root.get("argument")));
            case "blend_density" -> new BlendDensity(inputParser.apply(root.get("argument")));
            case "clamp" -> new Clamp(
                    inputParser.apply(root.get("input")),
                    root.get("min") == null ? 0 : root.get("min").getAsDouble(),
                    root.get("max") == null ? 1 : root.get("max").getAsDouble()
            );
            case "abs", "square", "cube", "half_negative", "quarter_negative", "squeeze" ->
                    new Mapped(type, inputParser.apply(root.get("argument")));
            case "add", "mul", "min", "max" -> new Ap2(
                    type,
                    inputParser.apply(root.get("argument1")),
                    inputParser.apply(root.get("argument2"))
            );
            case "spline" -> new Spline(
                    CubicSpline.fromJson(root.get("spline"), inputParser::apply)
            );
            case "constant" -> new Constant(root.get("argument") == null ? 0 : root.get("argument").getAsDouble());
            case "y_clamped_gradient" -> new YClampedGradient(
                    root.get("from_y") == null ? -4064 : root.get("from_y").getAsInt(),
                    root.get("to_y") == null ? 4062 : root.get("to_y").getAsInt(),
                    root.get("from_value") == null ? -4064 : root.get("from_value").getAsDouble(),
                    root.get("to_value") == null ? 4062 : root.get("to_value").getAsDouble()
            );
            default -> Constant.ZERO;
        };
    }

    record Constant(double value) implements DensityFunction {
        public static final Constant ZERO = new Constant(0);
        public static Constant ONE = new Constant(1);

        @Override
        public double compute(Point point) {
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
        public double compute(Point point) {
            return this.holder().value().compute(point);
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

    record ConstantMinMax(double value, double min, double max) implements DensityFunction {

        @Override
        public double compute(Point point) {
            return value;
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

    record OldBlendedNoise(double xzScale,
                           double yScale,
                           double xzFactor,
                           double yFactor,
                           double smearScaleMultiplier,
                           @Nullable BlendedNoise blendedNoise) implements DensityFunction {

        public OldBlendedNoise(double xzScale, double yScale, double xzFactor, double yFactor, double smearScaleMultiplier) {
            this(xzScale, yScale, xzFactor, yFactor, smearScaleMultiplier, null);
        }

        @Override
        public double compute(Point point) {
            return this.blendedNoise.sample(point.x(), point.y(), point.z());
        }

        @Override
        public double maxValue() {
            return this.blendedNoise.maxValue;
        }
    }

    interface Wrapper extends DensityFunction {

        @NotNull DensityFunction wrapped();

        @Override
        default double minValue() {
            return wrapped().minValue();
        }

        @Override
        default double maxValue() {
            return wrapped().maxValue();
        }

    }

    class FlatCache implements Wrapper {
        private final @NotNull DensityFunction wrapped;

        private int lastQuartX = 0, lastQuartZ = 0;
        private double lastValue = 0;

        public FlatCache(@NotNull DensityFunction wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public @NotNull DensityFunction wrapped() {
            return wrapped;
        }

        public double compute(Point point) {
            int quartX = point.blockX() >> 2;
            int quartZ = point.blockZ() >> 2;
            if (this.lastQuartX != quartX || this.lastQuartZ != quartZ) {
                this.lastValue = wrapped().compute(new Vec(quartX << 2, 0, quartZ << 2));
                this.lastQuartX = quartX;
                this.lastQuartZ = quartZ;
            }
            return this.lastValue;
        }

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new FlatCache(this.wrapped.mapAll(visitor)));
        }
    }

    record CacheAllInCell(@NotNull DensityFunction wrapped) implements Wrapper {
        public double compute(Point point) {
            return this.wrapped.compute(point);
        }

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new CacheAllInCell(this.wrapped.mapAll(visitor)));
        }
    }

    class Cache2D implements Wrapper {
        private final @NotNull DensityFunction wrapped;

        private int lastBlockX = 0, lastBlockZ = 0;
        private double lastValue = 0;

        public Cache2D(@NotNull DensityFunction wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public @NotNull DensityFunction wrapped() {
            return wrapped;
        }

        public double compute(Point point) {
            int blockX = point.blockX();
            int blockZ = point.blockZ();
            if (this.lastBlockX != blockX || this.lastBlockZ != blockZ) {
                this.lastValue = wrapped().compute(point);
                this.lastBlockX = blockX;
                this.lastBlockZ = blockZ;
            }
            return this.lastValue;
        }

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new Cache2D(this.wrapped.mapAll(visitor)));
        }
    }

    class CacheOnce implements Wrapper {
        private final @NotNull DensityFunction wrapped;

        private int lastBlockX = 0, lastBlockY = 0, lastBlockZ = 0;
        private double lastValue = 0;

        public CacheOnce(@NotNull DensityFunction wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public @NotNull DensityFunction wrapped() {
            return wrapped;
        }

        public double compute(Point point) {
            int blockX = point.blockX();
            int blockY = point.blockY();
            int blockZ = point.blockZ();
            if (this.lastBlockX != blockX || this.lastBlockY != blockY || this.lastBlockZ != blockZ) {
                this.lastValue = this.wrapped.compute(point);
                this.lastBlockX = blockX;
                this.lastBlockY = blockY;
                this.lastBlockZ = blockZ;
            }
            return this.lastValue;
        }

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new CacheOnce(this.wrapped.mapAll(visitor)));
        }
    }

    record Interpolated(@NotNull DensityFunction wrapped,
                        @NotNull DoubleStorage storage,
                        int cellWidth,
                        int cellHeight) implements Wrapper {

        public Interpolated(DensityFunction wrapped) {
            this(wrapped, 4, 4);
        }

        public Interpolated(DensityFunction wrapped, int cellWidth, int cellHeight) {
            this(wrapped, DoubleStorage.exactCache(DoubleStorage.from(wrapped)), cellWidth, cellHeight);
        }

        @Override
        public double compute(Point point) {
            int blockX = point.blockX();
            int blockY = point.blockY();
            int blockZ = point.blockZ();
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
        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new Interpolated(this.wrapped.mapAll(visitor), this.cellWidth, this.cellHeight));
        }

        public DensityFunction withCellSize(int cellWidth, int cellHeight) {
            return new Interpolated(this.wrapped, cellWidth, cellHeight);
        }
    }

    record Noise(double xzScale, double yScale,
                 @NotNull Holder<NormalNoise.NoiseParameters> noiseData,
                 @NotNull NormalNoise noise) implements DensityFunction {

        public Noise(double xzScale, double yScale, Holder<NormalNoise.NoiseParameters> noiseData) {
            this(xzScale, yScale, noiseData, null);
        }

        @Override
        public double compute(Point point) {
            if (this.noise == null) return 0;
            return this.noise.sample(point.x() * this.xzScale, point.y() * this.yScale, point.z() * this.xzScale);
        }

        @Override
        public double maxValue() {
            return this.noise == null ? 2 : this.noise.maxValue;
        }
    }

    record EndIslands(@NotNull SimplexNoise islandNoise) implements DensityFunction {
        public EndIslands() {
            this(0);
        }

        public EndIslands(long seed) {
            this(standard(seed));
        }

        private static SimplexNoise standard(long seed) {
            WorldgenRandom random = WorldgenRandom.standard(seed);
            random.consume(17292);
            return new SimplexNoise(random);
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
        public double compute(Point point) {
            return (this.getHeightValue(Math.floorDiv(point.blockX(), 8), Math.floorDiv(point.blockZ(), 8)) - 8) / 128;
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

    String[] RarityValueMapper = new String[]{"type_1", "type_2"};

    record WeirdScaledSampler(@NotNull DensityFunction input,
                              @NotNull String rarityValueMapper,
                              @NotNull DoubleUnaryOperator mapper,
                              @NotNull Holder<NormalNoise.NoiseParameters> noiseData,
                              @Nullable NormalNoise noise) implements Transformer {

        private static final Map<String, DoubleUnaryOperator> ValueMapper = Map.of(
                "type_1", WeirdScaledSampler::rarityValueMapper1,
                "type_2", WeirdScaledSampler::rarityValueMapper2
        );


        public WeirdScaledSampler(DensityFunction input, String rarityValueMapper,
                                  Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise noise) {
            this(input, rarityValueMapper, WeirdScaledSampler.ValueMapper.get(rarityValueMapper), noiseData, noise);
        }

        public WeirdScaledSampler(DensityFunction input, String rarityValueMapper,
                                  Holder<NormalNoise.NoiseParameters> noiseData) {
            this(input, rarityValueMapper, noiseData, null);
        }

        @Override
        public double transform(Point point, double density) {
            if (this.noise == null) {
                return 0;
            }
            double rarity = this.mapper.applyAsDouble(density);
            return rarity * Math.abs(this.noise.sample(point.x() / rarity, point.y() / rarity, point.z() / rarity));
        }

        @Override
        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new WeirdScaledSampler(this.input().mapAll(visitor), this.rarityValueMapper, this.noiseData, this.noise));
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

    record ShiftedNoise(@NotNull DensityFunction shiftX,
                        @NotNull DensityFunction shiftY,
                        @NotNull DensityFunction shiftZ,
                        double xzScale, double yScale,
                        @NotNull Holder<NormalNoise.NoiseParameters> noiseData,
                        @NotNull NormalNoise noise) implements DensityFunction {

        @Override
        public double maxValue() {
            return this.noise == null ? 2 : this.noise.maxValue;
        }

        @Override
        public double compute(Point point) {
            return this.noise.sample(
                    point.x() * this.xzScale + this.shiftX.compute(point),
                    point.y() * this.yScale + this.shiftY.compute(point),
                    point.z() * this.xzScale + this.shiftZ.compute(point)
            );
        }

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new ShiftedNoise(this.shiftX.mapAll(visitor), this.shiftY.mapAll(visitor), this.shiftZ.mapAll(visitor), this.xzScale, this.yScale, this.noiseData, this.noise));
        }
    }

    record RangeChoice(@NotNull DensityFunction input,
                       double minInclusive,
                       double maxExclusive,
                       @NotNull DensityFunction whenInRange,
                       @NotNull DensityFunction whenOutOfRange) implements DensityFunction {

        public double compute(Point point) {
            return this.input.compute(point) >= this.minInclusive && this.input.compute(point) < this.maxExclusive
                    ? this.whenInRange.compute(point)
                    : this.whenOutOfRange.compute(point);
        }

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new RangeChoice(this.input.mapAll(visitor), this.minInclusive, this.maxExclusive, this.whenInRange.mapAll(visitor), this.whenOutOfRange.mapAll(visitor)));
        }

        public double minValue() {
            return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue());
        }

        public double maxValue() {
            return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue());
        }
    }

    interface ShiftNoise extends DensityFunction {

        @NotNull Holder<NormalNoise.NoiseParameters> noiseData();

        @Nullable NormalNoise offsetNoise();

        @Override
        default double compute(Point point) {
            return offsetNoise().sample(point.x() * 0.25, point.y() * 0.25, point.z() * 0.25);
        }

        @Override
        default double maxValue() {
            return offsetNoise().maxValue * 4;
        }

        @NotNull ShiftNoise withNewNoise(NormalNoise noise);

    }

    record ShiftA(@NotNull Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise offsetNoise) implements ShiftNoise {
        ShiftA(Holder<NormalNoise.NoiseParameters> noiseData) {
            this(noiseData, null);
        }

        public double compute(Point point) {
            return ShiftNoise.super.compute(new Vec(point.x(), 0, point.z()));
        }

        public ShiftNoise withNewNoise(NormalNoise newNoise) {
            return new ShiftA(this.noiseData, newNoise);
        }
    }

    record ShiftB(@NotNull Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise offsetNoise) implements ShiftNoise {
        ShiftB(Holder<NormalNoise.NoiseParameters> noiseData) {
            this(noiseData, null);
        }

        public double compute(Point point) {
            return ShiftNoise.super.compute(new Vec(point.z(), point.x(), 0));
        }

        public ShiftNoise withNewNoise(NormalNoise newNoise) {
            return new ShiftB(this.noiseData, newNoise);
        }
    }

    record Shift(@NotNull Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise offsetNoise) implements ShiftNoise {
        Shift(Holder<NormalNoise.NoiseParameters> noiseData) {
            this(noiseData, null);
        }

        public ShiftNoise withNewNoise(NormalNoise newNoise) {
            return new Shift(this.noiseData, newNoise);
        }
    }

    record BlendDensity(@NotNull DensityFunction input) implements Transformer {

        public double transform(Point context, double density) {
            return density; // blender not supported
        }

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new BlendDensity(this.input.mapAll(visitor)));
        }

        public double minValue() {
            return -Double.MAX_VALUE;
        }

        public double maxValue() {
            return Double.MAX_VALUE;
        }
    }

    record Clamp(@NotNull DensityFunction input, double min, double max) implements Transformer {

        public double transform(Point point, double density) {
            return Util.clamp(density, this.min, this.max);
        }

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new Clamp(this.input.mapAll(visitor), this.min, this.max));
        }

        public double minValue() {
            return this.min;
        }

        public double maxValue() {
            return this.max;
        }
    }

    String[] MappedType = {"abs", "square", "cube", "half_negative", "quarter_negative", "squeeze"};

    record Mapped(@NotNull String type,
                  @NotNull DensityFunction input,
                  @NotNull DoubleUnaryOperator transformer,
                  @Nullable Double min,
                  @Nullable Double max) implements Transformer {

        private static final Map<String, DoubleUnaryOperator> MappedTypes = Map.of(
                "abs", Math::abs,
                "square", d -> d * d,
                "cube", d -> d * d * d,
                "half_negative", d -> d > 0 ? d : d * 0.5,
                "quarter_negative", d -> d > 0 ? d : d * 0.25,
                "squeeze", d -> {
                    double c = Util.clamp(d, -1, 1);
                    return c / 2 - c * c * c / 24;
                }
        );

        public Mapped(String type, DensityFunction input, @Nullable Double min, @Nullable Double max) {
            this(type, input, Mapped.MappedTypes.get(type), min, max);
        }

        Mapped(String type, DensityFunction input) {
            this(type, input, null, null);
        }

        public double transform(Point point, double density) {
            return this.transformer.applyAsDouble(density);
        }

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new Mapped(this.type, this.input.mapAll(visitor)));
        }

        public double minValue() {
            return this.min == null ? -Double.MAX_VALUE : this.min;
        }

        public double maxValue() {
            return this.max == null ? Double.MAX_VALUE : this.max;
        }

        public Mapped withMinMax() {
            double minInput = this.input.minValue();
            double min = this.transformer.applyAsDouble(minInput);
            double max = this.transformer.applyAsDouble(this.input.maxValue());
            if (this.type.equals("abs") || this.type.equals("square")) {
                max = Math.max(min, max);
                min = Math.max(0, minInput);
            }
            return new Mapped(this.type, this.input, min, max);
        }
    }

    String[] Ap2Type = {"add", "mul", "min", "max"};

    record Ap2(@NotNull String type,
               @NotNull DensityFunction argument1,
               @NotNull DensityFunction argument2,
               @Nullable Double min,
               @Nullable Double max) implements DensityFunction {

        public Ap2(String type, DensityFunction argument1, DensityFunction argument2) {
            this(type, argument1, argument2, null, null);
        }


        public double compute(Point point) {
            double a = this.argument1.compute(point);
            return switch (this.type) {
                case "add" -> a + this.argument2.compute(point);
                case "mul" -> a == 0 ? 0 : a * this.argument2.compute(point);
                case "min" -> a < this.argument2.minValue() ? a : Math.min(a, this.argument2.compute(point));
                case "max" -> a > this.argument2.maxValue() ? a : Math.max(a, this.argument2.compute(point));
                default -> 0;
            };
        }

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new Ap2(this.type, this.argument1.mapAll(visitor), this.argument2.mapAll(visitor)));
        }

        public double minValue() {
            return this.min == null ? -Double.MAX_VALUE : this.min;
        }

        public double maxValue() {
            return this.max == null ? Double.MAX_VALUE : this.max;
        }

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

    record Spline(@NotNull CubicSpline<Point> spline) implements DensityFunction {

        public double compute(Point point) {
            return this.spline.compute(point);
        }

        public DensityFunction mapAll(Visitor visitor) {
            CubicSpline<Point> newCubicSpline = this.spline.mapAll((fn) -> {
                if (fn instanceof DensityFunction densityFunction) {
                    return densityFunction.mapAll(visitor);
                }
                return fn;
            });
            newCubicSpline.calculateMinMax();
            return visitor.map().apply(new Spline(newCubicSpline));
        }

        public double minValue() {
            return this.spline.min();
        }

        public double maxValue() {
            return this.spline.max();
        }
    }

    record YClampedGradient(double fromY, double toY, double fromValue, double toValue) implements DensityFunction {
        public double compute(Point point) {
            return Util.clampedMap(point.y(), this.fromY, this.toY, this.fromValue, this.toValue);
        }

        public double minValue() {
            return Math.min(this.fromValue, this.toValue);
        }

        public double maxValue() {
            return Math.max(this.fromValue, this.toValue);
        }
    }

}
