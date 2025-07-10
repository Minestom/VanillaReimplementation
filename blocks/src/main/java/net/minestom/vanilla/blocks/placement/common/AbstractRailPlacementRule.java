package net.minestom.vanilla.blocks.placement.common;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.item.DroppedItemFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public abstract class AbstractRailPlacementRule extends BlockPlacementRule {

    protected AbstractRailPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockUpdate(UpdateState updateState) {
        if (!isSupported(updateState.instance(), updateState.blockPosition())) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return super.blockUpdate(updateState);
    }

    protected FixedPlacementResult getFixedPlacement(PlacementState placementState) {
        List<BlockFace> fixedSidesNorthSouth = getFixedSides(placementState.instance(), placementState.placePosition(), RailShape.NORTH_SOUTH);
        List<BlockFace> fixedSidesEastWest = getFixedSides(placementState.instance(), placementState.placePosition(), RailShape.EAST_WEST);
        if (fixedSidesNorthSouth.size() == 2) {
            return new FixedPlacementResult.DefinitiveBlock(placementState.block().withProperty("shape", RailShape.NORTH_SOUTH.toString()));
        } else if (fixedSidesEastWest.size() == 2) {
            return new FixedPlacementResult.DefinitiveBlock(placementState.block().withProperty("shape", RailShape.EAST_WEST.toString()));
        }

        Direction lockedDirection = null;
        if (!fixedSidesNorthSouth.isEmpty()) {
            lockedDirection = fixedSidesNorthSouth.get(0).toDirection();
        } else if (!fixedSidesEastWest.isEmpty()) {
            lockedDirection = fixedSidesEastWest.get(0).toDirection();
        }

        return new FixedPlacementResult.LockedDirection(lockedDirection);
    }

    protected boolean isSupported(Block.Getter instance, Point blockPos) {
        return instance
                .getBlock(blockPos.sub(0.0, 1.0, 0.0))
                .registry()
                .collisionShape()
                .isFaceFull(BlockFace.BOTTOM);
    }

    protected Block createSidedConnection(PlacementState placementState, Direction rotated, Direction lockedDirection) {
        RailShape shape = getSideConnection(
                placementState.instance(),
                placementState.placePosition(),
                BlockFace.fromDirection(rotated)
        );

        if (shape != null) {
            Point sidePos = placementState.placePosition().add(rotated.vec());
            Block sideBlock = placementState.instance().getBlock(sidePos);
            if (!sideBlock.compare(Block.RAIL, Block.Comparator.ID) && !shape.isStraight()) {
                return null;
            }
            RailShape ownShape = RailShape.fromSides(Stream.of(lockedDirection, rotated).filter(Objects::nonNull).map(BlockFace::fromDirection).collect(Collectors.toList()));
            ((Instance) placementState.instance()).setBlock(
                    sidePos,
                    sideBlock.withProperty("shape", shape.toString())
            );
            return placementState.block().withProperty("shape", ownShape.toString());
        }

        return null;
    }

    protected RailShape getSideConnection(Block.Getter instance, Point point, BlockFace side) {
        Point sidePos = point.add(side.toDirection().vec());
        Block sideBlock = instance.getBlock(sidePos);
        String shapeStr = sideBlock.getProperty("shape");
        if (shapeStr == null) return null;
        RailShape shape = RailShape.fromString(shapeStr);
        if (shape == null) return null;

        List<BlockFace> fixed = getFixedSides(instance, sidePos, shape);
        if (fixed.size() == 2) return null;

        if (fixed.isEmpty()) {
            // 0 bounds, rotate rail
            switch (side) {
                case NORTH:
                case SOUTH:
                    return RailShape.NORTH_SOUTH;
                case EAST:
                case WEST:
                    return RailShape.EAST_WEST;
                default:
                    return null;
            }
        } else {
            // 1 bound, other is free to be rotated to us
            return RailShape.fromSides(Arrays.asList(side.getOppositeFace(), fixed.get(0)));
        }
    }

    protected List<BlockFace> getFixedSides(Block.Getter instance, Point point, RailShape shape) {
        return shape.getSides().stream().filter(side -> {
            Block neighborBlock = instance.getBlock(point.add(side.toDirection().vec()));
            String neighborShapeStr = neighborBlock.getProperty("shape");
            RailShape neighborShape = neighborShapeStr != null ? RailShape.fromString(neighborShapeStr) : null;
            boolean directNeighbour = neighborShape != null && neighborShape.getSides().contains(side.getOppositeFace());

            Block lowerNeighborBlock = instance.getBlock(point.add(side.toDirection().vec().sub(0.0, 1.0, 0.0)));
            String lowerNeighborShapeStr = lowerNeighborBlock.getProperty("shape");
            RailShape lowerNeighborShape = lowerNeighborShapeStr != null ? RailShape.fromString(lowerNeighborShapeStr) : null;
            boolean lowerNeighbor = lowerNeighborShape != null && lowerNeighborShape.getSides().contains(side.getOppositeFace()) && lowerNeighborShape.isAscending();

            return directNeighbour || lowerNeighbor;
        }).collect(Collectors.toList());
    }

    protected RailShape connectVertical(
      RailShape shape,
      PlacementState placementState
    ) {
        RailShape mutShape = shape;
        List<BlockFace> initialSides = mutShape.getSides();
        for (BlockFace face : initialSides) {
            Point position = placementState.placePosition().add(face.toDirection().vec().add(0.0, 1.0, 0.0));
            String upperShapeStr = placementState.instance().getBlock(position).getProperty("shape");
            if (upperShapeStr == null) continue;
            RailShape upperShape = RailShape.fromString(upperShapeStr);
            if (upperShape == null) continue;

            if (upperShape.getSides().stream().noneMatch(initialSides::contains)) continue;
            // vertical placement
            mutShape = RailShape.getAscendingTowards(face);
        }

        //update verticals below
        List<BlockFace> finalSides = mutShape.getSides();
        for (BlockFace face : finalSides) {
            Point position = placementState.placePosition().add(face.toDirection().vec().add(0.0, -1.0, 0.0));
            Block lowerBlock = placementState.instance().getBlock(position);
            String lowerShapeStr = lowerBlock.getProperty("shape");
            if (lowerShapeStr == null) continue;
            RailShape lowerShape = RailShape.fromString(lowerShapeStr);
            if (lowerShape == null) continue;

            if (lowerShape.getSides().stream().noneMatch(finalSides::contains)) continue;
            if (!lowerShape.isStraight() || lowerShape.isAscending()) continue;
            ((Instance) placementState.instance()).setBlock(
              position,
              lowerBlock
                .withProperty("shape", RailShape.getAscendingTowards(face.getOppositeFace()).toString())
            );
        }
        return mutShape;
    }

    public enum RailShape {
        NORTH_SOUTH(BlockFace.NORTH, BlockFace.SOUTH),
        EAST_WEST(BlockFace.EAST, BlockFace.WEST),
        NORTH_EAST(BlockFace.NORTH, BlockFace.EAST),
        NORTH_WEST(BlockFace.NORTH, BlockFace.WEST),
        SOUTH_EAST(BlockFace.SOUTH, BlockFace.EAST),
        SOUTH_WEST(BlockFace.SOUTH, BlockFace.WEST),
        ASCENDING_EAST(BlockFace.EAST, BlockFace.WEST),
        ASCENDING_WEST(BlockFace.WEST, BlockFace.EAST),
        ASCENDING_NORTH(BlockFace.NORTH, BlockFace.SOUTH),
        ASCENDING_SOUTH(BlockFace.SOUTH, BlockFace.NORTH);

        private final List<BlockFace> sides;

        RailShape(BlockFace... sides) {
            this.sides = Arrays.asList(sides);
        }

        public List<BlockFace> getSides() {
            return sides;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public boolean isAscending() {
            return this == ASCENDING_EAST || this == ASCENDING_WEST || this == ASCENDING_NORTH || this == ASCENDING_SOUTH;
        }

        public boolean isStraight() {
            return this == NORTH_SOUTH || this == EAST_WEST || isAscending();
        }

        public static RailShape fromString(String value) {
            try {
                return valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public static RailShape fromSides(List<BlockFace> requiredSides) {
            return Arrays.stream(values())
                    .filter(shape -> new HashSet<>(shape.sides).containsAll(requiredSides))
                    .min(Comparator.comparingInt(Enum::ordinal))
                    .orElse(null);
        }

        public static RailShape getAscendingTowards(BlockFace face) {
            switch (face) {
                case NORTH:
                    return ASCENDING_NORTH;
                case SOUTH:
                    return ASCENDING_SOUTH;
                case EAST:
                    return ASCENDING_EAST;
                case WEST:
                    return ASCENDING_WEST;
                default:
                    throw new IllegalArgumentException("Only horizontal faces are supported");
            }
        }
    }

    public sealed interface FixedPlacementResult {
        final class DefinitiveBlock implements FixedPlacementResult {
            private final Block block;

            public DefinitiveBlock(Block block) {
                this.block = block;
            }

            public Block getBlock() {
                return block;
            }
        }

        final class LockedDirection implements FixedPlacementResult {
            private final Direction direction;

            public LockedDirection(Direction direction) {
                this.direction = direction;
            }

            public Direction getDirection() {
                return direction;
            }
        }
    }
}

