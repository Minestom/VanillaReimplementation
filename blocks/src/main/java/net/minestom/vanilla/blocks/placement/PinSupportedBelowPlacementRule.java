package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.item.DroppedItemFactory;

public class PinSupportedBelowPlacementRule extends BlockPlacementRule {

    public PinSupportedBelowPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!isSupported(placementState.instance(), placementState.placePosition())) {
            return null;
        }
        return placementState.block();
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (!isSupported(updateState.instance(), updateState.blockPosition())) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return updateState.currentBlock();
    }

    protected boolean isSupported(Block.Getter instance, Point block) {
        Block below = instance.getBlock(block.sub(0.0, 1.0, 0.0));
        return below.isSolid();
    }
}
