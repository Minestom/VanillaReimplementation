package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.utils.DirectionUtils;

public class GlazedTerracottaPlacementRule extends BlockPlacementRule {

    public GlazedTerracottaPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        var horizontalLookingDirection = DirectionUtils.getNearestHorizontalLookingDirection(placementState);
        return block.withProperty("facing", horizontalLookingDirection.name().toLowerCase());
    }
}
