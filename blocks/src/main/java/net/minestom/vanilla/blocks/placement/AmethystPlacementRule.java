package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.utils.DirectionUtils;
import net.minestom.vanilla.common.utils.FluidUtils;

public class AmethystPlacementRule extends BlockPlacementRule {

    public AmethystPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!DirectionUtils.canAttach(placementState)) return null;

        Block currentBlock = placementState.instance().getBlock(placementState.placePosition());
        boolean waterlogged = FluidUtils.isWater(currentBlock);

        return placementState.block()
            .withProperty("waterlogged", String.valueOf(waterlogged))
            .withProperty("facing", placementState.blockFace().toDirection().name().toLowerCase());
    }
}
