package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import net.minestom.vanilla.common.utils.DirectionUtils;
import org.jetbrains.annotations.NotNull;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class FacedFacingPlacementRule extends BlockPlacementRule {

    public FacedFacingPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        BlockFace blockFace = placementState.blockFace();
        if (blockFace == null) return null;

        String face;
        BlockFace facing;

        if (blockFace == BlockFace.TOP) {
            face = "floor";
            facing = BlockFace.fromDirection(DirectionUtils.getNearestHorizontalLookingDirection(placementState).opposite());
        } else if (blockFace == BlockFace.BOTTOM) {
            face = "ceiling";
            facing = BlockFace.fromDirection(DirectionUtils.getNearestHorizontalLookingDirection(placementState).opposite());
        } else {
            face = "wall";
            facing = blockFace;
        }

        Point supporting = getSupportingBlockPosition(face, facing, placementState.placePosition());
        if (needSupport() && !placementState.instance().getBlock(supporting).isSolid()) {
            return null;
        }

        return block
            .withProperty("facing", facing.name().toLowerCase())
            .withProperty("face", face);
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        String face = updateState.currentBlock().getProperty("face");
        BlockFace facing = BlockFace.valueOf(updateState.currentBlock().getProperty("facing").toUpperCase());
        Point supportingBlockPos = getSupportingBlockPosition(face, facing, updateState.blockPosition());

        if (needSupport() && !updateState.instance().getBlock(supportingBlockPos).isSolid()) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }

        return updateState.currentBlock();
    }

    public Point getSupportingBlockPosition(String face, BlockFace facing, Point blockPosition) {
        if ("ceiling".equals(face)) {
            return blockPosition.add(0.0, 1.0, 0.0);
        } else if ("floor".equals(face)) {
            return blockPosition.sub(0.0, 1.0, 0.0);
        } else {
            return blockPosition.add(facing.getOppositeFace().toDirection().vec());
        }
    }

    public boolean needSupport() {
        return true;
    }
}
