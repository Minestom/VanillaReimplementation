package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import net.minestom.vanilla.common.tag.BlockTags;

import java.util.Set;

public class CactusPlacementRule extends BlockPlacementRule {
    private final Set<Block> plantableOn;

    public CactusPlacementRule(Block block) {
        super(block);
        this.plantableOn = BlockTags.getInstance().getTaggedWith("minecraft:sand");
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (checkEligibility(placementState.instance(), placementState.placePosition())) {
            return placementState.block();
        }
        return null;
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (checkEligibility(updateState.instance(), updateState.blockPosition())) {
            return updateState.currentBlock();
        }
        DroppedItemFactory.maybeDrop(updateState);
        return Block.AIR;
    }

    public boolean checkEligibility(Block.Getter instance, Point position) {
        Block blockBelow = instance.getBlock(position.sub(0.0, 1.0, 0.0));

        // Check if on sand or another cactus
        boolean validBelow = false;
        if (blockBelow.compare(Block.CACTUS)) {
            validBelow = true;
        } else {
            for (Block validBlock : plantableOn) {
                if (validBlock.compare(blockBelow)) {
                    validBelow = true;
                    break;
                }
            }
        }
        if (!validBelow) return false;

        // Check if no blocks adjacent
        for (Direction direction : Direction.HORIZONTAL) {
            if (!instance.getBlock(position.add(direction.vec())).isAir()) {
                return false;
            }
        }

        return true;
    }
}
