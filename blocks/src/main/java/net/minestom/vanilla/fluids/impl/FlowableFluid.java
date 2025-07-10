package net.minestom.vanilla.fluids.impl;

import it.unimi.dsi.fastutil.shorts.Short2BooleanFunction;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.fluids.FluidUtils;
import net.minestom.vanilla.fluids.MinestomFluids;
import net.minestom.vanilla.fluids.event.BlockFluidReplacementEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class FlowableFluid extends Fluid {

    protected FlowableFluid(Block defaultBlock, Material bucket) {
        super(defaultBlock, bucket);
    }

    @Override
    public void onTick(Instance instance, Point point, Block block) {
        Block varBlock = block;

        if (!isSource(varBlock)) {
            Block updated = getUpdatedState(instance, point, varBlock);
            if (MinestomFluids.getFluidOnBlock(updated) == MinestomFluids.EMPTY) {
                varBlock = updated;
                instance.setBlock(point, Block.AIR, true);
            } else if (updated != varBlock) {
                varBlock = updated;
                instance.setBlock(point, updated, true);
            }
        }
        tryFlow(instance, point, varBlock);
    }

    protected void tryFlow(Instance instance, Point point, Block block) {
        Fluid fluid = MinestomFluids.getFluidInstanceOnBlock(block);
        if (fluid == MinestomFluids.getRegistry().get(MinestomFluids.EMPTY)) return;

        Point down = point.add(0.0, -1.0, 0.0);
        Block downBlock = instance.getBlock(down);
        Block updatedDownFluid = getUpdatedState(instance, down, downBlock);
        if (canFlow(instance, point, block, Direction.DOWN, down, downBlock, updatedDownFluid)) {
            flow(instance, down, downBlock, Direction.DOWN, updatedDownFluid);
            if (getAdjacentSourceCount(instance, point) >= 3) {
                flowSides(instance, point, block);
            }
        } else if (isSource(block) || !canFlowDown(instance, updatedDownFluid, point, block, down, downBlock)) {
            flowSides(instance, point, block);
        }
    }

    @Override
    public boolean isInTile(Block block) {
        return block.compare(defaultBlock);
    }

    private void flowSides(Instance instance, Point point, Block block) {
        int newLevel = getLevel(block) - getLevelDecreasePerBlock(instance);
        if (isFalling(block)) newLevel = 7;
        if (newLevel <= 0) return;

        Map<Direction, Block> map = getSpread(instance, point, block);
        for (Map.Entry<Direction, Block> entry : map.entrySet()) {
            Direction direction = entry.getKey();
            Block newBlock = entry.getValue();
            Point offset = point.add(
                    direction.normalX(),
                    direction.normalY(),
                    direction.normalZ()
            );
            Block currentBlock = instance.getBlock(offset);
            if (!canFlow(instance, point, block, direction, offset, currentBlock, newBlock)) continue;
            flow(instance, offset, currentBlock, direction, newBlock);
        }
    }

    public Block getUpdatedState(Instance instance, Point point, Block block) {
        int highestLevel = 0;
        int stillCount = 0;
        for (Direction direction : Direction.HORIZONTAL) {
            Point directionPos = point.add(
                    direction.normalX(),
                    direction.normalY(),
                    direction.normalZ()
            );
            Block directionBlock = instance.getBlock(directionPos);
            Fluid directionFluid = MinestomFluids.getFluidInstanceOnBlock(directionBlock);
            if (directionFluid != this || !receivesFlow(
                    direction,
                    instance,
                    point,
                    block,
                    directionPos,
                    directionBlock
            )) continue;

            if (isSource(directionBlock)) {
                ++stillCount;
            }
            highestLevel = Math.max(highestLevel, getLevel(directionBlock));
        }

        if (isInfinite() && stillCount >= 2) {
            Block downBlock = instance.getBlock(point.add(0.0, -1.0, 0.0));
            if (downBlock.isSolid() || isMatchingAndStill(downBlock)) {
                return getSource(false);
            }
        }

        Point above = point.add(0.0, 1.0, 0.0);
        Block aboveBlock = instance.getBlock(above);
        Fluid aboveFluid = MinestomFluids.getFluidInstanceOnBlock(aboveBlock);
        if (aboveFluid != MinestomFluids.getRegistry().get(MinestomFluids.EMPTY) && aboveFluid == this && receivesFlow(
                Direction.UP,
                instance,
                point,
                block,
                above,
                aboveBlock
        )) {
            return getFlowing(8, true);
        }

        int newLevel = highestLevel - getLevelDecreasePerBlock(instance);
        return newLevel <= 0 ? Block.AIR : getFlowing(newLevel, false);
    }

    private boolean receivesFlow(
            Direction face,
            Instance instance,
            Point point,
            Block block,
            Point fromPoint,
            Block fromBlock
    ) {
        if (block.isLiquid()) {
            switch (face) {
                case UP:
                    return fromBlock.isLiquid() || block.isSolid() || block.isAir();
                case DOWN:
                    return fromBlock.isLiquid() || fromBlock.isSolid() || fromBlock.isAir();
                default:
                    return true;
            }
        } else {
            switch (face) {
                case UP:
                case DOWN:
                    return block.isSolid() || block.isAir();
                default:
                    return block.isSolid() || block.isAir();
            }
        }
    }

    public Map<Direction, Block> getSpread(Instance instance, Point point, Block block) {
        int weight = 1000;
        EnumMap<Direction, Block> map = new EnumMap<>(Direction.class);
        Short2BooleanOpenHashMap holeMap = new Short2BooleanOpenHashMap();

        for (Direction direction : Direction.HORIZONTAL) {
            Point directionPoint = point.add(
                    direction.normalX(),
                    direction.normalY(),
                    direction.normalZ()
            );
            Block directionBlock = instance.getBlock(directionPoint);
            short id = getID(point, directionPoint);

            Block updatedBlock = getUpdatedState(instance, directionPoint, directionBlock);
            if (!canFlowThrough(
                    instance,
                    updatedBlock,
                    point,
                    block,
                    direction,
                    directionPoint,
                    directionBlock
            )) continue;

            boolean down = holeMap.computeIfAbsent(id, (Short2BooleanFunction) s -> {
                Point downPoint = directionPoint.add(0.0, -1.0, 0.0);
                return canFlowDown(
                        instance, getFlowing(getLevel(updatedBlock), false),
                        directionPoint, directionBlock, downPoint, instance.getBlock(downPoint)
                );
            });

            int newWeight = down ? 0 : getWeight(
                    instance, directionPoint, 1,
                    direction.opposite(), directionBlock, point, holeMap
            );
            if (newWeight < weight) map.clear();

            if (newWeight <= weight) {
                map.put(direction, updatedBlock);
                weight = newWeight;
            }
        }
        return map;
    }

    public int getWeight(
            Instance instance, Point point, int initialWeight, Direction skipCheck,
            Block block, Point originalPoint, Short2BooleanMap short2BooleanMap
    ) {
        int weight = 1000;
        for (Direction direction : Direction.HORIZONTAL) {
            if (direction == skipCheck) continue;
            Point directionPoint = point.add(
                    direction.normalX(),
                    direction.normalY(),
                    direction.normalZ()
            );
            Block directionBlock = instance.getBlock(directionPoint);
            short id = getID(originalPoint, directionPoint);

            if (!canFlowThrough(
                    instance, getFlowing(getLevel(block), false), point, block,
                    direction, directionPoint, directionBlock
            )) continue;

            boolean down = short2BooleanMap.computeIfAbsent(id, (Short2BooleanFunction) s -> {
                Point downPoint = directionPoint.add(0.0, -1.0, 0.0);
                Block downBlock = instance.getBlock(downPoint);
                return canFlowDown(
                        instance, getFlowing(getLevel(block), false),
                        directionPoint, downBlock, downPoint, downBlock
                );
            });
            if (down) return initialWeight;

            if (initialWeight < getHoleRadius(instance)) {
                int newWeight = getWeight(
                        instance, directionPoint, initialWeight + 1,
                        direction.opposite(), directionBlock, originalPoint, short2BooleanMap
                );
                if (newWeight < weight) weight = newWeight;
            }
        }
        return weight;
    }

    private int getAdjacentSourceCount(Instance instance, Point point) {
        int i = 0;
        for (Direction direction : Direction.HORIZONTAL) {
            Point currentPoint = point.add(
                    direction.normalX(),
                    direction.normalY(),
                    direction.normalZ()
            );
            Block block = instance.getBlock(currentPoint);
            if (!isMatchingAndStill(block)) continue;
            ++i;
        }
        return i;
    }

    private boolean canFill(Instance instance, Point point, Block block, Block flowing) {
        BlockFluidReplacementEvent event = new BlockFluidReplacementEvent(instance, block, new BlockVec(point));
        EventDispatcher.call(event);
        return block.isAir() || block.registry().isReplaceable() || !event.isCancelled();
    }

    private boolean canFlowDown(
            Instance instance, Block flowing, Point point,
            Block block, Point fromPoint, Block fromBlock
    ) {
        if (!getDirections(flowing).contains(Direction.DOWN)) return false;
        if (!this.receivesFlow(Direction.DOWN, instance, point, block, fromPoint, fromBlock)) return false;
        if (MinestomFluids.getFluidOnBlock(fromBlock) == this) return true;
        return this.canFill(instance, fromPoint, fromBlock, flowing);
    }

    private boolean canFlowThrough(
            Instance instance, Block flowing, Point point, Block block,
            Direction face, Point fromPoint, Block fromBlock
    ) {
        if (!getDirections(flowing).contains(face)) return false;
        return !isMatchingAndStill(fromBlock) && receivesFlow(face, instance, point, block, fromPoint, fromBlock)
                && canFill(instance, fromPoint, fromBlock, flowing);
    }

    protected boolean canFlow(
            Instance instance, Point fluidPoint, Block flowingBlock,
            Direction flowDirection, Point flowTo, Block flowToBlock, Block newFlowing
    ) {
        if (!getDirections(flowingBlock).contains(flowDirection)) return false;

        return MinestomFluids.getFluidInstanceOnBlock(flowToBlock)
                .canBeReplacedWith(instance, flowTo, MinestomFluids.getFluidInstanceOnBlock(newFlowing), flowDirection)
                && receivesFlow(flowDirection, instance, fluidPoint, flowingBlock, flowTo, flowToBlock)
                && canFill(instance, flowTo, flowToBlock, newFlowing);
    }

    protected void flow(Instance instance, Point point, Block block, Direction direction, Block newBlock) {
        if (block == newBlock) return;

        if (point.y() >= instance.getCachedDimensionType().minY()) instance.setBlock(point, newBlock);
    }

    private boolean isMatchingAndStill(Block block) {
        return MinestomFluids.getFluidOnBlock(block) == this && isSource(block);
    }

    public Block getFlowing(int level, boolean falling) {
        return defaultBlock.withProperty("level", Integer.toString(falling ? 8 : (level == 0 ? 0 : 8 - level)));
    }

    public Block getSource(boolean falling) {
        return falling ? defaultBlock.withProperty("level", "8") : defaultBlock;
    }

    protected abstract boolean isInfinite();

    protected abstract int getLevelDecreasePerBlock(Instance instance);

    protected abstract int getHoleRadius(Instance instance);

    @Override
    public double getHeight(Block block, Instance instance, Point point) {
        return isFluidAboveEqual(block, instance, point) ? 1.0 : getHeight(block);
    }

    @Override
    public double getHeight(Block block) {
        return (double) getLevel(block) / 9.0;
    }

    protected Collection<Direction> getDirections(Block block) {
        if (block.isLiquid()) return Arrays.asList(Direction.values());
        return Arrays.stream(Direction.values()).filter(
                direction -> !block.registry().collisionShape().isFaceFull(FluidUtils.asBlockFace(direction))
        ).collect(Collectors.toList());
    }

    private static short getID(Point point, Point point2) {
        int i = (int) (point2.x() - point.x());
        int j = (int) (point2.z() - point.z());
        return (short) (((i + 128 & 0xFF) << 8) | (j + 128 & 0xFF));
    }

    private static boolean isFluidAboveEqual(Block block, Instance instance, Point point) {
        return MinestomFluids.getFluidOnBlock(block) == MinestomFluids.getFluidOnBlock(
                instance.getBlock(
                        point.add(
                                0.0,
                                1.0,
                                0.0
                        )
                )
        );
    }
}

