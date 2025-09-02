package net.minestom.vanilla.blocks.placement;

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
public class BellPlacementRule extends BlockPlacementRule {

    public BellPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        BlockFace blockFace = placementState.blockFace();
        if (blockFace == null) return block;

        if (blockFace == BlockFace.BOTTOM) {
            return block.withProperty("attachment", "ceiling");
        } else if (blockFace == BlockFace.TOP) {
            return block
                .withProperty("attachment", "floor")
                .withProperty("facing", DirectionUtils.getNearestHorizontalLookingDirection(placementState).name().toLowerCase());
        } else {
            String direction = blockFace.getOppositeFace().name().toLowerCase();
            boolean doubleWall = placementState.instance().getBlock(
                placementState.placePosition().add(
                    blockFace.toDirection().vec()
                )
            ).isSolid();

            return block
                .withProperty("facing", direction)
                .withProperty("attachment", doubleWall ? "double_wall" : "single_wall");
        }
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        String attachment = updateState.currentBlock().getProperty("attachment");
        if ("ceiling".equals(attachment) && !updateState.instance().getBlock(
                updateState.blockPosition().add(
                    0.0,
                    1.0,
                    0.0
                )
            ).isSolid()) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }

        if ("floor".equals(attachment) && !updateState.instance().getBlock(
                updateState.blockPosition().add(
                    0.0,
                    -1.0,
                    0.0
                )
            ).isSolid()) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }

        BlockFace attachmentDirection = BlockFace.valueOf(updateState.currentBlock().getProperty("facing").toUpperCase());
        Block blockInFront = updateState.instance().getBlock(updateState.blockPosition().add(attachmentDirection.toDirection().vec()));
        Block blockBehind = updateState.instance().getBlock(updateState.blockPosition().add(attachmentDirection.getOppositeFace().toDirection().vec()));

        if (blockInFront.isSolid() && blockBehind.isSolid()) {
            return updateState.currentBlock()
                .withProperty("attachment", "double_wall");
        } else if (blockInFront.isSolid()) {
            return updateState.currentBlock()
                .withProperty("attachment", "single_wall")
                .withProperty("facing", attachmentDirection.name().toLowerCase());
        } else if (blockBehind.isSolid()) {
            return updateState.currentBlock()
                .withProperty("attachment", "single_wall")
                .withProperty("facing", attachmentDirection.getOppositeFace().name().toLowerCase());
        } else {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
    }
}
