package net.minestom.vanilla.blocks.group;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.blocks.group.block.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 * <p>
 * Base class for rule sets that manage block groups and associated functionality
 * @param <GroupImpl> The implementation type for the group
 * @param <Functor> The function type associated with the group
 */
public abstract class VanillaRuleset<GroupImpl, Functor> {
    public final List<GroupImpl> ALL = new ArrayList<>();

    /**
     * Creates and registers a group with a block group and value function
     * @param blockGroup The block group to associate with
     * @param valueFunction The function to associate with the blocks
     * @return The created group implementation
     */
    protected GroupImpl group(BlockGroup blockGroup, Functor valueFunction) {
        GroupImpl result = createGroup(blockGroup, valueFunction);
        ALL.add(result);
        return result;
    }

    /**
     * Combines multiple block groups into a single aggregate group
     * @param blockGroups The block groups to combine
     * @return The combined block group
     */
    protected BlockGroup all(BlockGroup... blockGroups) {
        return new AggregateTagBlockGroup(blockGroups);
    }

    /**
     * Creates a block group based on a tag
     * @param tag The tag name to match
     * @return A block group for the tag
     */
    protected BlockGroup byTag(String tag) {
        return new TagBlockGroup(Key.key(tag));
    }

    /**
     * Creates a block group for a single block
     * @param block The block to match
     * @return A block group for the block
     */
    protected BlockGroup byBlock(Block block) {
        return new BlockBlockGroup(block);
    }

    /**
     * Creates a block group from a collection of blocks
     * @param blocks The blocks to include in the group
     * @return A block group for the blocks
     */
    protected BlockGroup byList(Collection<Block> blocks) {
        return new ListBlockGroup(blocks);
    }

    /**
     * Creates a block group that excludes blocks from another group
     * @param positive The group of blocks to include
     * @param negative The group of blocks to exclude
     * @return A block group with the exclusion applied
     */
    protected BlockGroup byExclusion(BlockGroup positive, BlockGroup negative) {
        return new ExcludeBlockRule(positive, negative);
    }

    /**
     * Abstract method to create a specific group implementation
     * @param blockGroup The block group to associate with
     * @param valueFunction The function to associate with the blocks
     * @return The created group implementation
     */
    protected abstract GroupImpl createGroup(BlockGroup blockGroup, Functor valueFunction);
}
