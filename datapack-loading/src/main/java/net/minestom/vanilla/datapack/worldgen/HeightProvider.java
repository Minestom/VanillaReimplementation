package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.NamespaceTag;
import net.minestom.vanilla.datapack.json.Optional;

import java.io.IOException;
import java.util.List;

public sealed interface HeightProvider {

    default NamespaceID type() {
        return JsonUtils.getNamespaceTag(this.getClass());
    }

    static HeightProvider fromJson(JsonReader reader) throws IOException {
        try (var json = reader.peekJson()) {
            json.beginObject();
            if (JsonUtils.findProperty(json, "type", JsonReader::nextString) == null) {
                return new Constant(VerticalAnchor.fromJson(reader));
            }
        }
        return JsonUtils.sealedUnionNamespace(reader, HeightProvider.class, "type");
    }

    // value: The vertical anchor to use as constant height.
    @NamespaceTag("constant")
    record Constant(VerticalAnchor value) implements HeightProvider {
    }

    //  min_inclusive: The vertical anchor to use as minimum height.
    //  max_inclusive: The vertical anchor to use as maximum height.
    @NamespaceTag("uniform")
    record Uniform(VerticalAnchor min_inclusive, VerticalAnchor max_inclusive) implements HeightProvider {
    }

    //  min_inclusive: The vertical anchor to use as minimum height.
    //  max_inclusive: The vertical anchor to use as maximum height.
    //  inner: (optional, defaults to 1) The inner value. Must be at least 1.
    @NamespaceTag("biased_to_bottom")
    record BiasedToBottom(VerticalAnchor min_inclusive, VerticalAnchor max_inclusive, @Optional Integer inner) implements HeightProvider {
    }

    // min_inclusive: The vertical anchor to use as minimum height.
    // max_inclusive: The vertical anchor to use as maximum height.
    // inner: (optional, defaults to 1) The inner value. Must be at least 1.
    @NamespaceTag("very_biased_to_bottom")
    record VeryBiasedToBottom(VerticalAnchor min_inclusive, VerticalAnchor max_inclusive, @Optional Integer inner) implements HeightProvider {
    }

    // min_inclusive: The vertical anchor to use as minimum height.
    // max_inclusive: The vertical anchor to use as maximum height.
    // plateau: (optional, defaults to 0) The length of the range in the middle of the trapezoid distribution that has a uniform distribution.
    @NamespaceTag("trapezoid")
    record Trapezoid(VerticalAnchor min_inclusive, VerticalAnchor max_inclusive, @Optional Integer plateau) implements HeightProvider {
    }

    //  distribution: (Cannot be empty) A random weighted pool of height providers.
    @NamespaceTag("weighted_list")
    record WeightedList(List<Entry> distribution) implements HeightProvider {

        // data: A height provider.
        // weight: The weight of this entry.
        public record Entry(HeightProvider provider, int weight) {
        }
    }
}
