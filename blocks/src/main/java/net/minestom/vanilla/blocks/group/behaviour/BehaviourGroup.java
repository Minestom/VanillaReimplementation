package net.minestom.vanilla.blocks.group.behaviour;

import java.util.function.Function;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.vanilla.blocks.group.block.BlockGroup;
import net.minestom.vanilla.blocks.group.block.IntoBlockGroup;

/**
 * A group that pairs a block group with a function to create block handlers
 */
public class BehaviourGroup implements IntoBlockGroup {
    private final BlockGroup blockGroup;
    private final Function<Block, BlockHandler> valueFunction;

    /**
     * Creates a new behaviour group
     * @param blockGroup The group of blocks to apply behavior to
     * @param valueFunction The function to create handlers for blocks
     */
    public BehaviourGroup(BlockGroup blockGroup, Function<Block, BlockHandler> valueFunction) {
        this.blockGroup = blockGroup;
        this.valueFunction = valueFunction;
    }

    /**
     * Gets the block group this behavior applies to
     * @return The block group
     */
    @Override
    public BlockGroup getBlockGroup() {
        return blockGroup;
    }

    /**
     * Creates a handler for the given block
     * @param block The block to create a handler for
     * @return The block handler
     */
    public BlockHandler createHandler(Block block) {
        return valueFunction.apply(block);
    }
}
