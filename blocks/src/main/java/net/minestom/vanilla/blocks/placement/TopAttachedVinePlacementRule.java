package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.item.DroppedItemFactory;

public class TopAttachedVinePlacementRule extends BlockPlacementRule {

    public TopAttachedVinePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!validatePosition(placementState.instance(), placementState.placePosition())) {
            return null;
        }
        return placementState.block();
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (!validatePosition(updateState.instance(), updateState.blockPosition())) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return updateState.currentBlock();
    }

    public boolean validatePosition(Block.Getter instance, Point position) {
        Block above = instance.getBlock(position.add(0.0, 1.0, 0.0));

        if (above.registry().collisionShape().isFaceFull(BlockFace.BOTTOM)) {
            return true;
        }
        if (above.compare(block)) {
            return true;
        }
        if (above.key().value().substring(0, 3).equals(block.key().value().substring(0, 3))) {
            return true;
        }
        return false;
    }
}
