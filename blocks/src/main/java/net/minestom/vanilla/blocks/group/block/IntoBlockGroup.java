package net.minestom.vanilla.blocks.group.block;

/**
 *
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 * <p>
 * Interface for objects that can provide a block group
 */
public interface IntoBlockGroup {
    /**
     * Gets the block group associated with this object
     * @return The block group
     */
    BlockGroup getBlockGroup();
}
