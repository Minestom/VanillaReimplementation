package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import org.jetbrains.annotations.NotNull;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class LadderPlacementRule extends BlockPlacementRule {

    public LadderPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        BlockFace blockFace = placementState.blockFace();
        if (blockFace == null) {
            return null;
        }

        var supporting = placementState.placePosition().add(blockFace.getOppositeFace().toDirection().vec());
        if (!placementState.instance().getBlock(supporting).registry().collisionShape().isFaceFull(blockFace)) {
            return null;
        }

        return block.withProperty("facing", blockFace.name().toLowerCase());
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        BlockFace facing = BlockFace.valueOf(updateState.currentBlock().getProperty("facing").toUpperCase());
        var supportingBlockPos = updateState.blockPosition().add(facing.getOppositeFace().toDirection().vec());

        if (!updateState.instance().getBlock(supportingBlockPos).isSolid()) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return updateState.currentBlock();
    }
}
