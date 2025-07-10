package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.utils.DirectionUtils;

public class InverseWorkstationPlacementRule extends BlockPlacementRule {

    public InverseWorkstationPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        Direction direction = DirectionUtils.getHorizontalPlacementDirection(placementState);
        if (direction == null) {
            return placementState.block();
        }

        // Triple rotation to get the inverse direction
        Direction rotated = DirectionUtils.rotateR(DirectionUtils.rotateR(DirectionUtils.rotateR(direction)));

        return placementState.block()
            .withProperty("facing", rotated.name().toLowerCase());
    }
}
