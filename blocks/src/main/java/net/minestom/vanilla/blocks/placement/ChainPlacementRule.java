package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public class ChainPlacementRule extends BlockPlacementRule {

    public ChainPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        BlockFace placementFace = placementState.blockFace();
        String axis;

        if (placementFace == BlockFace.TOP || placementFace == BlockFace.BOTTOM) {
            axis = "y";
        } else if (placementFace == BlockFace.NORTH || placementFace == BlockFace.SOUTH) {
            axis = "z";
        } else if (placementFace == BlockFace.EAST || placementFace == BlockFace.WEST) {
            axis = "x";
        } else {
            axis = "y";
        }

        return placementState.block().withProperty("axis", axis);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        return updateState.currentBlock();
    }
}
