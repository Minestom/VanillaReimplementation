package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.item.DroppedItemFactory;

public class SupportedBelowPlacementRule extends BlockPlacementRule {

    public SupportedBelowPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!placementState.instance().getBlock(placementState.placePosition().add(0.0, -1.0, 0.0))
                .registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return null;
        }
        return placementState.block();
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (!updateState.instance().getBlock(updateState.blockPosition().add(0.0, -1.0, 0.0))
                .registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return updateState.currentBlock();
    }
}
