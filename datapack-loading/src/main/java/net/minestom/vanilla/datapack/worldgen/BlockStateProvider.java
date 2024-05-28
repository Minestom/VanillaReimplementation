package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.NamespaceTag;
import net.minestom.vanilla.datapack.worldgen.noise.Noise;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public sealed interface BlockStateProvider {
    default NamespaceID type() {
        return JsonUtils.getNamespaceTag(getClass());
    }

    static BlockStateProvider fromJson(JsonReader reader) throws IOException {
        return JsonUtils.sealedUnionNamespace(reader, BlockStateProvider.class, "type");
    }

    /**
     * specifies a block state directly
     * @param state The block state to use.
     */
    @NamespaceTag("simple_state_provider")
    record SimpleStateProvider(BlockState state) implements BlockStateProvider {
    }

    /**
     * rotates axially-rotated block, such as logs, chain
     * @param state
     */
    @NamespaceTag("rotated_block_provider")
    record RotatedBlockProvider(BlockState state) implements BlockStateProvider {
    }

    /**
     * chooses a block state from a weighted list
     * @param entries A weighted list of block state entries.
     */
    @NamespaceTag("weighted_state_provider")
    record WeightedStateProvider(List<WeightedBlockStateEntry> entries) implements BlockStateProvider {

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
    @NamespaceTag("randomized_int_state_provider")
    record RandomizedIntStateProvider(String property, IntProvider values, BlockStateProvider source) implements BlockStateProvider {
    }

    /**
     * randomly choose a block state according to a noise value
     * @param seed The seed of the noise.
     * @param noise A noise.
     * @param scale Horizontal scale of the noise. Must be a positive value.
     * @param states List of optional block states.
     */
    @NamespaceTag("noise_provider")
    record NoiseProvider(long seed, Noise noise, float scale, List<BlockState> states) implements BlockStateProvider {
    }

    /**
     * randomly choose a block state according to two noise values
     * @param seed The seed of the noise.
     * @param noise A noise.
     * @param scale Horizontal scale of noise. Must be a positive value.
     * @param slow_noise The noise used for the first selection.
     * @param slow_scale Horizontal scale of the slow noise. Must be a positive value.
     * @param variety The number of block states that selected out by slow noise.
     * @param states List of block states to choose from.
     */
    @NamespaceTag("dual_noise_provider")
    record DualNoiseProvider(long seed, Noise noise, float scale, Noise slow_noise, float slow_scale, Variety variety, List<BlockState> states) implements BlockStateProvider {

        public sealed interface Variety {
            int minInclusive();
            int maxInclusive();

            static Variety fromJson(JsonReader reader) throws IOException {
                return JsonUtils.<Variety>typeMapMapped(reader, Map.of(
                        JsonReader.Token.BEGIN_OBJECT, DatapackLoader.moshi(Object.class),
                        JsonReader.Token.BEGIN_ARRAY, json -> new Array(DatapackLoader.moshi(IntList.class).apply(json)),
                        JsonReader.Token.NUMBER, json -> new Single(json.nextInt())
                ));
            }

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
     * @param high_chance Value between -1.0 and 1.0 (inclusive). If the noise value is higher than the threshold, the block states in high_states will be selected with a probability of high_chance.
     * @param default_state Default block state. These block states is used when the noise value is higher than threshold but high_states is not selected according to high_chance.
     * @param low_states List of block states to choose from when lower than threshold.
     * @param high_states List of block states to choose from when higher than threshold.
     */
    @NamespaceTag("noise_threshold_provider")
    record NoiseThresholdProvider(long seed, Noise noise, float scale, float threshold, float high_chance, BlockState default_state, List<BlockState> low_states, List<BlockState> high_states) implements BlockStateProvider {
    }
}
