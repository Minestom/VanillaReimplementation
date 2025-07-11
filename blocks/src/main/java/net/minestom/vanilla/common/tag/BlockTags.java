package net.minestom.vanilla.common.tag;

import net.minestom.server.instance.block.Block;

/**
 *
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 * <p>
 * Aggregating tag provider for blocks that combines multiple block tag sources
 */
public class BlockTags extends AggregatingTagProvider<Block> {

    private static final BlockTags INSTANCE = new BlockTags();

    private BlockTags() {
        addChild(BlockRegistryTagProvider.getInstance());
        addChild(MinestomBlockTagProvider.getInstance());
    }

    /**
     * Gets the singleton instance of BlockTags
     * @return The BlockTags instance
     */
    public static BlockTags getInstance() {
        return INSTANCE;
    }
}
