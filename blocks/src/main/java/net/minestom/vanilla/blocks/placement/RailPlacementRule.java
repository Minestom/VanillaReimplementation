package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.blocks.placement.common.AbstractRailPlacementRule;
import net.minestom.vanilla.common.utils.DirectionUtils;

public class RailPlacementRule extends AbstractRailPlacementRule {

    public RailPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!isSupported(placementState.instance(), placementState.placePosition())) {
            return null;
        }

        Direction primaryDirection = DirectionUtils.getNearestHorizontalLookingDirection(placementState);

        FixedPlacementResult fixed = getFixedPlacement(placementState);
        if (fixed instanceof FixedPlacementResult.DefinitiveBlock) {
            return ((FixedPlacementResult.DefinitiveBlock) fixed).getBlock();
        }

        Direction lockedDirection = ((FixedPlacementResult.LockedDirection) fixed).getDirection();
        Direction direction = lockedDirection != null ? lockedDirection : primaryDirection;

        RailShape shape = RailShape.fromSides(java.util.Collections.singletonList(
                BlockFace.fromDirection(direction)));

        if (shape == null) {
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                shape = RailShape.NORTH_SOUTH;
            } else {
                shape = RailShape.EAST_WEST;
            }
        }

        shape = connectVertical(shape, placementState);

        return placementState.block().withProperty("shape", shape.toString());
    }
}
