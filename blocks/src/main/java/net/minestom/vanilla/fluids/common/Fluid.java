package net.minestom.vanilla.fluids.common;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public abstract class Fluid {
	protected final FluidState defaultState;
	private final ItemStack bucket;
	
	public Fluid(Block block, Material bucket) {
		this.defaultState = new FluidState(block, this);
		this.bucket = ItemStack.of(bucket);
	}
	
	public FluidState getDefaultState() {
		return defaultState;
	}
	
	public ItemStack getBucket() {
		return bucket;
	}
	
	protected abstract boolean canBeReplacedWith(Instance instance, BlockVec point, FluidState currentState,
	                                             FluidState newState, BlockFace direction);
	
	public abstract int getNextTickDelay(Instance instance, BlockVec point);
	
	public void onTick(Instance instance, BlockVec point, FluidState state) {}
	
	protected boolean isEmpty() {
		return false;
	}
	
	protected abstract double getBlastResistance();
	
	public abstract double getHeight(FluidState state, Instance instance, BlockVec point);
	public abstract double getHeight(FluidState state);
}
