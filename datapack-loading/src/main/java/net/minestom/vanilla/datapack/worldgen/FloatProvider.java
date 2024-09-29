package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.kyori.adventure.key.Key;
import net.minestom.vanilla.datapack.json.JsonUtils;

import java.io.IOException;

public interface FloatProvider {
    Key type();

    static FloatProvider fromJson(JsonReader reader) throws IOException {
        return JsonUtils.<FloatProvider>typeMap(reader, token -> switch (token) {
            case NUMBER -> json -> new Constant((float) json.nextDouble());
            case BEGIN_OBJECT -> json -> JsonUtils.unionStringTypeAdapted(json, "type", type -> switch (type) {
                case "minecraft:constant" -> Constant.class;
                case "minecraft:uniform" -> Uniform.class;
                case "minecraft:clamped_normal" -> ClampedNormal.class;
                case "minecraft:trapezoid" -> Trapezoid.class;
                default -> null;
            });
            default -> null;
        });
    }

    //     value: The constant value to use.
    record Constant(float value) implements FloatProvider {
        @Override
        public Key type() {
            return Key.key("minecraft:constant");
        }
    }

    // Gives a number between two bounds.
    //     min_inclusive: The minimum possible value (inclusive).
    //     max_exclusive: The maximum possible value (exclusive). Must be larger than min_inclusive.
    //
    record Uniform(Value value) implements FloatProvider {
        public record Value(float min_inclusive, float max_exclusive) {}

        @Override
        public Key type() {
            return Key.key("minecraft:uniform");
        }
    }

    // Calculated by clamp(normal(mean, deviation), min, max)
    //
    //     mean: The mean.
    //     deviation: The deviation.
    //     min: The minimum value to clamp to.
    //     max: The maximum value to clamp to. Must be larger than  min.
    record ClampedNormal(Value value) implements FloatProvider {
        public record Value(float mean, float deviation, float min, float max) {}

        @Override
        public Key type() {
            return Key.key("minecraft:clamped_normal");
        }
    }

    //     min: The minimum value.
    //     max: The maximum value. Must be larger than  min.
    //     plateau: The range in the middle of the trapezoid distribution that has a uniform distribution. Must be less than or equal to max - min
    record Trapezoid(Value value) implements FloatProvider {
        public record Value(float min, float max, float plateau) {}

        @Override
        public Key type() {
            return Key.key("minecraft:trapezoid");
        }
    }
}
