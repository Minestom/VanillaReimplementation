package net.minestom.vanilla.common.utils;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule.PlacementState;
import net.minestom.server.utils.Direction;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DirectionUtils {

    public static String getAxis(Direction direction) {
        switch (direction) {
            case UP:
            case DOWN:
                return "y";
            case NORTH:
            case SOUTH:
                return "z";
            case EAST:
            case WEST:
                return "x";
            default:
                return "";
        }
    }

    public static Direction getHorizontalPlacementDirection(PlacementState state) {
        if (state.playerPosition() == null) return null;

        float yaw = state.playerPosition().yaw();
        if (yaw < 0) yaw += 360f;

        if (yaw >= 45f && yaw <= 135f) return Direction.EAST;
        else if (yaw >= 135f && yaw <= 225f) return Direction.SOUTH;
        else if (yaw >= 225f && yaw <= 315f) return Direction.WEST;
        else return Direction.NORTH;
    }

    public static int sixteenStepRotation(PlacementState state) {
        if (state.playerPosition() == null) return 0;

        float yaw = state.playerPosition().yaw() + (22.5f / 2.0f);
        if (yaw < 0) yaw += 360f;

        int rotation = (int)(yaw / 22.5f);
        return Math.max(0, Math.min(rotation, 15));
    }

    public static boolean canAttach(PlacementState state) {
        if (state.blockFace() == null) return false;

        Vec vec = state.blockFace().toDirection().vec();
        var anchor = state.placePosition().sub(vec);
        var anchorBlock = state.instance().getBlock(anchor);
        return anchorBlock.registry().collisionShape().isFaceFull(state.blockFace());
    }

    public static Direction getNearestLookingDirection(Pos position, Collection<Direction> allowedDirections) {
        Direction nearest = null;
        double minAngle = Double.MAX_VALUE;

        for (Direction direction : allowedDirections) {
            double angle = scalarProduct(direction.vec(), position.direction().normalize());
            if (nearest == null || angle < minAngle) {
                nearest = direction;
                minAngle = angle;
            }
        }
        return nearest;
    }

    public static Direction getNearestLookingDirection(PlacementState state, Collection<Direction> allowedDirections) {
        if (state.playerPosition() == null) return Direction.EAST;
        return getNearestLookingDirection(state.playerPosition(), allowedDirections);
    }

    public static Direction getNearestLookingDirection(PlacementState state) {
        if (state.playerPosition() == null) return Direction.EAST;
        return getNearestLookingDirection(state.playerPosition(), Arrays.asList(Direction.values()));
    }

    public static Direction getNearestHorizontalLookingDirection(PlacementState state) {
        if (state.playerPosition() == null) return Direction.EAST;

        List<Direction> horizontal = StreamSupport.stream(Arrays.stream(Direction.HORIZONTAL).spliterator(), false)
            .collect(Collectors.toList());
        return getNearestLookingDirection(state.playerPosition(), horizontal);
    }

    public static Direction getNearestLookingDirection(BlockHandler.Interaction interaction, Collection<Direction> allowedDirections) {
        return getNearestLookingDirection(interaction.getPlayer().getPosition(), allowedDirections);
    }

    public static Direction getNearestHorizontalLookingDirection(BlockHandler.Interaction interaction) {
        List<Direction> horizontal = StreamSupport.stream(Arrays.stream(Direction.HORIZONTAL).spliterator(), false)
            .collect(Collectors.toList());
        return getNearestLookingDirection(interaction.getPlayer().getPosition(), horizontal);
    }

    public static double scalarProduct(Vec a, Vec b) {
        Vec componentsMultiplied = a.mul(b);
        return componentsMultiplied.x() + componentsMultiplied.y() + componentsMultiplied.z();
    }

    public static Direction rotateR(Direction direction) {
        switch (direction) {
            case NORTH: return Direction.EAST;
            case EAST: return Direction.SOUTH;
            case SOUTH: return Direction.WEST;
            case WEST: return Direction.NORTH;
            default: return direction;
        }
    }

    public static Direction rotateL(Direction direction) {
        return rotateR(direction.opposite());
    }

    public static float getYaw(Direction direction) {
        switch (direction) {
            case UP:
            case DOWN:
                return 0f;
            case NORTH:
                return 180f;
            case EAST:
                return -90f;
            case SOUTH:
                return 0f;
            case WEST:
                return 90f;
            default:
                return 0f;
        }
    }
}
