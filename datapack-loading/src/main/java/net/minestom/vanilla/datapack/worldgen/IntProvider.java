package net.minestom.vanilla.datapack.worldgen;


import net.minestom.server.utils.NamespaceID;

import java.util.List;

public interface IntProvider {

    NamespaceID type();

    /**
     * Provides a constant integer value
     * @param value The constant value to use
     */
    record Constant(int value) implements IntProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:constant");
        }
    }

    /**
     * Gives a number between two bounds.
     * @param min_inclusive The minimum possible value
     * @param max_inclusive The maximum possible value
     */
    record Uniform(int min_inclusive, int max_inclusive) implements IntProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:uniform");
        }
    }

    /**
     * Gives a number between two bounds. The number will be biased towards min_inclusive.
     * @param min_inclusive The minimum possible value
     * @param max_inclusive The maximum possible value
     */
    record BiasedToBottom(int min_inclusive, int max_inclusive) implements IntProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:biased_to_bottom");
        }
    }

    /**
     * Clamps a number from another int provider between a lower and upper bound.
     * @param min_inclusive The minimum allowed value that the number will be
     * @param max_inclusive The maximum allowed value that the number will be
     * @param source The source int provider
     */
    record Clamped(int min_inclusive, int max_inclusive, IntProvider source) implements IntProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:clamped");
        }
    }

    /**
     * Clamps a number from the specific normal distribution.
     * @param mean The mean value of the normal distribution
     * @param deviation The deviation of the normal distribution
     * @param min_inclusive The minimum allowed value that the number will be
     * @param max_inclusive The maximum allowed value that the number will be
     */
    record ClampedNormal(int mean, int deviation, int min_inclusive, int max_inclusive) implements IntProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:clamped_normal");
        }
    }

    /**
     * A random pool of int providers.
     */
    record WeightedList(List<Entry> distribution) implements IntProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:weighted_list");
        }

        /**
         * One entry in the random pool
         * @param provider An int provider
         * @param weight The weight of this entry
         */
        public record Entry(IntProvider provider, int weight) {
        }
    }
}
