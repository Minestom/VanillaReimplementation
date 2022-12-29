package net.minestom.vanilla.generation.densityfunctions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.Holder;
import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.WorldgenRegistries;
import net.minestom.vanilla.generation.densityfunctions.NativeDensityFunctions.*;
import net.minestom.vanilla.generation.math.CubicSpline;
import net.minestom.vanilla.generation.math.NumberFunction;
import net.minestom.vanilla.generation.noise.BlendedNoise;
import net.minestom.vanilla.generation.noise.NoiseSettings;
import net.minestom.vanilla.generation.noise.NormalNoise;
import net.minestom.vanilla.generation.random.WorldgenRandom;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class DensityFunctionsUtil {
    static DensityFunction fromJson(Object obj) {

        Function<Object, Holder<NormalNoise.NoiseParameters>> NoiseParser = Holder.parser(WorldgenRegistries.NOISE, NormalNoise.NoiseParameters::fromJson);

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
            case "blend_offset", "beardifier" ->
                    new ConstantMinMax(0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            case "old_blended_noise" -> new OldBlendedNoise(
                    root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
                    root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
                    root.get("xz_factor") == null ? 80 : root.get("xz_factor").getAsDouble(),
                    root.get("y_factor") == null ? 160 : root.get("y_factor").getAsDouble(),
                    root.get("smear_scale_multiplier") == null ? 8 : root.get("smear_scale_multiplier").getAsDouble()
            );
            case "flat_cache" -> new FlatCache(fromJson(root.get("argument")));
            case "interpolated" -> new Interpolated(fromJson(root.get("argument")));
            case "cache_2d" -> new Cache2D(fromJson(root.get("argument")));
            case "cache_once" -> new CacheOnce(fromJson(root.get("argument")));
            case "cache_all_in_cell" -> new CacheAllInCell(fromJson(root.get("argument")));
            case "noise" -> new Noise(
                    root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
                    root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
                    NoiseParser.apply(root.get("noise"))
            );
            case "end_islands" -> new EndIslands();
            case "weird_scaled_sampler" -> new WeirdScaledSampler(
                    fromJson(root.get("input")),
                    root.get("rarity_value_mapper").getAsString(),
                    NoiseParser.apply(root.get("noise"))
            );
            case "shifted_noise" -> new ShiftedNoise(
                    fromJson(root.get("shift_x")),
                    fromJson(root.get("shift_y")),
                    fromJson(root.get("shift_z")),
                    root.get("xz_scale") == null ? 1 : root.get("xz_scale").getAsDouble(),
                    root.get("y_scale") == null ? 1 : root.get("y_scale").getAsDouble(),
                    NoiseParser.apply(root.get("noise")),
                    null
            );
            case "range_choice" -> new RangeChoice(
                    fromJson(root.get("input")),
                    root.get("min_inclusive") == null ? 0 : root.get("min_inclusive").getAsDouble(),
                    root.get("max_exclusive") == null ? 1 : root.get("max_exclusive").getAsDouble(),
                    fromJson(root.get("when_in_range")),
                    fromJson(root.get("when_out_of_range"))
            );
            case "shift_a" -> new ShiftA(NoiseParser.apply(root.get("argument")));
            case "shift_b" -> new ShiftB(NoiseParser.apply(root.get("argument")));
            case "blend_density" -> new BlendDensity(fromJson(root.get("argument")));
            case "clamp" -> new Clamp(
                    fromJson(root.get("input")),
                    root.get("min") == null ? 0 : root.get("min").getAsDouble(),
                    root.get("max") == null ? 1 : root.get("max").getAsDouble()
            );
            case "abs", "square", "cube", "half_negative", "quarter_negative", "squeeze" ->
                    new Mapped(type, fromJson(root.get("argument")));
            case "add", "mul", "min", "max" -> new Ap2(
                    type,
                    fromJson(root.get("argument1")),
                    fromJson(root.get("argument2"))
            );
            case "spline" -> new Spline(CubicSpline.fromJson(root.get("spline"), DensityFunctionsUtil::fromJson));
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

    static DensityFunction.Mapper createMapper(long seed, NoiseSettings noiseSettings,
                                               Map<String, DensityFunction> mapped,
                                               Function<Holder<NormalNoise.NoiseParameters>, NormalNoise> getNoise,
                                               WorldgenRandom.Positional random) {
        return new DensityFunction.Mapper() {
            @Override
            public DensityFunction map(NumberFunction<DensityFunction.Context> fn1) {
                if (fn1 instanceof HolderHolder holderHolder) {
                    NamespaceID key = holderHolder.holder().key();
                    if (key != null && mapped.containsKey(key.toString())) {
                        return Objects.requireNonNull(mapped.get(key.toString()));
                    } else {
                        DensityFunction value = holderHolder.holder().value().mapAll(this);
                        if (key != null) {
                            mapped.put(key.toString(), value);
                        }
                        return value;
                    }
                }

                if (fn1 instanceof Interpolated interpolated) {
                    return interpolated.withCellSize(NoiseSettings.cellWidth(noiseSettings), NoiseSettings.cellHeight(noiseSettings));
                }

                if (fn1 instanceof ShiftedNoise shiftedNoise) {
                    return new ShiftedNoise(shiftedNoise.shiftX(), shiftedNoise.shiftY(), shiftedNoise.shiftZ(), shiftedNoise.xzScale(), shiftedNoise.yScale(), shiftedNoise.noiseData(), getNoise.apply(shiftedNoise.noiseData()));
                }

                if (fn1 instanceof Noise noise) {
                    return new Noise(noise.xzScale(), noise.yScale(), noise.noiseData(), getNoise.apply(noise.noiseData()));
                }

                if (fn1 instanceof ShiftNoise shiftNoise) {
                    return shiftNoise.withNewNoise(getNoise.apply(shiftNoise.noiseData()));
                }

                if (fn1 instanceof WeirdScaledSampler weirdScaledSampler) {
                    return new WeirdScaledSampler(weirdScaledSampler.input(), weirdScaledSampler.rarityValueMapper(), weirdScaledSampler.noiseData(), getNoise.apply(weirdScaledSampler.noiseData()));
                }

                if (fn1 instanceof OldBlendedNoise oldBlendedNoise) {
                    return new OldBlendedNoise(oldBlendedNoise.xzScale(),
                            oldBlendedNoise.yScale(), oldBlendedNoise.xzFactor(), oldBlendedNoise.yFactor(),
                            oldBlendedNoise.smearScaleMultiplier(),
                            new BlendedNoise(random.fromHashOf(NamespaceID.from("terrain").toString()),
                                    oldBlendedNoise.xzScale(), oldBlendedNoise.yScale(), oldBlendedNoise.xzFactor(),
                                    oldBlendedNoise.yFactor(), oldBlendedNoise.smearScaleMultiplier()));
                }

                if (fn1 instanceof EndIslands endIslands) {
                    return new EndIslands(seed);
                }

                if (fn1 instanceof Mapped mapped) {
                    return mapped.withMinMax();
                }

                if (fn1 instanceof Ap2 ap2) {
                    return ap2.withMinMax();
                }
                return (DensityFunction) fn1;
            }
        };
    }

    record ContextImpl(double x, double y, double z) implements DensityFunction.Context {
    }
}
