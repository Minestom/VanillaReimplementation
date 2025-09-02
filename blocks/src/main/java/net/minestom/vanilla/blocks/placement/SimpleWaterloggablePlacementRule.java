package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.utils.FluidUtils;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class SimpleWaterloggablePlacementRule extends BlockPlacementRule {

    public SimpleWaterloggablePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        boolean isWater = FluidUtils.isWater(
            placementState.instance().getBlock(placementState.placePosition())
        );

        return placementState.block().withProperty("waterlogged", String.valueOf(isWater));
    }
}
