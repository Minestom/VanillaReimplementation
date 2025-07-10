package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import net.minestom.vanilla.common.utils.DirectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class LeverPlacementRule extends BlockPlacementRule {

    public LeverPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        BlockFace clickedFace = placementState.blockFace();
        if (clickedFace == null) {
            return null;
        }

        if (!DirectionUtils.canAttach(placementState)) {
            return null;
        }

        Block newBlock = block.withProperty("powered", "false");

        if (clickedFace == BlockFace.NORTH || clickedFace == BlockFace.SOUTH ||
            clickedFace == BlockFace.EAST || clickedFace == BlockFace.WEST) {
            return newBlock
                .withProperty("face", "wall")
                .withProperty("facing", clickedFace.name().toLowerCase(Locale.ROOT));
        } else if (clickedFace == BlockFace.TOP) {
            Direction playerFacing = DirectionUtils.getNearestHorizontalLookingDirection(placementState).opposite();
            return newBlock
                .withProperty("face", "floor")
                .withProperty("facing", playerFacing.name().toLowerCase(Locale.ROOT));
        } else if (clickedFace == BlockFace.BOTTOM) {
            Direction playerFacing = DirectionUtils.getNearestHorizontalLookingDirection(placementState).opposite();
            return newBlock
                .withProperty("face", "ceiling")
                .withProperty("facing", playerFacing.name().toLowerCase(Locale.ROOT));
        }

        return null;
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        Block currentBlock = updateState.currentBlock();
        String face = currentBlock.getProperty("face");
        String facing = currentBlock.getProperty("facing");

        if (face == null || facing == null) {
            return Block.AIR;
        }

        BlockFace supportDirection;

        switch (face) {
            case "floor":
                supportDirection = BlockFace.BOTTOM;
                break;
            case "ceiling":
                supportDirection = BlockFace.TOP;
                break;
            case "wall":
                supportDirection = BlockFace.valueOf(facing.toUpperCase(Locale.ROOT)).getOppositeFace();
                break;
            default:
                return Block.AIR;
        }

        var supportBlockPosition = updateState.blockPosition().relative(supportDirection);
        var supportBlock = updateState.instance().getBlock(supportBlockPosition);
        var attachedFace = supportDirection.getOppositeFace();

        if (!supportBlock.registry().collisionShape().isFaceFull(attachedFace)) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }

        return updateState.currentBlock();
    }
}
