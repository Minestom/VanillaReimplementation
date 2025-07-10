package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.item.DroppedItemFactory;

public class CropPlacementRule extends BlockPlacementRule {

    public CropPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        Block blockBelow = placementState.instance().getBlock(placementState.placePosition().add(0.0, -1.0, 0.0));
        if (!blockBelow.compare(Block.FARMLAND)) {
            return null;
        }
        return placementState.block();
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        Block blockBelow = updateState.instance().getBlock(updateState.blockPosition().add(0.0, -1.0, 0.0));
        if (!blockBelow.compare(Block.FARMLAND)) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return updateState.currentBlock();
    }
}
