package net.minestom.vanilla.datapack.worldgen;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.worldgen.noise.Noise;

import java.util.List;

public interface BlockStateProvider {
    NamespaceID type();


    /**
     * specifies a block state directly
     * @param state The block state to use.
     */
    record SimpleStateProvider(BlockState state) implements BlockStateProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("simple_state_provider");
        }
    }

    /**
     * rotates axially-rotated block, such as logs, chain
     * @param state
     */
    record RotatedBlockProvider(BlockState state) implements BlockStateProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("rotated_block_provider");
        }
    }

    /**
     * chooses a block state from a weighted list
     * @param entries A weighted list of block state entries.
     */
    record WeightedStateProvider(List<WeightedBlockStateEntry> entries) implements BlockStateProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("weighted_state_provider");
        }

        /**
         * A block state and its corresponding weight.
         * @param data The block state to use.
         * @param weight The weight of this entry.
         */
        public record WeightedBlockStateEntry(BlockState data, int weight) {
        }
    }

    /**
     * assigns a random value to an integer block property
     * @param property The name of a block property.
     * @param values The value of the block property.
     * @param source Another block state provider that specifies the source of the block state.
     */
    record RandomizedIntStateProvider(String property, IntProvider values, BlockStateProvider source) implements BlockStateProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("randomized_int_state_provider");
        }
    }

    /**
     * randomly choose a block state according to a noise value
     * @param seed The seed of the noise.
     * @param noise A noise.
     * @param scale Horizontal scale of the noise. Must be a positive value.
     * @param states List of optional block states.
     */
    record NoiseProvider(long seed, Noise noise, float scale, List<BlockState> states) implements BlockStateProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("noise_provider");
        }
    }

    /**
     * randomly choose a block state according to two noise values
     * @param seed The seed of the noise.
     * @param noise A noise.
     * @param scale Horizontal scale of noise. Must be a positive value.
     * @param slowNoise The noise used for the first selection.
     * @param slowScale Horizontal scale of the slow noise. Must be a positive value.
     * @param variety The number of block states that selected out by slow noise.
     * @param states List of block states to choose from.
     */
    record DualNoiseProvider(long seed, Noise noise, float scale, Noise slowNoise, float slowScale, Variety variety, List<BlockState> states) implements BlockStateProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("dual_noise_provider");
        }

        interface Variety {
            int minInclusive();
            int maxInclusive();

            record Object(int minInclusive, int maxInclusive) implements Variety {
            }

            record Array(IntList list) implements Variety {
                @Override
                public int minInclusive() {
                    return list.getInt(0);
                }

                @Override
                public int maxInclusive() {
                    return list.getInt(1);
                }
            }

            record Single(int value) implements Variety {
                @Override
                public int minInclusive() {
                    return value;
                }

                @Override
                public int maxInclusive() {
                    return value;
                }
            }
        }
    }

    /**
     * use different block state when a noise value above or below the threshold
     * @param seed The seed of the noise.
     * @param noise A noise.
     * @param scale Horizontal scale of noise. Must be a positive value.
     * @param threshold Value between -1.0 and 1.0 (inclusive). The threshold of the noise value. If the noise value is lower than this value, the block states in low_states will be selected.
     * @param highChance Value between -1.0 and 1.0 (inclusive). If the noise value is higher than the threshold, the block states in high_states will be selected with a probability of high_chance.
     * @param defaultState Default block state. These block states is used when the noise value is higher than threshold but high_states is not selected according to high_chance.
     * @param lowStates List of block states to choose from when lower than threshold.
     * @param highStates List of block states to choose from when higher than threshold.
     */
    record NoiseThresholdProvider(long seed, Noise noise, float scale, float threshold, float highChance, BlockState defaultState, List<BlockState> lowStates, List<BlockState> highStates) implements BlockStateProvider {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("noise_threshold_provider");
        }
    }
}
