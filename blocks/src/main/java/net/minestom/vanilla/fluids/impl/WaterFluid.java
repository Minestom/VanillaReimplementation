package net.minestom.vanilla.fluids.impl;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.Material;
import net.minestom.vanilla.fluids.common.FlowableFluid;
import net.minestom.vanilla.fluids.common.FluidState;
import net.minestom.vanilla.fluids.event.FluidBlockBreakEvent;
import org.jetbrains.annotations.Nullable;

public class WaterFluid extends FlowableFluid {
	public WaterFluid() {
		super(Block.WATER, Material.WATER_BUCKET);
	}
	
	@Override
	protected boolean isInfinite() {
		return true;
	}
	
	@Override
	protected @Nullable FluidState onBreakingBlock(Instance instance, BlockVec point,
																								 BlockFace direction, Block block, FluidState newState) {
		FluidBlockBreakEvent event = new FluidBlockBreakEvent(instance, point, direction, block, newState);
		EventDispatcher.call(event);
		return event.isCancelled() ? null : event.getNewState();
	}
	
	@Override
	protected int getHoleRadius(Instance instance) {
		return 4;
	}
	
	@Override
	public int getLevelDecreasePerBlock(Instance instance) {
		return 1;
	}
	
	@Override
	public int getNextTickDelay(Instance instance, BlockVec point) {
		return 5;
	}
	
	@Override
	protected boolean canBeReplacedWith(Instance instance, BlockVec point, FluidState currentState,
	                                    FluidState newState, BlockFace direction) {
		return direction == BlockFace.BOTTOM && !newState.isWater();
	}
	
	@Override
	protected double getBlastResistance() {
		return 100;
	}
}
