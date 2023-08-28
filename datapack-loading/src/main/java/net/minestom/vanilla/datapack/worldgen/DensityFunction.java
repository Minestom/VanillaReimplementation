package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;

import java.io.IOException;
import java.util.Map;

public interface DensityFunction extends DensityFunctions /*NumberFunction<DensityFunction.Context>*/ {
    double compute(Context context);

    default double minValue() {
        return -maxValue();
    }

    double maxValue();

    static DensityFunction fromJson(JsonReader reader) throws IOException {
        return JsonUtils.unionNamespaceStringType(reader, "type", Map.ofEntries(

                Map.entry("minecraft:blend_alpha", DatapackLoader.moshi(BlendAlpha.class)),
                Map.entry("minecraft:blend_offset", DatapackLoader.moshi(BlendOffset.class)),
                Map.entry("minecraft:beardifier", DatapackLoader.moshi(Beardifier.class)),
                Map.entry("minecraft:old_blended_noise", DatapackLoader.moshi(OldBlendedNoise.class)),
                Map.entry("minecraft:flat_cache", DatapackLoader.moshi(FlatCache.class)),
                Map.entry("minecraft:interpolated", DatapackLoader.moshi(Interpolated.class)),
                Map.entry("minecraft:cache_2d", DatapackLoader.moshi(Cache2d.class)),
                Map.entry("minecraft:cache_once", DatapackLoader.moshi(CacheOnce.class)),
                Map.entry("minecraft:cache_all_in_cell", DatapackLoader.moshi(CacheAllInCell.class)),
                Map.entry("minecraft:noise", DatapackLoader.moshi(Noise.class)),
                Map.entry("minecraft:end_islands", DatapackLoader.moshi(EndIslands.class)),
                Map.entry("minecraft:weird_scaled_sampler", DatapackLoader.moshi(WeirdScaledSampler.class)),
                Map.entry("minecraft:shifted_noise", DatapackLoader.moshi(ShiftedNoise.class)),
                Map.entry("minecraft:range_choice", DatapackLoader.moshi(RangeChoice.class)),
                Map.entry("minecraft:shift_a", DatapackLoader.moshi(ShiftA.class)),
                Map.entry("minecraft:shift_b", DatapackLoader.moshi(ShiftB.class)),
                Map.entry("minecraft:blend_density", DatapackLoader.moshi(BlendDensity.class)),
                Map.entry("minecraft:clamp", DatapackLoader.moshi(Clamp.class)),
                Map.entry("minecraft:abs", DatapackLoader.moshi(Abs.class)),
                Map.entry("minecraft:square", DatapackLoader.moshi(Square.class)),
                Map.entry("minecraft:cube", DatapackLoader.moshi(Cube.class)),
                Map.entry("minecraft:half_negative", DatapackLoader.moshi(HalfNegative.class)),
                Map.entry("minecraft:quarter_negative", DatapackLoader.moshi(QuarterNegative.class)),
                Map.entry("minecraft:squeeze", DatapackLoader.moshi(Squeeze.class)),
                Map.entry("minecraft:add", DatapackLoader.moshi(Add.class)),
                Map.entry("minecraft:mul", DatapackLoader.moshi(Mul.class)),
                Map.entry("minecraft:min", DatapackLoader.moshi(Min.class)),
                Map.entry("minecraft:max", DatapackLoader.moshi(Max.class)),
                Map.entry("minecraft:spline", DatapackLoader.moshi(Spline.class)),
                Map.entry("minecraft:constant", DatapackLoader.moshi(Constant.class)),
                Map.entry("minecraft:y_clamped_gradient", DatapackLoader.moshi(YClampedGradient.class))
        ));
    }

    default DensityFunction mapAll(Visitor visitor) {
        return visitor.map().apply(this);
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
