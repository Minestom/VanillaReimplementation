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
public class HopperPlacementRule extends BlockPlacementRule {

    public HopperPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        BlockFace placementFace = placementState.blockFace();
        String facing;

        if (placementFace == BlockFace.BOTTOM || placementFace == BlockFace.TOP) {
            facing = "down";
        } else if (placementFace == BlockFace.NORTH ||
                   placementFace == BlockFace.SOUTH ||
                   placementFace == BlockFace.EAST ||
                   placementFace == BlockFace.WEST) {
            facing = placementFace.getOppositeFace().toDirection().name().toLowerCase();
        } else {
            facing = "down";
        }

        return placementState.block()
            .withProperty("facing", facing)
            .withProperty("enabled", "true");
    }
}
