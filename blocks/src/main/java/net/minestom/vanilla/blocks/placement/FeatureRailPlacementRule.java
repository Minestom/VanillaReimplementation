package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.blocks.placement.common.AbstractRailPlacementRule;
import net.minestom.vanilla.common.utils.DirectionUtils;

public class FeatureRailPlacementRule extends AbstractRailPlacementRule {

    public FeatureRailPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!isSupported(placementState.instance(), placementState.placePosition())) {
            return null;
        }

        Direction primaryDirection = DirectionUtils.getNearestHorizontalLookingDirection(placementState);

        FixedPlacementResult fixed = getFixedPlacement(placementState);
        Direction lockedDirection;

        if (fixed instanceof FixedPlacementResult.DefinitiveBlock) {
            return ((FixedPlacementResult.DefinitiveBlock) fixed).getBlock();
        } else {
            lockedDirection = ((FixedPlacementResult.LockedDirection) fixed).getDirection();
        }

        BlockFace face = BlockFace.fromDirection(lockedDirection != null ? lockedDirection : primaryDirection);

        for (RailShape shape : RailShape.values()) {
            if (shape.isStraight() && !shape.isAscending() && shape.getSides().contains(face)) {
                for (BlockFace rotated : shape.getSides()) {
                    Block result = createSidedConnection(placementState, rotated.toDirection(), lockedDirection);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        RailShape shape = connectVertical(
            RailShape.fromSides(java.util.Arrays.asList(
                BlockFace.fromDirection(lockedDirection != null ? lockedDirection : primaryDirection)
            )),
            placementState
        );

        return placementState.block()
            .withProperty("shape", shape.toString());
    }
}
