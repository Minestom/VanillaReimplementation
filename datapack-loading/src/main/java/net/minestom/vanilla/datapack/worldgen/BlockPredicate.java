package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.NamespaceTag;

import java.io.IOException;
import java.util.List;

public sealed interface BlockPredicate {

    default NamespaceID type() {
        return JsonUtils.getNamespaceTag(this.getClass());
    }

    static BlockPredicate fromJson(JsonReader reader) throws IOException {
        return JsonUtils.sealedUnionNamespace(reader, BlockPredicate.class, "type");
    }

    /**
     * All the specified block predicates need to match
     * @param predicates The child predicates
     */
    @NamespaceTag("all_of")
    record AllOf(List<BlockPredicate> predicates) implements BlockPredicate {
    }

    /**
     * Must matches at least one of the specified block predicates
     * @param predicates The child predicates
     */
    @NamespaceTag("any_of")
    record AnyOf(List<BlockPredicate> predicates) implements BlockPredicate {
    }

    /**
     * Whether the Y level is in the world
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     */
    @NamespaceTag("inside_world_bounds")
    record InsideWorldBounds(IntList offset) implements BlockPredicate {
    }

    /**
     * Material is solid
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     */
    @NamespaceTag("solid")
    record Solid(IntList offset) implements BlockPredicate {
    }

    /**
     * Material is replacable
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     */
    @NamespaceTag("replaceable")
    record Replaceable(IntList offset) implements BlockPredicate {
    }

    /**
     * Checks whether the block at a location has a full block supporting surface in a direction
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     * @param direction The direction of the block to check if it is sturdy. With sturdy is meant that the block's block supporting box is full in the direction.
     */
    @NamespaceTag("has_sturdy_face")
    record HasSturdyFace(IntList offset, String direction) implements BlockPredicate {
    }

    /**
     * Checks that the given tag contains the block at the specified offset
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     * @param tag The block tag without # to check
     */
    @NamespaceTag("matching_block_tag")
    record MatchingBlockTag(IntList offset, String tag) implements BlockPredicate {
    }

    /**
     * Checks that the given list of blocks contains the block at the specified offset
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     * @param blocks The blocks that will match. Cn be a block ID or a block tag, or a list of block IDs
     */
    @NamespaceTag("matching_blocks")
    record MatchingBlocks(IntList offset, JsonUtils.SingleOrList<String> blocks) implements BlockPredicate {
    }

    /**
     * Checks that the given list of fluids contains the fluid at the specified offset
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     * @param fluids The fluids that will match. Cn be a fluid ID or a fluid tag, or a list of fluid IDs
     */
    @NamespaceTag("matching_fluids")
    record MatchingFluids(IntList offset, JsonUtils.SingleOrList<String> fluids) implements BlockPredicate {
    }

    /**
     * Inverts the predicate
     */
    @NamespaceTag("not")
    record Not(BlockPredicate predicate) implements BlockPredicate {
    }

    /**
     * Checks whether this block state can survive in the specified position
     * @param offset A list of 3 integers specifying an [X, Y, Z] block position offset to check
     * @param state The block state to check
     */
    @NamespaceTag("would_survive")
    record WouldSurvive(IntList offset, BlockState state) implements BlockPredicate {
    }
}
