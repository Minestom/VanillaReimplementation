package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.item.DroppedItemFactory;

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
    public Block blockUpdate(UpdateState updateState) {
        BlockFace facing = BlockFace.valueOf(updateState.currentBlock().getProperty("facing").toUpperCase());
        var supportingBlockPos = updateState.blockPosition().add(facing.getOppositeFace().toDirection().vec());

        if (!updateState.instance().getBlock(supportingBlockPos).isSolid()) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return updateState.currentBlock();
    }
}
