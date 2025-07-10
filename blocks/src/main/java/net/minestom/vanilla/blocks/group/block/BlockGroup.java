package net.minestom.vanilla.blocks.group.block;

import net.minestom.server.instance.block.Block;
import java.util.Collection;

/**
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
