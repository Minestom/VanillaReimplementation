package net.minestom.vanilla.common.tag;

import net.minestom.server.instance.block.Block;

/**
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
