package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

public class RotatedPillarPlacementRule extends BlockPlacementRule {

    public RotatedPillarPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        BlockFace blockFace = placementState.blockFace();
        if (blockFace == null) return placementState.block();

        String axis;
        if (blockFace == BlockFace.TOP || blockFace == BlockFace.BOTTOM) {
            axis = "y";
        } else if (blockFace == BlockFace.EAST || blockFace == BlockFace.WEST) {
            axis = "x";
        } else {
            axis = "z";
        }

        return placementState.block().withProperty("axis", axis);
    }
}
