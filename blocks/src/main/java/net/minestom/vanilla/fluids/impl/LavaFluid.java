package net.minestom.vanilla.fluids.impl;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.WorldEventPacket;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.world.DimensionType;
import net.minestom.server.worldevent.WorldEvent;
import net.minestom.vanilla.fluids.common.FlowableFluid;
import net.minestom.vanilla.fluids.common.FluidState;
import net.minestom.vanilla.fluids.event.FluidBlockBreakEvent;
import net.minestom.vanilla.fluids.event.LavaSolidifyEvent;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class LavaFluid extends FlowableFluid {
	public static final float MIN_REPLACE_HEIGHT = 0.44444445F;
	
	public LavaFluid() {
		super(Block.LAVA, Material.LAVA_BUCKET);
	}
	
	@Override
	protected boolean isInfinite() {
		return false; // TODO customize
	}
	
	@Override
	protected int getLevelDecreasePerBlock(Instance instance) {
		DimensionType dimensionType = MinecraftServer.getDimensionTypeRegistry().get(instance.getDimensionType());
		return (dimensionType != null && dimensionType.ultrawarm()) ? 1 : 2;
	}
	
	@Override
	protected int getHoleRadius(Instance instance) {
		DimensionType dimensionType = MinecraftServer.getDimensionTypeRegistry().get(instance.getDimensionType());
		return (dimensionType != null && dimensionType.ultrawarm()) ? 4 : 2;
	}
	
	@Override
	protected @Nullable FluidState onBreakingBlock(Instance instance, BlockVec point,
																								 BlockFace direction, Block block, FluidState newState) {
		FluidBlockBreakEvent event = new FluidBlockBreakEvent(instance, point, direction, block, newState);
		EventDispatcher.call(event);
		if (event.isCancelled()) return null;
		fizz(instance, point);
		return event.getNewState();
	}
	
	@Override
	protected boolean canBeReplacedWith(Instance instance, BlockVec point, FluidState currentState,
	                                    FluidState newState, BlockFace direction) {
		return currentState.getHeight(instance, point) >= MIN_REPLACE_HEIGHT && newState.isWater();
	}
	
	@Override
	public int getNextTickDelay(Instance instance, BlockVec point) {
		DimensionType dimensionType = MinecraftServer.getDimensionTypeRegistry().get(instance.getDimensionType());
		return (dimensionType != null && dimensionType.ultrawarm()) ? 10 : 30;
	}
	
	@Override
	public int getNextSpreadDelay(Instance instance, BlockVec point, FluidState state, FluidState newState) {
		int tickDelay = getNextTickDelay(instance, point);
		if (!state.isEmpty() && !newState.isEmpty() && !state.isFalling() && !newState.isFalling()) {
			double currentHeight = state.getHeight(instance, point);
			double newHeight = newState.getHeight(instance, point);
			
			if (newHeight > currentHeight && ThreadLocalRandom.current().nextInt(4) != 0)
				tickDelay *= 4;
		}
		
		return tickDelay;
	}
	
	@Override
	protected void flow(Instance instance, BlockVec point, FluidState newState, BlockFace direction) {
		if (direction == BlockFace.BOTTOM) {
			FluidState currentState = FluidState.of(instance.getBlock(point));
			if (currentState.isWater() && newState.isLava()) {
				LavaSolidifyEvent event = new LavaSolidifyEvent(instance, point, direction, Block.STONE);
				EventDispatcher.call(event);
				if (event.isCancelled()) return;
				
				instance.setBlock(point, event.getResultingBlock());
				fizz(instance, point);
				return;
			}
		}
		
		super.flow(instance, point, newState, direction);
	}
	
	@Override
	protected double getBlastResistance() {
		return 100.0;
	}
	
	public static void fizz(Instance instance, BlockVec point) {
		PacketSendingUtils.sendGroupedPacket(
				instance.getChunkAt(point).getViewers(),
				new WorldEventPacket(WorldEvent.LAVA_FIZZ.id(), point, 0, false)
		);
	}
}
