package io.github.togar2.fluids;

import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;

import java.util.EnumMap;
import java.util.Map;

public abstract class FlowableFluid extends Fluid {
	
	public FlowableFluid(Block defaultBlock, Material bucket) {
		super(defaultBlock, bucket);
	}
	
	@Override
	public void onTick(Instance instance, Point point, Block block) {
		if (!isSource(block)) {
			Block updated = getUpdatedState(instance, point, block);
			if (MinestomFluids.get(updated).isEmpty()) {
				block = updated;
				instance.setBlock(point, Block.AIR);
			} else if (updated != block) {
				block = updated;
				instance.setBlock(point, updated);
			}
		}
		tryFlow(instance, point, block);
	}
	
	@Override
	public int getNextTickDelay(Instance instance, Point point, Block block) {
		return getTickRate(instance);
	}
	
	protected void tryFlow(Instance instance, Point point, Block block) {
		Fluid fluid = MinestomFluids.get(block);
		if (fluid.isEmpty()) return;
		
		Point down = point.add(0, -1, 0);
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
	
	/**
	 * Flows to the sides whenever possible, or to a hole if found
	 */
	private void flowSides(Instance instance, Point point, Block block) {
		int newLevel = getLevel(block) - getLevelDecreasePerBlock(instance);
		if (isFalling(block)) newLevel = 7;
		if (newLevel <= 0) return;
		
		Map<Direction, Block> map = getSpread(instance, point, block);
		for (Map.Entry<Direction, Block> entry : map.entrySet()) {
			Direction direction = entry.getKey();
			Block newBlock = entry.getValue();
			Point offset = point.add(direction.normalX(), direction.normalY(), direction.normalZ());
			Block currentBlock = instance.getBlock(offset);
			if (!canFlow(instance, point, block, direction, offset, currentBlock, newBlock)) continue;
			flow(instance, offset, currentBlock, direction, newBlock);
		}
	}
	
	/**
	 * Gets the updated state of a source block by taking into account its surrounding blocks.
	 */
	protected Block getUpdatedState(Instance instance, Point point, Block block) {
		int highestLevel = 0;
		int stillCount = 0;
		for (Direction direction : Direction.HORIZONTAL) {
			Point directionPos = point.add(direction.normalX(), direction.normalY(), direction.normalZ());
			Block directionBlock = instance.getBlock(directionPos);
			Fluid directionFluid = MinestomFluids.get(directionBlock);
			if (directionFluid != this || !receivesFlow(direction, instance, point, block, directionPos, directionBlock))
				continue;
			
			if (isSource(directionBlock)) {
				++stillCount;
			}
			highestLevel = Math.max(highestLevel, getLevel(directionBlock));
		}
		
		if (isInfinite() && stillCount >= 2) {
			// If there's 2 or more still fluid blocks around
			// and below is still or a solid block, make this block still
			Block downBlock = instance.getBlock(point.add(0, -1, 0));
			if (downBlock.isSolid() || isMatchingAndStill(downBlock)) {
				return getSource(false);
			}
		}
		
		Point above = point.add(0, 1, 0);
		Block aboveBlock = instance.getBlock(above);
		Fluid aboveFluid = MinestomFluids.get(aboveBlock);
		if (!aboveFluid.isEmpty() && aboveFluid == this
				&& receivesFlow(Direction.UP, instance, point, block, above, aboveBlock)) {
			return getFlowing(8, true);
		}
		
		int newLevel = highestLevel - getLevelDecreasePerBlock(instance);
		if (newLevel <= 0) return Block.AIR;
		return getFlowing(newLevel, false);
	}
	
	private boolean receivesFlow(Direction face, Instance instance, Point point,
	                             Block block, Point fromPoint, Block fromBlock) {
		// Vanilla seems to check if the adjacent block shapes cover the same square, but this seems to work as well
		// (Might not work with some special blocks)
		// If there is anything wrong it is most likely this method :D
		
		if (block.isLiquid()) {
			if (face == Direction.UP) {
				if (fromBlock.isLiquid()) return true;
				return block.isSolid() || block.isAir();
				//return isSource(block) || getLevel(block) == 8;
			} else if (face == Direction.DOWN) {
				if (fromBlock.isLiquid()) return true;
				return fromBlock.isSolid() || fromBlock.isAir();
				//return isSource(fromBlock) || getLevel(fromBlock) == 8;
			} else {
				return true;
			}
		} else {
			if (face == Direction.UP) {
				return block.isSolid() || block.isAir();
			} else if (face == Direction.DOWN) {
				return block.isSolid() || block.isAir();
			} else {
				return block.isSolid() || block.isAir();
			}
		}
	}
	
	/**
	 * Creates a unique id based on the relation between point and point2
	 */
	private static short getID(Point point, Point point2) {
		int i = (int) (point2.x() - point.x());
		int j = (int) (point2.z() - point.z());
		return (short) ((i + 128 & 0xFF) << 8 | j + 128 & 0xFF);
	}
	
	/**
	 * Returns a map with the directions the water can flow in and the block the water will become in that direction.
	 * If a hole is found within {@code getHoleRadius()} blocks, the water will only flow in that direction.
	 * A weight is used to determine which hole is the closest.
	 */
	protected Map<Direction, Block> getSpread(Instance instance, Point point, Block block) {
		int weight = 1000;
		EnumMap<Direction, Block> map = new EnumMap<>(Direction.class);
		Short2BooleanOpenHashMap holeMap = new Short2BooleanOpenHashMap();
		
		for (Direction direction : Direction.HORIZONTAL) {
			Point directionPoint = point.add(direction.normalX(), direction.normalY(), direction.normalZ());
			Block directionBlock = instance.getBlock(directionPoint);
			short id = FlowableFluid.getID(point, directionPoint);
			
			Block updatedBlock = getUpdatedState(instance, directionPoint, directionBlock);
			if (!canFlowThrough(instance, updatedBlock, point, block, direction, directionPoint, directionBlock))
				continue;
			
			boolean down = holeMap.computeIfAbsent(id, s -> {
				Point downPoint = directionPoint.add(0, -1, 0);
				return canFlowDown(
						instance, getFlowing(getLevel(updatedBlock), false),
						directionPoint, directionBlock, downPoint, instance.getBlock(downPoint)
				);
			});
			
			int newWeight = down ? 0 : getWeight(instance, directionPoint, 1,
					direction.opposite(), directionBlock, point, holeMap);
			if (newWeight < weight) map.clear();
			
			if (newWeight <= weight) {
				map.put(direction, updatedBlock);
				weight = newWeight;
			}
		}
		return map;
	}
	
	protected int getWeight(Instance instance, Point point, int initialWeight, Direction skipCheck,
	                        Block block, Point originalPoint, Short2BooleanMap short2BooleanMap) {
		int weight = 1000;
		for (Direction direction : Direction.HORIZONTAL) {
			if (direction == skipCheck) continue;
			Point directionPoint = point.add(direction.normalX(), direction.normalY(), direction.normalZ());
			Block directionBlock = instance.getBlock(directionPoint);
			short id = FlowableFluid.getID(originalPoint, directionPoint);
			
			if (!canFlowThrough(instance, getFlowing(getLevel(block), false), point, block,
					direction, directionPoint, directionBlock)) continue;
			
			boolean down = short2BooleanMap.computeIfAbsent(id, s -> {
				Point downPoint = directionPoint.add(0, -1, 0);
				Block downBlock = instance.getBlock(downPoint);
				return canFlowDown(
						instance, getFlowing(getLevel(block), false),
						directionPoint, downBlock, downPoint, downBlock
				);
			});
			if (down) return initialWeight;
			
			if (initialWeight < getHoleRadius(instance)) {
				int newWeight = getWeight(instance, directionPoint, initialWeight + 1,
						direction.opposite(), directionBlock, originalPoint, short2BooleanMap);
				if (newWeight < weight) weight = newWeight;
			}
		}
		return weight;
	}
	
	private int getAdjacentSourceCount(Instance instance, Point point) {
		int i = 0;
		for (Direction direction : Direction.HORIZONTAL) {
			Point currentPoint = point.add(direction.normalX(), direction.normalY(), direction.normalZ());
			Block block = instance.getBlock(currentPoint);
			if (!isMatchingAndStill(block)) continue;
			++i;
		}
		return i;
	}
	
	/**
	 * Returns whether the fluid can flow through a specific block
	 */
	private boolean canFill(Instance instance, Point point, Block block, Block flowing) {
		//TODO check waterloggable
		TagManager tags = MinecraftServer.getTagManager();
		if (block.compare(Block.LADDER)
				|| block.compare(Block.SUGAR_CANE)
				|| block.compare(Block.BUBBLE_COLUMN)
				|| block.compare(Block.NETHER_PORTAL)
				|| block.compare(Block.END_PORTAL)
				|| block.compare(Block.END_GATEWAY)
				|| block.compare(Block.KELP)
				|| block.compare(Block.KELP_PLANT)
				|| block.compare(Block.SEAGRASS)
				|| block.compare(Block.TALL_SEAGRASS)
				|| block.compare(Block.SEA_PICKLE)
				|| tags.getTag(Tag.BasicType.BLOCKS, "minecraft:signs").contains(block.namespace())
				|| block.name().contains("door")
				|| block.name().contains("coral")) {
			return false;
		}
		return !block.isSolid();
	}
	
	private boolean canFlowDown(Instance instance, Block flowing, Point point,
	                            Block block, Point fromPoint, Block fromBlock) {
		if (!this.receivesFlow(Direction.DOWN, instance, point, block, fromPoint, fromBlock)) return false;
		if (MinestomFluids.get(fromBlock) == this) return true;
		return this.canFill(instance, fromPoint, fromBlock, flowing);
	}
	
	private boolean canFlowThrough(Instance instance, Block flowing, Point point, Block block,
	                               Direction face, Point fromPoint, Block fromBlock) {
		return !isMatchingAndStill(fromBlock)
				&& receivesFlow(face, instance, point, block, fromPoint, fromBlock)
				&& canFill(instance, fromPoint, fromBlock, flowing);
	}
	
	protected boolean canFlow(Instance instance, Point fluidPoint, Block flowingBlock,
	                          Direction flowDirection, Point flowTo, Block flowToBlock, Block newFlowing) {
		return MinestomFluids.get(flowToBlock).canBeReplacedWith(instance, flowTo, MinestomFluids.get(newFlowing), flowDirection)
				&& receivesFlow(flowDirection, instance, fluidPoint, flowingBlock, flowTo, flowToBlock)
				&& canFill(instance, flowTo, flowToBlock, newFlowing);
	}
	
	/**
	 * Sets the position to the new block, executing {@code onBreakingBlock()} before breaking any non-air block.
	 */
	protected void flow(Instance instance, Point point, Block block, Direction direction, Block newBlock) {
		//TODO waterloggable check
		boolean cancel = false;
		if (!block.isAir()) {
			if (!onBreakingBlock(instance, point, block))
				cancel = true;
		}
		
		if (!cancel) instance.setBlock(point, newBlock);
	}
	
	private boolean isMatchingAndStill(Block block) {
		return MinestomFluids.get(block) == this && isSource(block);
	}
	
	public Block getFlowing(int level, boolean falling) {
		return defaultBlock.withProperty("level", String.valueOf(falling ? 8 : level));
	}
	
	public Block getSource(boolean falling) {
		return falling ? defaultBlock.withProperty("level", "8") : defaultBlock;
	}
	
	protected abstract boolean isInfinite();

	protected abstract int getLevelDecreasePerBlock(Instance instance);

	protected abstract int getHoleRadius(Instance instance);
	
	/**
	 * Returns whether the block can be broken
	 */
	protected abstract boolean onBreakingBlock(Instance instance, Point point, Block block);
	
	public abstract int getTickRate(Instance instance);
	
	private static boolean isFluidAboveEqual(Block block, Instance instance, Point point) {
		return MinestomFluids.get(block) == MinestomFluids.get(instance.getBlock(point.add(0, 1, 0)));
	}
	
	@Override
	public double getHeight(Block block, Instance instance, Point point) {
		return isFluidAboveEqual(block, instance, point) ? 1 : getHeight(block);
	}
	
	@Override
	public double getHeight(Block block) {
		return getLevel(block) / 9.0;
	}
}
