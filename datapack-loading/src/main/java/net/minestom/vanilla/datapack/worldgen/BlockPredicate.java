package net.minestom.vanilla.datapack.worldgen;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.json.JsonUtils;

import java.util.List;

public interface BlockPredicate {
    NamespaceID type();

    /**
     * All the specified block predicates need to match
     * @param predicates The child predicates
     */
    record AllOf(List<BlockPredicate> predicates) implements BlockPredicate {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:all_of");
        }
    }

    /**
     * Must matches at least one of the specified block predicates
     * @param predicates The child predicates
     */
    record AnyOf(List<BlockPredicate> predicates) implements BlockPredicate {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:any_of");
        }
    }

    /**
     * Whether the Y level is in the world
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     */
    record InsideWorldBounds(IntList offset) implements BlockPredicate {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:inside_world_bounds");
        }
    }

    /**
     * Material is solid
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     */
    record Solid(IntList offset) implements BlockPredicate {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:solid");
        }
    }

    /**
     * Material is replacable
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     */
    record Replaceable(IntList offset) implements BlockPredicate {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:replaceable");
        }
    }

    /**
     * Checks whether the block at a location has a full block supporting surface in a direction
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     * @param direction The direction of the block to check if it is sturdy. With sturdy is meant that the block's block supporting box is full in the direction.
     */
    record HasSturdyFace(IntList offset, String direction) implements BlockPredicate {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:has_sturdy_face");
        }
    }

    /**
     * Checks that the given tag contains the block at the specified offset
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     * @param tag The block tag without # to check
     */
    record MatchingBlockTag(IntList offset, String tag) implements BlockPredicate {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:matching_block_tag");
        }
    }

    /**
     * Checks that the given list of blocks contains the block at the specified offset
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     * @param blocks The blocks that will match. Cn be a block ID or a block tag, or a list of block IDs
     */
    record MatchingBlocks(IntList offset, JsonUtils.SingleOrList<String> blocks) implements BlockPredicate {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:matching_blocks");
        }
    }

    /**
     * Checks that the given list of fluids contains the fluid at the specified offset
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     * @param fluids The fluids that will match. Cn be a fluid ID or a fluid tag, or a list of fluid IDs
     */
    record MatchingFluids(IntList offset, JsonUtils.SingleOrList<String> fluids) implements BlockPredicate {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:matching_fluids");
        }
    }

    /**
     * Inverts the predicate
     */
    record Not(BlockPredicate predicate) implements BlockPredicate {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:not");
        }
    }

    /**
     * Checks whether this block state can survive in the specified position
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     * @param state The block state to check
     */
    record WouldSurvive(IntList offset, BlockState state) implements BlockPredicate {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("minecraft:would_survive");
        }
    }
}
