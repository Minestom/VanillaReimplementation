package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.vanilla.datapack.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface FloatProvider {
    Key type();

    @NotNull Codec<FloatProvider> CODEC = makeCodec();

    private static StructCodec<FloatProvider> makeCodec() {
        return Codec.RegistryTaggedUnion(registries -> {
            class Holder {
                static final @NotNull DynamicRegistry<StructCodec<? extends FloatProvider>> CODEC = createDefaultRegistry();
            }
            return Holder.CODEC;
        }, FloatProvider::codec, "type").orElse(Codec.FLOAT.transform(Constant::new, Constant::value));
    }

    private static DynamicRegistry<StructCodec<? extends FloatProvider>> createDefaultRegistry() {
        var registry = DynamicRegistry.<StructCodec<? extends FloatProvider>>create("float_provider");
        
        registry.register(Key.key("minecraft:constant"), Constant.CODEC);
        registry.register(Key.key("minecraft:uniform"), Uniform.CODEC);
        registry.register(Key.key("minecraft:clamped_normal"), ClampedNormal.CODEC);
        registry.register(Key.key("minecraft:trapezoid"), Trapezoid.CODEC);
        
        return registry;
    }

    @NotNull StructCodec<? extends FloatProvider> codec();

    // Legacy fromJson method for backward compatibility during transition
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
        public static final @NotNull StructCodec<Constant> CODEC = StructCodec.struct(
                "value", Codec.FLOAT, Constant::value,
                Constant::new
        );

        @Override
        public Key type() {
            return Key.key("minecraft:constant");
        }

        @Override
        public @NotNull StructCodec<? extends FloatProvider> codec() {
            return CODEC;
        }
    }

    // Gives a number between two bounds.
    //     min_inclusive: The minimum possible value (inclusive).
    //     max_exclusive: The maximum possible value (exclusive). Must be larger than min_inclusive.
    //
    record Uniform(Value value) implements FloatProvider {
        public record Value(float min_inclusive, float max_exclusive) {
            public static final @NotNull StructCodec<Value> CODEC = StructCodec.struct(
                    "min_inclusive", Codec.FLOAT, Value::min_inclusive,
                    "max_exclusive", Codec.FLOAT, Value::max_exclusive,
                    Value::new
            );
        }

        public static final @NotNull StructCodec<Uniform> CODEC = StructCodec.struct(
                "value", Value.CODEC, Uniform::value,
                Uniform::new
        );

        @Override
        public Key type() {
            return Key.key("minecraft:uniform");
        }

        @Override
        public @NotNull StructCodec<? extends FloatProvider> codec() {
            return CODEC;
        }
    }

    // Calculated by clamp(normal(mean, deviation), min, max)
    //
    //     mean: The mean.
    //     deviation: The deviation.
    //     min: The minimum value to clamp to.
    //     max: The maximum value to clamp to. Must be larger than  min.
    record ClampedNormal(Value value) implements FloatProvider {
        public record Value(float mean, float deviation, float min, float max) {
            public static final @NotNull StructCodec<Value> CODEC = StructCodec.struct(
                    "mean", Codec.FLOAT, Value::mean,
                    "deviation", Codec.FLOAT, Value::deviation,
                    "min", Codec.FLOAT, Value::min,
                    "max", Codec.FLOAT, Value::max,
                    Value::new
            );
        }

        public static final @NotNull StructCodec<ClampedNormal> CODEC = StructCodec.struct(
                "value", Value.CODEC, ClampedNormal::value,
                ClampedNormal::new
        );

        @Override
        public Key type() {
            return Key.key("minecraft:clamped_normal");
        }

        @Override
        public @NotNull StructCodec<? extends FloatProvider> codec() {
            return CODEC;
        }
    }

    //     min: The minimum value.
    //     max: The maximum value. Must be larger than  min.
    //     plateau: The range in the middle of the trapezoid distribution that has a uniform distribution. Must be less than or equal to max - min
    record Trapezoid(Value value) implements FloatProvider {
        public record Value(float min, float max, float plateau) {
            public static final @NotNull StructCodec<Value> CODEC = StructCodec.struct(
                    "min", Codec.FLOAT, Value::min,
                    "max", Codec.FLOAT, Value::max,
                    "plateau", Codec.FLOAT, Value::plateau,
                    Value::new
            );
        }

        public static final @NotNull StructCodec<Trapezoid> CODEC = StructCodec.struct(
                "value", Value.CODEC, Trapezoid::value,
                Trapezoid::new
        );

        @Override
        public Key type() {
            return Key.key("minecraft:trapezoid");
        }

        @Override
        public @NotNull StructCodec<? extends FloatProvider> codec() {
            return CODEC;
        }
    }
}
