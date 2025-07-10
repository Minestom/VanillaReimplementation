package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.utils.FluidUtils;

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
