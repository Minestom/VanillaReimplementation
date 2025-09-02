package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.blocks.placement.util.States;

import java.util.Locale;
import java.util.Map;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class TrapdoorPlacementRule extends BlockPlacementRule {

    public TrapdoorPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        BlockFace placementFace = placementState.blockFace();
        Pos playerPos = placementState.playerPosition() != null ? placementState.playerPosition() : Pos.ZERO;
        var direction = BlockFace.fromYaw(playerPos.yaw()).toDirection().opposite();
        Vec cursorPos = placementState.cursorPosition() != null ? (Vec) placementState.cursorPosition() : Vec.ZERO;
        BlockFace facing = BlockFace.fromDirection(direction);

        BlockFace half;
        if (placementFace == BlockFace.BOTTOM ||
                (placementFace != BlockFace.TOP && cursorPos.y() > 0.5)) {
            half = BlockFace.TOP;
        } else {
            half = BlockFace.BOTTOM;
        }

        return placementState.block().withProperties(
                Map.of(
                        States.HALF, half.name().toLowerCase(Locale.getDefault()),
                        States.FACING, facing.name().toLowerCase(Locale.getDefault())
                )
        );
    }
}

