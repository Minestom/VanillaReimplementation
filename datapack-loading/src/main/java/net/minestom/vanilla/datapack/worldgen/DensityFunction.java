package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.worldgen.math.NumberFunction;

import java.io.IOException;

public interface DensityFunction extends DensityFunctions, NumberFunction<DensityFunction.Context> {
    double compute(Context context);

    default double minValue() {
        return -maxValue();
    }

    double maxValue();

    static DensityFunction fromJson(JsonReader reader) throws IOException {
        return JsonUtils.typeMap(reader, token -> switch (token) {
            case NUMBER -> json -> new Constant(json.nextDouble());
            case STRING -> json -> new LazyLoadedDensityFunction(json.nextString(), DatapackLoader.loading());
            case BEGIN_OBJECT -> json -> JsonUtils.unionStringTypeAdapted(json, "type", type -> switch (type) {
                case "minecraft:blend_alpha" -> BlendAlpha.class;
                case "minecraft:blend_offset" -> BlendOffset.class;
                case "minecraft:beardifier" -> Beardifier.class;
                case "minecraft:old_blended_noise" -> OldBlendedNoise.class;
                case "minecraft:flat_cache" -> FlatCache.class;
                case "minecraft:interpolated" -> Interpolated.class;
                case "minecraft:cache_2d" -> Cache2D.class;
                case "minecraft:cache_once" -> CacheOnce.class;
                case "minecraft:cache_all_in_cell" -> CacheAllInCell.class;
                case "minecraft:noise" -> NoiseRoot.class;
                case "minecraft:end_islands" -> EndIslands.class;
                case "minecraft:weird_scaled_sampler" -> WeirdScaledSampler.class;
                case "minecraft:shifted_noise" -> ShiftedNoise.class;
                case "minecraft:range_choice" -> RangeChoice.class;
                case "minecraft:shift_a" -> ShiftA.class;
                case "minecraft:shift_b" -> ShiftB.class;
                case "minecraft:shift" -> Shift.class;
                case "minecraft:blend_density" -> BlendDensity.class;
                case "minecraft:clamp" -> Clamp.class;
                case "minecraft:abs" -> Abs.class;
                case "minecraft:square" -> Square.class;
                case "minecraft:cube" -> Cube.class;
                case "minecraft:half_negative" -> HalfNegative.class;
                case "minecraft:quarter_negative" -> QuarterNegative.class;
                case "minecraft:squeeze" -> Squeeze.class;
                case "minecraft:add" -> Add.class;
                case "minecraft:mul" -> Mul.class;
                case "minecraft:min" -> Min.class;
                case "minecraft:max" -> Max.class;
                case "minecraft:spline" -> Spline.class;
                case "minecraft:constant" -> Constant.class;
                case "minecraft:y_clamped_gradient" -> YClampedGradient.class;
                default -> null;
            });
            default -> null;
        });
    }

    static DensityFunction.Context context(double x, double y, double z) {
        ContextImpl context = new ContextImpl();
        context.x = x;
        context.y = y;
        context.z = z;
        return context;
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
}
