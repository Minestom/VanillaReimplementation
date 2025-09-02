package net.minestom.vanilla.blocks.placement.util;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.Direction;

import java.util.Locale;


/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class States {
    public static final String HALF = "half";
    public static final String FACING = "facing";
    public static final String FACE = "face";
    public static final String SHAPE = "shape";
    public static final String WATERLOGGED = "waterlogged";
    public static final String NORTH = "north";
    public static final String EAST = "east";
    public static final String SOUTH = "south";
    public static final String WEST = "west";

    public static BlockFace getHalf(Block block) {
        if (block.getProperty(HALF) == null) {
            return BlockFace.BOTTOM;
        }
        return BlockFace.valueOf(block.getProperty(HALF).toUpperCase(Locale.getDefault()));
    }

    public static BlockFace getFacing(Block block) {
        if (block.getProperty(FACING) == null) {
            return BlockFace.NORTH;
        }
        return BlockFace.valueOf(block.getProperty(FACING).toUpperCase(Locale.getDefault()));
    }

    public static Direction getDirection(Block block) {
        if (block.getProperty(FACE) == null) {
            return Direction.NORTH;
        }
        switch (block.getProperty(FACE)) {
            case "ceiling":
                return Direction.DOWN;
            case "floor":
                return Direction.UP;
            default:
                return getFacing(block).toDirection();
        }
    }

    public static Direction rotateYCounterclockwise(Direction direction) {
        switch (direction.ordinal()) {
            case 2: // NORTH
                return Direction.WEST;
            case 5: // WEST
                return Direction.SOUTH;
            case 3: // EAST
                return Direction.NORTH;
            case 4: // SOUTH
                return Direction.EAST;
            default:
                throw new IllegalStateException("Unable to rotate " + direction);
        }
    }

    public static Direction rotateYClockwise(Direction direction) {
        switch (direction.ordinal()) {
            case 2: // NORTH
                return Direction.EAST;
            case 5: // WEST
                return Direction.NORTH;
            case 3: // EAST
                return Direction.SOUTH;
            case 4: // SOUTH
                return Direction.WEST;
            default:
                throw new IllegalStateException("Unable to rotate " + direction);
        }
    }

    public static Axis getAxis(Direction direction) {
        switch (direction) {
            case DOWN:
            case UP:
                return Axis.Y;
            case NORTH:
            case SOUTH:
                return Axis.Z;
            case WEST:
            case EAST:
                return Axis.X;
            default:
                throw new IllegalStateException("Unknown direction: " + direction);
        }
    }

    public enum Axis {
        X,
        Y,
        Z
    }
}
