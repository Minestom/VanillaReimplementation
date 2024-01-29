package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.Optional;

import java.io.IOException;
import java.util.List;

public sealed interface HeightProvider {

    NamespaceID type();

    static HeightProvider fromJson(JsonReader reader) throws IOException {
        try (var json = reader.peekJson()) {
            json.beginObject();
            if (JsonUtils.findProperty(json, "type", JsonReader::nextString) == null) {
                return new Constant(VerticalAnchor.fromJson(reader));
            }
        }
        return JsonUtils.unionStringTypeAdapted(reader, "type", type -> switch (type) {
            case "minecraft:constant" -> Constant.class;
            case "minecraft:uniform" -> Uniform.class;
            case "minecraft:biased_to_bottom" -> BiasedToBottom.class;
            case "minecraft:very_biased_to_bottom" -> VeryBiasedToBottom.class;
            case "minecraft:biased_to_top" -> BiasedToTop.class;
            case "minecraft:weighted_list" -> WeightedList.class;
            default -> null;
        });
    }

    // value: The vertical anchor to use as constant height.
    record Constant(VerticalAnchor value) implements HeightProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:constant");
        }
    }

    //  min_inclusive: The vertical anchor to use as minimum height.
    //  max_inclusive: The vertical anchor to use as maximum height.
    record Uniform(VerticalAnchor min_inclusive, VerticalAnchor max_inclusive) implements HeightProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:uniform");
        }
    }

    //  min_inclusive: The vertical anchor to use as minimum height.
    //  max_inclusive: The vertical anchor to use as maximum height.
    //  inner: (optional, defaults to 1) The inner value. Must be at least 1.
    record BiasedToBottom(VerticalAnchor min_inclusive, VerticalAnchor max_inclusive, @Optional Integer inner) implements HeightProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:biased_to_bottom");
        }
    }

    // min_inclusive: The vertical anchor to use as minimum height.
    // max_inclusive: The vertical anchor to use as maximum height.
    // inner: (optional, defaults to 1) The inner value. Must be at least 1.
    record VeryBiasedToBottom(VerticalAnchor min_inclusive, VerticalAnchor max_inclusive, @Optional Integer inner) implements HeightProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:very_biased_to_bottom");
        }
    }

    // min_inclusive: The vertical anchor to use as minimum height.
    // max_inclusive: The vertical anchor to use as maximum height.
    // plateau: (optional, defaults to 0) The length of the range in the middle of the trapezoid distribution that has a uniform distribution.
    record BiasedToTop(VerticalAnchor min_inclusive, VerticalAnchor max_inclusive, @Optional Integer plateau) implements HeightProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:biased_to_top");
        }
    }

    //  distribution: (Cannot be empty) A random weighted pool of height providers.
    record WeightedList(List<Entry> distribution) implements HeightProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:weighted_list");
        }

        // data: A height provider.
        // weight: The weight of this entry.
        public record Entry(HeightProvider provider, int weight) {
        }
    }
}
