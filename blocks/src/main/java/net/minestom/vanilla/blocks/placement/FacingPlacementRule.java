package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.utils.DirectionUtils;

public class FacingPlacementRule extends BlockPlacementRule {

    public FacingPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (placementState.playerPosition() == null) {
            return placementState.block();
        }

        return placementState.block()
            .withProperty("facing", DirectionUtils.getNearestLookingDirection(placementState).name().toLowerCase());
    }
}
