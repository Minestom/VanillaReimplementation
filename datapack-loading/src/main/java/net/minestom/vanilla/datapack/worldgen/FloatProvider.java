package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.NamespaceTag;
import net.minestom.vanilla.datapack.json.Optional;

import java.io.IOException;

public sealed interface FloatProvider {
    default NamespaceID type() {
        return JsonUtils.getNamespaceTag(this.getClass());
    }

    static FloatProvider fromJson(JsonReader reader) throws IOException {
        return JsonUtils.typeMap(reader, token -> switch (token) {
            case NUMBER -> json -> new Constant((float) json.nextDouble());
            case BEGIN_OBJECT -> json -> JsonUtils.sealedUnionNamespace(json, FloatProvider.class, "type");
            default -> null;
        });
    }

    //     value: The constant value to use.
    @NamespaceTag("constant")
    record Constant(float value) implements FloatProvider {
    }

    // Gives a number between two bounds.
    //     min_inclusive: The minimum possible value (inclusive).
    //     max_exclusive: The maximum possible value (exclusive). Must be larger than min_inclusive.
    //
    @NamespaceTag("uniform")
    record Uniform(Value value) implements FloatProvider {
        public record Value(float min_inclusive, float max_exclusive) {}
    }

    // Calculated by clamp(normal(mean, deviation), min, max)
    //
    //     mean: The mean.
    //     deviation: The deviation.
    //     min: The minimum value to clamp to.
    //     max: The maximum value to clamp to. Must be larger than  min.
    @NamespaceTag("clamped_normal")
    record ClampedNormal(Value value) implements FloatProvider {
        public record Value(float mean, float deviation, float min, float max) {}
    }

    //     min: The minimum value.
    //     max: The maximum value. Must be larger than  min.
    //     plateau: The range in the middle of the trapezoid distribution that has a uniform distribution. Must be less than or equal to max - min
    @NamespaceTag("trapezoid")
    record Trapezoid(Value value) implements FloatProvider {
        public record Value(float min, float max, float plateau) {}
    }
}
