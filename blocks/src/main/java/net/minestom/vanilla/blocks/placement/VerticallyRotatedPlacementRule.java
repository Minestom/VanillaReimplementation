package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.utils.DirectionUtils;

public class VerticallyRotatedPlacementRule extends BlockPlacementRule {

    public VerticallyRotatedPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        Direction direction = DirectionUtils.getHorizontalPlacementDirection(placementState);
        if (direction == null) {
            return placementState.block();
        }
        return placementState.block()
                .withProperty("facing", direction.name().toLowerCase());
    }
}

