package net.minestom.vanilla.generation.densityfunctions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.Holder;
import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.WorldgenRegistries;
import net.minestom.vanilla.generation.math.CubicSpline;
import net.minestom.vanilla.generation.math.NumberFunction;

import java.util.function.Function;

public interface DensityFunction extends DensityFunctions, NumberFunction<DensityFunction.Context> {

    /**
     * Creates a new immutable context from the given coords
     * @param x the x coord
     * @param y the y coord
     * @param z the z coord
     * @return a new immutable context
     */
    static Context context(double x, double y, double z) {
        return new ContextImpl(x, y, z);
    }

    /**
     * Computes the density function value at the given context
     * @param context the context
     * @return the density function value
     */
    double compute(Context context);

    /**
     * Returns the minimum possible value of this density function
     * @return the minimum possible value
     */
    default double minValue() {
        return -maxValue();
    }

    /**
     * Returns the maximum possible value of this density function
     * @return the maximum possible value
     */
    double maxValue();

    /**
     * Maps this density function to a new density function using the given visitor
     * @param mapper the mapper
     * @return the new density function
     */
    default DensityFunction mapAll(Mapper mapper) {
        return mapper.map(this);
    }

    interface Context {
        double x();

        default int blockX() {
            return (int) Math.floor(x());
        }

        double y();

        default int blockY() {
            return (int) Math.floor(y());
        }

        double z();

        default int blockZ() {
            return (int) Math.floor(z());
        }
    }

    /**
     * A density function mapper
     */
    interface Mapper {
        /**
         * Maps the given density function to a new density function
         * @param function the density function
         * @return the new density function
         */
        DensityFunction map(DensityFunction function);
    }

    static DensityFunction fromJson(Object obj) {
        return fromJson(obj, null);
    }

    static DensityFunction fromJson(Object obj, Function<Object, DensityFunction> inputParser) {
        if (inputParser == null) {
            inputParser = DensityFunction::fromJson;
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
                return new HolderHolder(Holder.reference(WorldgenRegistries.DENSITY_FUNCTION,
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
//            case 'blend_alpha': return new ConstantMinMax(1, 0, 1)
            case "blend_alpha" -> new ConstantMinMax(1, 0, 1);
//            case 'blend_offset': return new ConstantMinMax(0, -Infinity, Infinity)
            case "blend_offset" -> new ConstantMinMax(0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
//            case 'beardifier': return new ConstantMinMax(0, -Infinity, Infinity)
            case "beardifier" -> new ConstantMinMax(0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
//            case 'old_blended_noise': return new OldBlendedNoise(
//                    Json.readNumber(root.xz_scale) ?? 1,
//            Json.readNumber(root.y_scale) ?? 1,
//                    Json.readNumber(root.xz_factor) ?? 80,
//                    Json.readNumber(root.y_factor) ?? 160,
//                    Json.readNumber(root.smear_scale_multiplier) ?? 8
//			)
            case "old_blended_noise" -> new OldBlendedNoise(
                    root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
                    root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
                    root.get("xz_factor") == null ? 80 : root.get("xz_factor").getAsDouble(),
                    root.get("y_factor") == null ? 160 : root.get("y_factor").getAsDouble(),
                    root.get("smear_scale_multiplier") == null ? 8 : root.get("smear_scale_multiplier").getAsDouble()
            );
//            case 'flat_cache': return new FlatCache(inputParser(root.argument))
            case "flat_cache" -> new FlatCache(inputParser.apply(root.get("argument")));
//            case 'interpolated': return new Interpolated(inputParser(root.argument))
            case "interpolated" -> new Interpolated(inputParser.apply(root.get("argument")));
//            case 'cache_2d': return new Cache2D(inputParser(root.argument))
            case "cache_2d" -> new Cache2D(inputParser.apply(root.get("argument")));
//            case 'cache_once': return new CacheOnce(inputParser(root.argument))
            case "cache_once" -> new CacheOnce(inputParser.apply(root.get("argument")));
//            case 'cache_all_in_cell': return new CacheAllInCell(inputParser(root.argument))
            case "cache_all_in_cell" -> new CacheAllInCell(inputParser.apply(root.get("argument")));
//            case 'noise': return new Noise(
//                    Json.readNumber(root.xz_scale) ?? 1,
//            Json.readNumber(root.y_scale) ?? 1,
//                    NoiseParser(root.noise),
//			)
            case "noise" -> new Noise(
                    root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
                    root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
                    NoiseParser.apply(root.get("noise"))
            );
//            case 'end_islands': return new EndIslands()
            case "end_islands" -> new EndIslands();
//            case 'weird_scaled_sampler': return new WeirdScaledSampler(
//                    inputParser(root.input),
//                    Json.readEnum(root.rarity_value_mapper, RarityValueMapper),
//                    NoiseParser(root.noise),
//                    )
            case "weird_scaled_sampler" -> new WeirdScaledSampler(
                    inputParser.apply(root.get("input")),
                    root.get("rarity_value_mapper").getAsString(),
                    NoiseParser.apply(root.get("noise"))
            );
//            case 'shifted_noise': return new ShiftedNoise(
//                    inputParser(root.shift_x),
//                    inputParser(root.shift_y),
//                    inputParser(root.shift_z),
//                    Json.readNumber(root.xz_scale) ?? 1,
//            Json.readNumber(root.y_scale) ?? 1,
//                    NoiseParser(root.noise),
//			)
            case "shifted_noise" -> new ShiftedNoise(
                    inputParser.apply(root.get("shift_x")),
                    inputParser.apply(root.get("shift_y")),
                    inputParser.apply(root.get("shift_z")),
                    root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
                    root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
                    NoiseParser.apply(root.get("noise")),
                    null
            );
//            case 'range_choice': return new RangeChoice(
//                    inputParser(root.input),
//                    Json.readNumber(root.min_inclusive) ?? 0,
//            Json.readNumber(root.max_exclusive) ?? 1,
//                    inputParser(root.when_in_range),
//                    inputParser(root.when_out_of_range),
//			)
            case "range_choice" -> new RangeChoice(
                    inputParser.apply(root.get("input")),
                    root.get("min_inclusive") == null ? 0 : root.get("min_inclusive").getAsDouble(),
                    root.get("max_exclusive") == null ? 1 : root.get("max_exclusive").getAsDouble(),
                    inputParser.apply(root.get("when_in_range")),
                    inputParser.apply(root.get("when_out_of_range"))
            );
//            case 'shift_a': return new ShiftA(NoiseParser(root.argument))
            case "shift_a" -> new ShiftA(NoiseParser.apply(root.get("argument")));
//            case 'shift_b': return new ShiftB(NoiseParser(root.argument))
            case "shift_b" -> new ShiftB(NoiseParser.apply(root.get("argument")));
//            case 'shift': return new Shift(NoiseParser(root.argument))
//            case 'blend_density': return new BlendDensity(inputParser(root.argument))
            case "blend_density" -> new BlendDensity(inputParser.apply(root.get("argument")));
//            case 'clamp': return new Clamp(
//                    inputParser(root.input),
//                    Json.readNumber(root.min) ?? 0,
//            Json.readNumber(root.max) ?? 1,
//			)
            case "clamp" -> new Clamp(
                    inputParser.apply(root.get("input")),
                    root.get("min") == null ? 0 : root.get("min").getAsDouble(),
                    root.get("max") == null ? 1 : root.get("max").getAsDouble()
            );
//            case 'abs':
//            case 'square':
//            case 'cube':
//            case 'half_negative':
//            case 'quarter_negative':
//            case 'squeeze':
//                return new Mapped(type, inputParser(root.argument))
            case "abs", "square", "cube", "half_negative", "quarter_negative", "squeeze" ->
                    new Mapped(type, inputParser.apply(root.get("argument")));
//            case 'add':
//            case 'mul':
//            case 'min':
//            case 'max': return new Ap2(
//                    Json.readEnum(type, Ap2Type),
//                    inputParser(root.argument1),
//                    inputParser(root.argument2),
//                    )
            case "add", "mul", "min", "max" -> new Ap2(
                    type,
                    inputParser.apply(root.get("argument1")),
                    inputParser.apply(root.get("argument2"))
            );
//            case 'spline': return new Spline(
//                    CubicSpline.fromJson(root.spline, inputParser)
//            )
            case "spline" -> new Spline(
                    CubicSpline.fromJson(root.get("spline"), inputParser::apply)
            );
//            case 'constant': return new Constant(Json.readNumber(root.argument) ?? 0)
            case "constant" -> new Constant(root.get("argument") == null ? 0 : root.get("argument").getAsDouble());
//            case 'y_clamped_gradient': return new YClampedGradient(
//                    Json.readInt(root.from_y) ?? -4064,
//            Json.readInt(root.to_y) ?? 4062,
//                    Json.readNumber(root.from_value) ?? -4064,
//                    Json.readNumber(root.to_value) ?? 4062,
//			)
            case "y_clamped_gradient" -> new YClampedGradient(
                    root.get("from_y") == null ? -4064 : root.get("from_y").getAsInt(),
                    root.get("to_y") == null ? 4062 : root.get("to_y").getAsInt(),
                    root.get("from_value") == null ? -4064 : root.get("from_value").getAsDouble(),
                    root.get("to_value") == null ? 4062 : root.get("to_value").getAsDouble()
            );
//          return Constant.ZERO
            default -> Constant.ZERO;
        };
    }
}
