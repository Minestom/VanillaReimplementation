package net.minestom.vanilla.datapack.worldgen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.DatapackLoadingFeature;
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

public interface DensityFunctions {
    interface Visitor {
        Function<DensityFunction, DensityFunction> map();
    }

    static DensityFunction.Context context(double x, double y, double z, Datapack datapack) {
        ContextImpl context = new ContextImpl();
        context.x = x;
        context.y = y;
        context.z = z;
        context.datapack = datapack;
        return context;
    }

    class ContextImpl implements DensityFunction.Context {
        public double x;
        public double y;
        public double z;
        public Datapack datapack;

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

        @Override
        public Datapack datapack() {
            return datapack;
        }
    }

    Function<Object, NormalNoise.NoiseParameters> NOISE_PARSER = NormalNoise.NoiseParameters::fromJson;

    static DensityFunction fromJson(Object obj) {
        if (obj instanceof String str) {
            Double strDouble = Util.parseDouble(str);
            if (strDouble != null) {
                return new Constant(strDouble);
            }
            return new DatapackDefined(NamespaceID.from(str));
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
                return new DatapackDefined(NamespaceID.from(Util.jsonToString(element)));
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


//		const type = Json.readString(root.type)?.replace(/^minecraft:/, '')
        String type;
        {
            var value = root.get("type");
            if (value == null) {
                return Constant.ZERO;
            }
            type = value.getAsString().replaceFirst("minecraft:", "");
        }

//        switch (type) {
        return switch (type) {
            case "blend_alpha" -> new ConstantMinMax(1, 0, 1);
            case "blend_offset", "beardifier" -> new ConstantMinMax(0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            case "old_blended_noise" -> new OldBlendedNoise(
                    root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
                    root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
                    root.get("xz_factor") == null ? 80 : root.get("xz_factor").getAsDouble(),
                    root.get("y_factor") == null ? 160 : root.get("y_factor").getAsDouble(),
                    root.get("smear_scale_multiplier") == null ? 8 : root.get("smear_scale_multiplier").getAsDouble()
            );
            case "flat_cache" -> new FlatCache(DensityFunctions.fromJson(root.get("argument")));
            case "interpolated" -> new Interpolated(DensityFunctions.fromJson(root.get("argument")));
            case "cache_2d" -> new Cache2D(DensityFunctions.fromJson(root.get("argument")));
            case "cache_once" -> new CacheOnce(DensityFunctions.fromJson(root.get("argument")));
            case "cache_all_in_cell" -> new CacheAllInCell(DensityFunctions.fromJson(root.get("argument")));
            case "noise" -> {
                JsonElement noise = root.get("noise");
                yield new Noise(
                        root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
                        root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
                        NOISE_PARSER.apply(noise)
                );
            }
            case "end_islands" -> new EndIslands();
            case "weird_scaled_sampler" -> {
                JsonElement noise = root.get("noise");
                yield new WeirdScaledSampler(
                        DensityFunctions.fromJson(root.get("input")),
                        root.get("rarity_value_mapper").getAsString(),
                        NOISE_PARSER.apply(noise)
                );
            }
            case "shifted_noise" -> {
                JsonElement noise = root.get("noise");
                yield new ShiftedNoise(
                        DensityFunctions.fromJson(root.get("shift_x")),
                        DensityFunctions.fromJson(root.get("shift_y")),
                        DensityFunctions.fromJson(root.get("shift_z")),
                        root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
                        root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
                        NOISE_PARSER.apply(noise),
                        null
                );
            }
            case "range_choice" -> new RangeChoice(
                    DensityFunctions.fromJson(root.get("input")),
                    root.get("min_inclusive") == null ? 0 : root.get("min_inclusive").getAsDouble(),
                    root.get("max_exclusive") == null ? 1 : root.get("max_exclusive").getAsDouble(),
                    DensityFunctions.fromJson(root.get("when_in_range")),
                    DensityFunctions.fromJson(root.get("when_out_of_range"))
            );
            case "shift_a" -> {
                JsonElement noise = root.get("argument");
                yield new ShiftA(NOISE_PARSER.apply(noise));
            }
            case "shift_b" -> {
                JsonElement noise = root.get("argument");
                yield new ShiftB(NOISE_PARSER.apply(noise));
            }
            case "shift" -> {
                JsonElement noise = root.get("argument");
                yield new Shift(NOISE_PARSER.apply(noise));
            }
            case "blend_density" -> new BlendDensity(DensityFunctions.fromJson(root.get("argument")));
            case "clamp" -> new Clamp(
                    DensityFunctions.fromJson(root.get("input")),
                    root.get("min") == null ? 0 : root.get("min").getAsDouble(),
                    root.get("max") == null ? 1 : root.get("max").getAsDouble()
            );
            case "abs", "square", "cube", "half_negative", "quarter_negative", "squeeze" ->
                    new Mapped(type, DensityFunctions.fromJson(root.get("argument")));
            case "add", "mul", "min", "max" -> new Ap2(
                    type,
                    DensityFunctions.fromJson(root.get("argument1")),
                    DensityFunctions.fromJson(root.get("argument2"))
            );
            case "spline" -> new Spline(
                    CubicSpline.fromJson(root.get("spline"), DensityFunctions::fromJson)
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
                this.lastValue = this.wrapped.compute(DensityFunctions.context(quartX << 2, 0, quartZ << 2, context.datapack()));
                this.lastQuartX = quartX;
                this.lastQuartZ = quartZ;
            }
            return this.lastValue;
        }

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new FlatCache(this.wrapped.mapAll(visitor)));
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

        @Override
        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new Interpolated(this.wrapped.mapAll(visitor)));
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

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new Cache2D(this.wrapped.mapAll(visitor)));
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

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new CacheOnce(this.wrapped.mapAll(visitor)));
        }

        @Override
        public DensityFunction wrapped() {
            return wrapped;
        }
    }

    // case "cache_all_in_cell" -> new CacheAllInCell(inputParser.apply(root.get("argument")));
    record CacheAllInCell(DensityFunction wrapped) implements Wrapped {
        public double compute(Context context) {
            return this.wrapped.compute(context);
        }

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new CacheAllInCell(this.wrapped.mapAll(visitor)));
        }
    }

    // case "noise" -> new Noise(
    //                    root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
    //                    root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
    //                    NoiseParser.apply(root.get("noise"))
    //            );
    record Noise(double xzScale, double yScale, NormalNoise.NoiseParameters noiseData, NormalNoise noise) implements DensityFunction {

        public Noise(double xzScale, double yScale, NormalNoise.NoiseParameters noiseData) {
            this(xzScale, yScale, noiseData, null);
        }

        public Noise(double xzScale, double yScale, NormalNoise.NoiseParameters noiseData, @Nullable NormalNoise noise) {
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
        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new WeirdScaledSampler(this.input.mapAll(visitor), this.rarity_value_mapper, this.noiseData));
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

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new ShiftedNoise(this.shiftX.mapAll(visitor), this.shiftY.mapAll(visitor), this.shiftZ.mapAll(visitor), this.xzScale, this.yScale, this.noiseData, this.noise));
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

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new RangeChoice(this.input.mapAll(visitor), this.minInclusive, this.maxExclusive, this.whenInRange.mapAll(visitor), this.whenOutOfRange.mapAll(visitor)));
        }

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
            return super.compute(DensityFunctions.context(context.x(), 0, context.z(), context.datapack()));
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
            return super.compute(DensityFunctions.context(context.z(), context.x(), 0, context.datapack()));
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

    abstract class Transformer implements DensityFunction {
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
    class BlendDensity extends Transformer {
        BlendDensity(DensityFunction input) {
            super(input);
        }

        public double transform(Context context, double density) {
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

    //    public class Clamp extends Transformer {
//        constructor(
//                input: DensityFunction,
//                public readonly min: number,
//                public readonly max: number,
//                ) {
//            super(input)
//        }
//        public transform(context: Context, density: number) {
//            return clamp(density, this.min, this.max)
//        }
//        public mapAll(visitor: Visitor): DensityFunction {
//            return visitor.map(new Clamp(this.input.mapAll(visitor), this.min, this.max))
//        }
//        public minValue() {
//            return this.min
//        }
//        public maxValue() {
//            return this.max
//        }
//    }
    class Clamp extends Transformer {
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

    //	const MappedType = ['abs', 'square', 'cube', 'half_negative', 'quarter_negative', 'squeeze'] as const
    String[] MappedType = {"abs", "square", "cube", "half_negative", "quarter_negative", "squeeze"};

    //    public class Mapped extends Transformer {
//        private static readonly MappedTypes: Record<typeof MappedType[number], (density: number) => number> = {
//            abs: d => Math.abs(d),
//                    square: d => d * d,
//                    cube: d => d * d * d,
//                    half_negative: d => d > 0 ? d : d * 0.5,
//                    quarter_negative: d => d > 0 ? d : d * 0.25,
//                    squeeze: d => {
//				const c = clamp(d, -1, 1)
//                return c / 2 - c * c * c / 24
//            },
//        }
//        private readonly transformer: (density: number) => number
//        constructor(
//                        public readonly type: typeof MappedType[number],
//                        input: DensityFunction,
//                        private readonly min?: number,
//                        private readonly max?: number,
//                        ) {
//            super(input)
//            this.transformer = Mapped.MappedTypes[this.type]
//        }
//        public transform(context: Context, density: number) {
//            return this.transformer(density)
//        }
//        public mapAll(visitor: Visitor): DensityFunction {
//            return visitor.map(new Mapped(this.type, this.input.mapAll(visitor)))
//        }
//        public minValue() {
//            return this.min ?? -Infinity
//        }
//        public maxValue() {
//            return this.max ?? Infinity
//        }
//        public withMinMax() {
//			const minInput = this.input.minValue()
//            let min = this.transformer(minInput)
//            let max = this.transformer(this.input.maxValue())
//            if (this.type === 'abs' || this.type === 'square') {
//                max = Math.max(min, max)
//                min = Math.max(0, minInput)
//            }
//            return new Mapped(this.type, this.input, min, max)
//        }
//    }
    class Mapped extends Transformer {
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
            double min = this.transformer.apply(minInput);
            double max = this.transformer.apply(this.input.maxValue());
            if (this.type.equals("abs") || this.type.equals("square")) {
                max = Math.max(min, max);
                min = Math.max(0, minInput);
            }
            return new Mapped(this.type, this.input, min, max);
        }
    }

    //	const Ap2Type = ['add', 'mul', 'min', 'max'] as const
    String[] Ap2Type = {"add", "mul", "min", "max"};

    //    public class Ap2 implements DensityFunction {
//        constructor(
//                public readonly type: typeof Ap2Type[number],
//                public readonly argument1: DensityFunction,
//                public readonly argument2: DensityFunction,
//                private readonly min?: number,
//                private readonly max?: number,
//                ) {
//            super()
//        }
//        public compute(context: Context) {
//			const a = this.argument1.compute(context)
//            switch (this.type) {
//                case 'add': return a + this.argument2.compute(context)
//                case 'mul': return a === 0 ? 0 : a * this.argument2.compute(context)
//                case 'min': return a < this.argument2.minValue() ? a : Math.min(a, this.argument2.compute(context))
//                case 'max': return a > this.argument2.maxValue() ? a : Math.max(a, this.argument2.compute(context))
//            }
//        }
//        public mapAll(visitor: Visitor) {
//            return visitor.map(new Ap2(this.type, this.argument1.mapAll(visitor), this.argument2.mapAll(visitor)))
//        }
//        public minValue() {
//            return this.min ?? -Infinity
//        }
//        public maxValue() {
//            return this.max ?? Infinity
//        }
//        public withMinMax() {
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
//    }
    class Ap2 implements DensityFunction {
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

        public DensityFunction mapAll(Visitor visitor) {
            return visitor.map().apply(new Ap2(this.type, this.argument1.mapAll(visitor), this.argument2.mapAll(visitor)));
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


    //    public class Spline implements DensityFunction {
//        constructor(
//                public readonly spline: CubicSpline<Context>,
//                ) {
//            super()
//        }
//        public compute(context: Context) {
//            return this.spline.compute(context)
//        }
//        public mapAll(visitor: Visitor): DensityFunction {
//			const newCubicSpline = this.spline.mapAll((fn) => {
//            if (fn instanceof DensityFunction) {
//                return fn.mapAll(visitor)
//            }
//            return fn
//			})
//            newCubicSpline.calculateMinMax()
//            return visitor.map(new Spline(newCubicSpline))
//        }
//        public minValue() {
//            return this.spline.min()
//        }
//        public maxValue() {
//            return this.spline.max()
//        }
//    }
    class Spline implements DensityFunction {
        final CubicSpline<Context> spline;

        Spline(CubicSpline<Context> spline) {
            this.spline = spline;
        }

        public double compute(Context context) {
            return this.spline.compute(context);
        }

        public DensityFunction mapAll(Visitor visitor) {
            CubicSpline<Context> newCubicSpline = this.spline.mapAll((fn) -> {
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

    //    public class YClampedGradient implements DensityFunction {
//        constructor(
//                public readonly fromY: number,
//                public readonly toY: number,
//                public readonly fromValue: number,
//                public readonly toValue: number,
//                ) {
//            super()
//        }
//        public compute(context: Context) {
//            return clampedMap(context.y, this.fromY, this.toY, this.fromValue, this.toValue)
//        }
//        public minValue() {
//            return Math.min(this.fromValue, this.toValue)
//        }
//        public maxValue() {
//            return Math.max(this.fromValue, this.toValue)
//        }
//    }
    class YClampedGradient implements DensityFunction {
        final double fromY;
        final double toY;
        final double fromValue;
        final double toValue;

        public YClampedGradient(double fromY, double toY, double fromValue, double toValue) {
            this.fromY = fromY;
            this.toY = toY;
            this.fromValue = fromValue;
            this.toValue = toValue;
        }

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
