package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
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
