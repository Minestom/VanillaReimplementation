package net.minestom.vanilla.datapack.worldgen;


import com.squareup.moshi.JsonReader;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.NamespaceTag;

import java.io.IOException;
import java.util.List;

public sealed interface IntProvider {

    default NamespaceID type() {
        return JsonUtils.getNamespaceTag(getClass());
    }

    static IntProvider fromJson(JsonReader reader) throws IOException {
        return JsonUtils.typeMap(reader, token -> switch(token) {
            case NUMBER -> json -> new Constant(json.nextInt());
            case BEGIN_OBJECT -> json -> JsonUtils.sealedUnionNamespace(json, IntProvider.class, "type");
            default -> throw new IOException("Unexpected token " + token);
        });
    }

    /**
     * Provides a constant integer value
     * @param value The constant value to use
     */
    @NamespaceTag("constant")
    record Constant(int value) implements IntProvider {
    }

    /**
     * Gives a number between two bounds.
     */
    @NamespaceTag("uniform")
    record Uniform(Value value) implements IntProvider {
        /**
         * @param min_inclusive The minimum possible value
         * @param max_inclusive The maximum possible value
         */
        public record Value(int min_inclusive, int max_inclusive) {
        }
    }

    /**
     * Gives a number between two bounds. The number will be biased towards min_inclusive.
     */
    @NamespaceTag("biased_to_bottom")
    record BiasedToBottom(Value value) implements IntProvider {
        /**
         * @param min_inclusive The minimum possible value
         * @param max_inclusive The maximum possible value
         */
        public record Value(int min_inclusive, int max_inclusive) {
        }
    }

    /**
     * Clamps a number from another int provider between a lower and upper bound.
     */
    @NamespaceTag("clamped")
    record Clamped(Value value) implements IntProvider {
        /**
         * @param min_inclusive The minimum allowed value that the number will be
         * @param max_inclusive The maximum allowed value that the number will be
         * @param source The source int provider
         */
        public record Value(int min_inclusive, int max_inclusive, IntProvider source) {
        }
    }

    /**
     * Clamps a number from the specific normal distribution.
     */
    @NamespaceTag("clamped_normal")
    record ClampedNormal(Value value) implements IntProvider {
        /**
         * @param mean The mean value of the normal distribution
         * @param deviation The deviation of the normal distribution
         * @param min_inclusive The minimum allowed value that the number will be
         * @param max_inclusive The maximum allowed value that the number will be
         */
        public record Value(float mean, float deviation, int min_inclusive, int max_inclusive) {
        }
    }

    /**
     * A random pool of int providers.
     */
    @NamespaceTag("weighted_list")
    record WeightedList(List<Entry> distribution) implements IntProvider {
        /**
         * One entry in the random pool
         * @param provider An int provider
         * @param weight The weight of this entry
         */
        public record Entry(IntProvider provider, int weight) {
        }
    }
}
