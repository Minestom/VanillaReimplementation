package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.DatapackUtils;
import net.minestom.vanilla.datapack.json.FileLoaded;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.NamespaceTag;
import net.minestom.vanilla.datapack.json.Optional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public sealed interface ProcessorList {

    List<Processor> processors();

    record Inline(List<Processor> processors) implements ProcessorList {
    }

    final class Reference extends FileLoaded<ProcessorList> implements ProcessorList {

        public Reference(String file) {
            super(file);

            Reference self = this;
            DatapackLoader.loading().whenFinished(finisher -> {
                DatapackUtils.findProcessorList(finisher.datapack(), file).ifPresentOrElse(processorList -> {
                    self.value = processorList;
                }, () -> {
                    throw new IllegalStateException("Could not find processor list with filename " + file);
                });
            });
        }

        @Override
        public List<Processor> processors() {
            return get().processors();
        }
    }

    static ProcessorList fromJson(JsonReader reader) throws IOException {
        return JsonUtils.typeMap(reader, token -> switch (token) {
            case BEGIN_OBJECT -> DatapackLoader.moshi(Inline.class);
            case BEGIN_ARRAY -> json -> {
                json.beginArray();
                List<Processor> processors = new ArrayList<>();
                while (json.peek() != JsonReader.Token.END_ARRAY) {
                    processors.add(DatapackLoader.moshi(Processor.class).apply(json));
                }
                json.endArray();
                return new Inline(processors);
            };
            case STRING -> json -> new Reference(json.nextString());
            default -> null;
        });
    }

    sealed interface Processor {

        default NamespaceID processor_type() {
            return JsonUtils.getNamespaceTag(this.getClass());
        }

        static Processor fromJson(JsonReader reader) throws IOException {
            return JsonUtils.sealedUnionNamespace(reader, Processor.class, "processor_type");
        }

        /**
         * Replaces blocks with custom rules
         *
         * @param position_predicate    (Optional, defaults to an "always_true" test) The test to apply to the distance from the structure start to this block.
         * @param input_predicate       The test to apply to the block placed by the structure.
         * @param location_predicate    The test to apply to the block in the world that is replaced by the structure.
         * @param output_state          The block that is placed when all conditions are met. Omitting block states use default values (e.g. the replacement of stairs with stairs without changing states need 40 rules to check for all facing combinations).
         * @param block_entity_modifier (optional) Modifies nbt data of the block entity if all conditions are met.
         */
        @NamespaceTag("rule")
        record Rule(PosRuleTest position_predicate, RuleTest input_predicate, RuleTest location_predicate,
                    BlockState output_state, @Optional BlockEntityModifier block_entity_modifier) implements Processor {
        }

        /**
         * Randomly removes blocks. The removed blocks are not replaced by air, but keep the old blocks before the structure being generated
         *
         * @param integrity       The probability of randomly removing blocks in the structure. Value between 0 and 1.
         * @param rottable_blocks (optional) Blocks that can be removed. A block ID or a block tag, or a list of block IDs.
         */
        @NamespaceTag("block_rot")
        record BlockRot(float integrity, @Optional JsonUtils.SingleOrList<NamespaceID> rottable_blocks) implements Processor {
        }

        /**
         * Makes blocks aged. A stone, stone bricks, or cracked stone bricks block has a chance of 0.5 to be replaced with one of cracked stone bricks, stone brick stairs, mossy stone bricks, and mossy stone brick stairs. All variants of stairs have a 0.5 chance to become one of stone slab, stone brick slab, mossy stone brick stairs, and mossy stone brick slab. All variants of slabs and walls may remain unchanged or become mossy stone brick variants. Obsidian also has a 0.15 chance to be replaced with crying obsidian.
         *
         * @param mossiness The probability of using mossy variants when making a block aged.
         */
        @NamespaceTag("block_age")
        record BlockAge(float mossiness) implements Processor {
        }

        /**
         * Removes specified blocks. The removed blocks are not replaced by air, but keep the old blocks before the structure being generated.
         *
         * @param blocks (Required, but can be empty) IDs of blocks to ignore. Specifying block states has no effect.
         */
        @NamespaceTag("block_ignore")
        record BlockIgnore(List<BlockState> blocks) implements Processor {
        }

        /**
         * Change the Y-level of blocks' positions to fit the terrain like a village road. Note that this is not used to make floating gravity blocks fall down. This processor is hardcoded to be used on a structure template if its "projection" field in its template pool is "terrain_matching"
         *
         * @param heightmap (optional, defaults to WORLD_SURFACE_WG) Must be one of "WORLD_SURFACE_WG"(if not during world generation, fallbacks to WORLD_SURFACE), "WORLD_SURFACE", "OCEAN_FLOOR_WG"(if not during world generation, fallbacks to OCEAN_FLOOR), "OCEAN_FLOOR", "MOTION_BLOCKING", or "MOTION_BLOCKING_NO_LEAVES".
         * @param offset    (optional, defaults to 0) The offset relative to the terrain. For example: 0 is to place the structure on the ground, -1 is to sink one block into the ground. When this processor is used on a structure template by hardcoding (when the template's "projection" field in its template pool is "terrain_matching"),  offset is -1.
         */
        @NamespaceTag("gravity")
        record Gravity(@Optional Heightmap heightmap, @Optional Integer offset) implements Processor {
            public enum Heightmap {
                WORLD_SURFACE_WG, WORLD_SURFACE, OCEAN_FLOOR_WG, OCEAN_FLOOR, MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES
            }
        }

        /**
         * Specifies which blocks in the world cannot be overridden by this structure
         *
         * @param value A block tag with #
         */
        @NamespaceTag("protected_blocks")
        record ProtectedBlocks(String value) implements Processor {
        }

        /**
         * Replaces all stone-variant blocks with blackstone variants and all iron bars with chains.
         */
        @NamespaceTag("blackstone_replace")
        record BlackstoneReplace() implements Processor {
        }

        /**
         * Replaces jigsaw blocks with the specified final state. This processor is hardcoded to be used unless generated in the jigsaw block GUI.
         */
        @NamespaceTag("jigsaw_replacement")
        record JigsawReplacement() implements Processor {
        }

        /**
         * Blocks with incomplete outline shapes cannot override the lava in the world
         */
        @NamespaceTag("lava_submerged_block")
        record LavaSubmergedBlock() implements Processor {
        }

        /**
         * Applies a processor to some random blocks instead of applying it to all blocks.
         *
         * @param value    The number of blocks on which the processor is applied. Must be greater than 0. If it is greater than or equal to the total number of blocks in the structure template, all blocks are processed as if the processer is not capped.
         * @param delegate Another processor object
         */
        @NamespaceTag("capped")
        record Capped(IntProvider value, Processor delegate) implements Processor {
        }

        /**
         * Does nothing
         */
        @NamespaceTag("nop")
        record Nop() implements Processor {
        }
    }
}
