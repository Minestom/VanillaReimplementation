package net.minestom.vanilla.blocks.group.block;

import net.minestom.server.instance.block.Block;
import java.util.Collection;

/**
 *
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 * <p>
 * Interface for a group of blocks with common characteristics
 */
public interface BlockGroup extends IntoBlockGroup {
    /**
     * Gets all blocks that match this group
     * @return Collection of matching blocks
     */
    Collection<Block> allMatching();

    /**
     * Gets the block group (self-reference for IntoBlockGroup implementation)
     * @return This block group
     */
    @Override
    default BlockGroup getBlockGroup() {
        return this;
    }
}
