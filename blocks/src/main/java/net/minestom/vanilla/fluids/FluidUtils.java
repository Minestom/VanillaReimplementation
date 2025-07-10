package net.minestom.vanilla.fluids;

import net.minestom.server.ServerFlag;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.Direction;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class FluidUtils {

    public static Point raycastForFluid(Player player, Point startPosition, Vec direction, double maxDistance) {
        Point currentPosition = startPosition;
        double stepSize = 0.1; // Smaller steps give more precision, but increase computational cost
        double distance = 0.0;
        BoundingBox box = new BoundingBox(0.001, 0.001, 0.001);

        while (distance < maxDistance) {
            currentPosition = currentPosition.add(direction.mul(stepSize));
            var block = player.getInstance().getBlock(currentPosition);

            if (block.isLiquid()) {
                String levelStr = block.getProperty("level");
                if (Integer.parseInt(levelStr) == 0) {
                    return currentPosition; // Found a fluid block, return it
                }
            }

            var shape = block.registry().collisionShape();
            if (shape.intersectBox(currentPosition, box)) {
                break;
            }

            distance += stepSize;
        }

        return null; // No fluid found within the range
    }

    public static BlockFace findBlockFace(Player player, Point blockPosition) {
        for (BlockFace dir : BlockFace.values()) {
            Direction direction = dir.toDirection();
            Point relative = blockPosition.add(
                direction.normalX(),
                direction.normalY(),
                direction.normalZ()
            );

            if (isApproximatelyEqual(relative, blockPosition.relative(dir))) {
                return dir;
            }
        }
        return null;
    }

    public static boolean isApproximatelyEqual(Point point, Point other) {
        return isApproximatelyEqual(point, other, 1e-6);
    }

    public static boolean isApproximatelyEqual(Point point, Point other, double epsilon) {
        return Math.max(point.x() - other.x(), -epsilon) <= epsilon &&
               Math.max(point.y() - other.y(), -epsilon) <= epsilon &&
               Math.max(point.z() - other.z(), -epsilon) <= epsilon;
    }

    public static int getRelativeTicks(int ticks) {
        return ticks * (ServerFlag.SERVER_TICKS_PER_SECOND / 20);
    }

    public static BlockFace asBlockFace(Direction direction) {
        for (BlockFace face : BlockFace.values()) {
            if (face.toDirection() == direction) {
                return face;
            }
        }
        throw new IllegalArgumentException("No matching BlockFace for direction: " + direction);
    }
}
