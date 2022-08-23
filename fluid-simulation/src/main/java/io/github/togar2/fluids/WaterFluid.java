package io.github.togar2.fluids;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Direction;

import java.util.Optional;

public class WaterFluid extends FlowableFluid {
	
	public WaterFluid() {
		super(Block.WATER, Material.WATER_BUCKET);
	}
	
	@Override
	protected boolean isInfinite() {
		return true;
	}
	
	@Override
	protected boolean onBreakingBlock(Instance instance, Point point, Block block) {
		WaterBlockBreakEvent event = new WaterBlockBreakEvent(instance, point, block);
		return !event.isCancelled();
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
	public int getTickRate(Instance instance) {
		return 5;
	}
	
	@Override
	protected boolean canBeReplacedWith(Instance instance, Point point, Fluid other, Direction direction) {
		return direction == Direction.DOWN && this == other;
	}
	
	@Override
	protected double getBlastResistance() {
		return 100;
	}
}
