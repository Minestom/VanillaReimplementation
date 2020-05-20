package net.minestom.vanilla.math;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * TODO: Move to Minestom Instance class
 */
public final class RayCast {

    public static Result rayCastBlocks(Instance instance, Vector start, Vector direction, float maxDistance, float stepLength, Predicate<BlockPosition> shouldContinue, Consumer<BlockPosition> onBlockStep) {
        return rayCastBlocks(instance, start.getX(), start.getY(), start.getZ(),
                direction.getX(), direction.getY(), direction.getZ(), maxDistance, stepLength, shouldContinue, onBlockStep);
    }

    public static Result rayCastBlocks(Instance instance, float startX, float startY, float startZ, float dirX, float dirY, float dirZ, float maxDistance, float stepLength, Predicate<BlockPosition> shouldContinue, Consumer<BlockPosition> onBlockStep) {
        Vector ray = new Vector(dirX, dirY, dirZ);
        ray.normalize().multiply(stepLength);

        Vector position = new Vector(startX, startY, startZ);
        BlockPosition blockPos = new BlockPosition(position);
        Set<BlockPosition> reachedPositions = new HashSet<>();

        Result hit = null;
        for (float step = 0f; step < maxDistance; step += stepLength) {
            blockPos.setX((int) Math.floor(position.getX()));
            blockPos.setY((int) Math.floor(position.getY()));
            blockPos.setZ((int) Math.floor(position.getZ()));

            if(blockPos.getY() < 0 || blockPos.getY() >= 255) { // out of bounds
                hit = new Result(position, HitType.OUT_OF_BOUNDS);
                break;
            }

            if(!shouldContinue.test(blockPos)) {
                hit = new Result(position, HitType.BLOCK);
                break;
            }

            if(!reachedPositions.contains(blockPos)) {
                reachedPositions.add(new BlockPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                onBlockStep.accept(blockPos);
            }

            position.add(ray.getX(), ray.getY(), ray.getZ());
        }
        if(hit == null) {
            return new Result(position, HitType.NONE);
        }
        return hit;
    }

    public static class Result {
        private Vector finalPosition;
        private HitType hitType;

        public Result(Vector finalPosition, HitType hitType) {
            this.finalPosition = finalPosition;
            this.hitType = hitType;
        }

        /**
         * Returns the final position the raycast tested BEFORE detecting a hit.
         * @return
         */
        public Vector getFinalPosition() {
            return finalPosition;
        }

        public HitType getHitType() {
            return hitType;
        }
    }

    public enum HitType {
        ENTITY,
        BLOCK,
        OUT_OF_BOUNDS,
        NONE;
    }

}
