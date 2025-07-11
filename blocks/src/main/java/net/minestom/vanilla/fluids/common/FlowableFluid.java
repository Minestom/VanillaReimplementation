package net.minestom.vanilla.fluids.common;

import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.fluids.MinestomFluids;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public abstract class FlowableFluid extends Fluid {
	public FlowableFluid(Block defaultBlock, Material bucket) {
		super(defaultBlock, bucket);
	}

	@Override
	public void onTick(Instance instance, BlockVec point, FluidState state) {
		if (!state.isSource()) {
			FluidState updated = getUpdatedState(instance, point, state);
			if (updated.isEmpty()) {
				state = updated;
				instance.setBlock(point, Block.AIR);
			} else if (!updated.equals(state)) {
				state = updated;
				instance.setBlock(point, updated.block());
				MinestomFluids.scheduleTick(instance, point, getNextSpreadDelay(instance, point, state, updated));
			}
		}
		trySpread(instance, point, state);
	}

	protected void trySpread(Instance instance, BlockVec point, FluidState state) {
		if (state.isEmpty()) return;

		BlockVec down = point.add(0, -1, 0);
		FluidState downState = FluidState.of(instance.getBlock(down));
		if (canMaybeFlowThrough(state, downState, BlockFace.BOTTOM)) {
			FluidState newState = getUpdatedState(instance, down, downState);

			if (downState.fluid().canBeReplacedWith(instance, down, downState, newState, BlockFace.BOTTOM)
				&& canFill(instance, down, downState.block(), newState)) {
				flow(instance, down, newState, BlockFace.BOTTOM);

				if (getAdjacentSourceCount(instance, point) >= 3)
					flowSides(instance, point, state);
			}
		} else if (state.isSource() || !isWaterHole(instance, state, down)) {
			flowSides(instance, point, state);
		}
	}

	/**
	 * Flows to the sides whenever possible, or to a hole if found
	 */
	private void flowSides(Instance instance, BlockVec point, FluidState flowing) {
		int newLevel = flowing.getLevel() - getLevelDecreasePerBlock(instance);
		if (flowing.isFalling()) newLevel = 7;
		if (newLevel <= 0) return;

		Map<BlockFace, FluidState> map = getSpread(instance, point, flowing);
		for (Map.Entry<BlockFace, FluidState> entry : map.entrySet()) {
			BlockFace direction = entry.getKey();
			FluidState newState = entry.getValue();

			BlockVec offset = point.relative(direction);
			FluidState currentState = FluidState.of(instance.getBlock(offset));
			if (preventFlowTo(instance, offset, flowing, newState, currentState, direction)) continue;
			flow(instance, offset, newState, direction);
		}
	}

	private static final BlockFace[] HORIZONTAL = new BlockFace[] {
		BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST
	};

	/**
	 * Gets the updated state of a fluid block by taking into account its surrounding blocks.
	 */
	protected FluidState getUpdatedState(Instance instance, BlockVec point, FluidState original) {
		int highestLevel = 0;
		int stillCount = 0;

		for (BlockFace face : HORIZONTAL) {
			FluidState directionState = FluidState.of(instance.getBlock(point.relative(face)));
			if (directionState.fluid() != this || !receivesFlow(face, original, directionState))
				continue;

			if (directionState.isSource()) stillCount++;
			highestLevel = Math.max(highestLevel, directionState.getLevel());
		}

		if (isInfinite() && stillCount >= 2) {
			// If there's 2 or more still fluid blocks around
			// and below is still or a solid block, make this block still
			Block downBlock = instance.getBlock(point.add(0, -1, 0));
			if (downBlock.isSolid() || isMatchingSource(FluidState.of(downBlock))) {
				return defaultState.asSource(false);
			}
		}

		BlockVec above = point.add(0, 1, 0);
		FluidState aboveState = FluidState.of(instance.getBlock(above));
		if (!aboveState.isEmpty() && aboveState.fluid() == this && receivesFlow(BlockFace.TOP, original, aboveState))
			return defaultState.asFlowing(8, true);

		int newLevel = highestLevel - getLevelDecreasePerBlock(instance);
		if (newLevel <= 0) return MinestomFluids.AIR_STATE;
		return defaultState.asFlowing(newLevel, false);
	}

	private boolean receivesFlow(BlockFace face, FluidState from, FluidState to) {
		// Check if both block faces merged occupy the whole square
		return !from.block().registry().collisionShape().isOccluded(to.block().registry().collisionShape(), face);
	}

	/**
	 * Creates a unique id based on the relation between point and point2
	 */
	private static short getID(BlockVec point, BlockVec point2) {
		int i = (int) (point2.x() - point.x());
		int j = (int) (point2.z() - point.z());
		return (short) ((i + 128 & 0xFF) << 8 | j + 128 & 0xFF);
	}

	/**
	 * Returns a map with the directions the water can flow in and the block the water will become in that direction.
	 * If a hole is found within {@code getHoleRadius()} blocks, the water will only flow in that direction.
	 * A weight is used to determine which hole is the closest.
	 */
	protected Map<BlockFace, FluidState> getSpread(Instance instance, BlockVec point, FluidState flowing) {
		int weight = 1000;
		EnumMap<BlockFace, FluidState> map = new EnumMap<>(BlockFace.class);
		Short2BooleanOpenHashMap holeMap = new Short2BooleanOpenHashMap();

		for (BlockFace direction : HORIZONTAL) {
			BlockVec directionPoint = point.relative(direction);
			FluidState directionState = FluidState.of(instance.getBlock(directionPoint));
			short id = FlowableFluid.getID(point, directionPoint);

			if (!canMaybeFlowThrough(flowing, directionState, direction))
				continue;

			FluidState newState = getUpdatedState(instance, directionPoint, directionState);
			if (!canFill(instance, directionPoint, directionState.block(), newState))
				continue;

			boolean down = holeMap.computeIfAbsent(id, s -> {
				BlockVec downPoint = directionPoint.add(0, -1, 0);
				return isWaterHole(instance, defaultState.asFlowing(newState.getLevel(), false), downPoint);
			});

			int newWeight = down ? 0 : getWeight(instance, directionPoint, 1,
				direction.getOppositeFace(), directionState, point, holeMap);
			if (newWeight < weight) map.clear();

			if (newWeight <= weight) {
				if (directionState.fluid().canBeReplacedWith(instance, directionPoint, directionState, newState, direction))
					map.put(direction, newState);

				weight = newWeight;
			}
		}

		return map;
	}

	protected int getWeight(Instance instance, BlockVec point, int initialWeight, BlockFace skipCheck,
													FluidState flowing, BlockVec originalPoint, Short2BooleanMap short2BooleanMap) {
		// NOTE: flowing will often be air

		int weight = 1000;
		for (BlockFace direction : HORIZONTAL) {
			if (direction == skipCheck) continue;
			BlockVec directionPoint = point.relative(direction);
			FluidState directionState = FluidState.of(instance.getBlock(directionPoint));
			short id = FlowableFluid.getID(originalPoint, directionPoint);

			if (preventFlowTo(instance, directionPoint, flowing, defaultState.asFlowing(7, false), directionState, direction))
				continue;

			boolean down = short2BooleanMap.computeIfAbsent(id, s -> {
				BlockVec downPoint = directionPoint.add(0, -1, 0);
				return isWaterHole(instance, defaultState.asFlowing(7, false), downPoint);
			});
			if (down) return initialWeight;

			if (initialWeight < getHoleRadius(instance)) {
				int newWeight = getWeight(instance, directionPoint, initialWeight + 1,
					direction.getOppositeFace(), directionState, originalPoint, short2BooleanMap);
				if (newWeight < weight) weight = newWeight;
			}
		}
		return weight;
	}

	private int getAdjacentSourceCount(Instance instance, BlockVec point) {
		int i = 0;
		for (Direction direction : Direction.HORIZONTAL) {
			BlockVec currentPoint = point.add(direction.normalX(), direction.normalY(), direction.normalZ());
			Block block = instance.getBlock(currentPoint);
			if (!isMatchingSource(FluidState.of(block))) continue;
			++i;
		}
		return i;
	}

	/**
	 * @return whether this block can hold any fluid
	 */
	private boolean canHoldFluid(Block block) {
		if (FluidState.canBeWaterlogged(block)) return true;
		if (block.isSolid()) return false;

		Registry<Block> registry = MinecraftServer.process().blocks();

		return !(block.compare(Block.LADDER)
			|| block.compare(Block.SUGAR_CANE)
			|| block.compare(Block.BUBBLE_COLUMN)
			|| block.compare(Block.NETHER_PORTAL)
			|| block.compare(Block.END_PORTAL)
			|| block.compare(Block.END_GATEWAY)
			|| block.compare(Block.STRUCTURE_VOID)
			|| registry.getTag(Key.key("minecraft:signs")).contains(block)
			|| registry.getTag(Key.key("minecraft:doors")).contains(block));
	}

	/**
	 * @return whether the specified position can be filled with the specified FluidState
	 */
	private boolean canFill(Instance instance, BlockVec point, Block block, FluidState newState) {
		WaterlogHandler handler = MinestomFluids.getWaterlog(block);
		if (handler != null) return handler.canPlaceFluid(instance, point, block, newState);
		return canHoldFluid(block);
	}

	/**
	 * Used to determine if water should prioritize going to this point.
	 * @return whether the specified point is a hole
	 */
	private boolean isWaterHole(Instance instance, FluidState flowing, BlockVec flowTo) {
		FluidState flowToState = FluidState.of(instance.getBlock(flowTo));
		if (!receivesFlow(BlockFace.BOTTOM, flowing, flowToState)) return false; // Don't flow down if the path is obstructed
		if (flowing.sameFluid(flowToState)) return true; // Always flow down when the fluid is the same
		return canFill(instance, flowTo, flowToState.block(), flowing); // Flow down when the block beneath can be filled
	}

	private boolean canMaybeFlowThrough(FluidState flowing, FluidState state,
																			BlockFace face) {
		return !isMatchingSource(state) // Don't flow through if matching source
			&& receivesFlow(face, flowing, state) // Only flow through when the path is not obstructed
			&& canHoldFluid(state.block()); // Only flow through when the block can hold fluid
	}

	protected boolean preventFlowTo(Instance instance, BlockVec flowTo,
																	FluidState flowing, FluidState newState, FluidState currentState,
																	BlockFace flowFace) {
		return isMatchingSource(currentState) // Don't flow if matching source
			|| !receivesFlow(flowFace, flowing, currentState) // Only flow when the path is not obstructed
			|| !canFill(instance, flowTo, currentState.block(), newState); // Only flow when the block can be filled with this state
	}

	/**
	 * Puts the new fluid at the position, executing {@code onBreakingBlock()} before breaking any non-air block.
	 */
	protected void flow(Instance instance, BlockVec point, FluidState newState, BlockFace direction) {
		if (point.y() < MinecraftServer.getDimensionTypeRegistry().get(instance.getDimensionType()).minY())
			return; // Prevent errors when flowing into the void

		Block currentBlock = instance.getBlock(point);
		WaterlogHandler handler = MinestomFluids.getWaterlog(currentBlock);
		if (handler != null) {
			handler.placeFluid(instance, point, newState);
		} else {
			if (currentBlock.equals(newState.block())) return; // Prevent unnecessary updates

			if (!currentBlock.isAir()) {
				newState = onBreakingBlock(instance, point, direction, currentBlock, newState);
				if (newState == null) {
					// Event has been cancelled
					return;
				}
			}

			instance.placeBlock(new BlockHandler.Placement(newState.block(), instance, point));
		}
	}

	private boolean isMatchingSource(FluidState state) {
		return state.fluid() == this && state.isSource();
	}

	protected abstract boolean isInfinite();

	protected abstract int getLevelDecreasePerBlock(Instance instance);

	protected abstract int getHoleRadius(Instance instance);

	public int getNextSpreadDelay(Instance instance, BlockVec point, FluidState state, FluidState newState) {
		return getNextTickDelay(instance, point);
	}

	/**
	 * @return the FluidState that should be placed on the broken block position.
	 */
	protected abstract @Nullable FluidState onBreakingBlock(Instance instance, BlockVec point, BlockFace direction,
																													Block block, FluidState newState);

	private static boolean isSameFluidAbove(FluidState state, Instance instance, Point point) {
		return state.sameFluid(instance.getBlock(point.add(0, 1, 0)));
	}

	@Override
	public double getHeight(FluidState state, Instance instance, BlockVec point) {
		return isSameFluidAbove(state, instance, point) ? 1 : getHeight(state);
	}

	@Override
	public double getHeight(FluidState state) {
		return state.getLevel() / 9.0;
	}
}