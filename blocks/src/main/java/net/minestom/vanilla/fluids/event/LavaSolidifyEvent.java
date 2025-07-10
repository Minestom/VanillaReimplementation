package net.minestom.vanilla.fluids.event;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;

public class LavaSolidifyEvent implements InstanceEvent, CancellableEvent {
	private final Instance instance;
	private final BlockVec blockPosition;
	private final BlockFace direction;
	
	private Block resultingBlock;
	private boolean cancelled;
	
	public LavaSolidifyEvent(@NotNull Instance instance, @NotNull BlockVec blockPosition,
	                         @NotNull BlockFace direction, @NotNull Block resultingBlock) {
		this.instance = instance;
		this.blockPosition = blockPosition;
		this.direction = direction;
		this.resultingBlock = resultingBlock;
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
	 * This could be the lava fluid itself (if lava on top of water) or the other fluid (if water on top of or next to lava).
	 * @return the direction as a BlockFace
	 */
	public @NotNull BlockFace getDirection() {
		return direction;
	}
	
	public @NotNull Block getResultingBlock() {
		return resultingBlock;
	}
	
	public void setResultingBlock(@NotNull Block resultingBlock) {
		this.resultingBlock = resultingBlock;
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
