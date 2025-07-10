package net.minestom.vanilla.blocks.group.block;

import net.minestom.server.instance.block.Block;

import java.util.Collection;
import java.util.Collections;

/**
 *
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 * <p>
 * A block group that contains a single block
 */
public class BlockBlockGroup implements BlockGroup {
    private final Block block;

    /**
     * Creates a new block group for a single block
     * @param block The block to include in this group
     */
    public BlockBlockGroup(Block block) {
        this.block = block;
    }

    @Override
    public Collection<Block> allMatching() {
        return Collections.singletonList(block);
    }
}
