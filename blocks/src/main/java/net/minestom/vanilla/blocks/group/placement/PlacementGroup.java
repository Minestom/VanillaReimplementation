package net.minestom.vanilla.blocks.group.placement;

import java.util.function.Function;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.blocks.group.block.BlockGroup;
import net.minestom.vanilla.blocks.group.block.IntoBlockGroup;

/**
 *
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 * <p>
 * A group that pairs a block group with a function to create block placement rules
 */
public class PlacementGroup implements IntoBlockGroup, IntoPlacementRule {
    private final BlockGroup blockGroup;
    private final Function<Block, BlockPlacementRule> valueFunction;

    /**
     * Creates a new placement group
     * @param blockGroup The group of blocks to apply rules to
     * @param valueFunction The function to create rules for blocks
     */
    public PlacementGroup(BlockGroup blockGroup, Function<Block, BlockPlacementRule> valueFunction) {
        this.blockGroup = blockGroup;
        this.valueFunction = valueFunction;
    }

    @Override
    public BlockGroup getBlockGroup() {
        return blockGroup;
    }

    @Override
    public BlockPlacementRule createRule(Block block) {
        return valueFunction.apply(block);
    }
}
