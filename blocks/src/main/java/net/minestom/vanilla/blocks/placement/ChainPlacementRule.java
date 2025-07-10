package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
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
    public @NotNull Block blockUpdate(UpdateState updateState) {
        return updateState.currentBlock();
    }
}
