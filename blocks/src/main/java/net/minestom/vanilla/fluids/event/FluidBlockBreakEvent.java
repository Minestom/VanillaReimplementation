package net.minestom.vanilla.fluids.event;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.vanilla.fluids.common.FluidState;
import org.jetbrains.annotations.NotNull;

public class FluidBlockBreakEvent implements InstanceEvent, BlockEvent, CancellableEvent {
	private final Instance instance;
	private final BlockVec blockPosition;
	private final BlockFace direction;
	private final Block block;
	
	private FluidState newState;
	private boolean cancelled;
	
	public FluidBlockBreakEvent(@NotNull Instance instance, @NotNull BlockVec blockPosition,
	                            @NotNull BlockFace direction, @NotNull Block block, @NotNull FluidState newState) {
		this.instance = instance;
		this.blockPosition = blockPosition;
		this.direction = direction;
		this.block = block;
		this.newState = newState;
	}
	
	@Override
	public @NotNull Instance getInstance() {
		return instance;
	}
	
	public @NotNull BlockVec getBlockPosition() {
		return blockPosition;
	}
	
	/**
	 * Returns the direction in which the fluid has flown.
	 * @return the direction as a BlockFace
	 */
	public @NotNull BlockFace getDirection() {
		return direction;
	}
	
	@Override
	public @NotNull Block getBlock() {
		return block;
	}
	
	/**
	 * @return the FluidState which will replace the block
	 */
	public FluidState getNewState() {
		return newState;
	}
	
	/**
	 * Sets the FluidState which will replace the block.
	 * @param newState the new FluidState
	 */
	public void setNewState(FluidState newState) {
		this.newState = newState;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}
