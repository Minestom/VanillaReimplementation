package net.minestom.vanilla.blocks.group.block;

import net.minestom.server.instance.block.Block;

import java.util.Collection;
import java.util.Collections;

/**
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
