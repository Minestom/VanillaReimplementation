package io.github.togar2.fluids;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Direction;

import java.util.Optional;

public abstract class Fluid {
	protected final Block defaultBlock;
	private final ItemStack bucket;
	
	public Fluid(Block block, Material bucket) {
		this.defaultBlock = block;
		this.bucket = ItemStack.of(bucket);
	}
	
	public Block getDefaultBlock() {
		return defaultBlock;
	}
	
	public ItemStack getBucket() {
		return bucket;
	}
	
	protected abstract boolean canBeReplacedWith(Instance instance, Point point,
	                                             Fluid other, Direction direction);
	
	public abstract int getNextTickDelay(Instance instance, Point point, Block block);
	
	public void onTick(Instance instance, Point point, Block block) {}
	
	protected boolean isEmpty() {
		return false;
	}
	
	protected abstract double getBlastResistance();
	
	public abstract double getHeight(Block block, Instance instance, Point point);
	public abstract double getHeight(Block block);
	
	public static boolean isSource(Block block) {
		String levelStr = block.getProperty("level");
		return levelStr == null || Integer.parseInt(levelStr) == 0;
	}
	
	public static int getLevel(Block block) {
		String levelStr = block.getProperty("level");
		if (levelStr == null) return 8;
		int level = Integer.parseInt(levelStr);
		if (level == 0) return 8; // Source block
		return level;
	}
	
	public static boolean isFalling(Block block) {
		String levelStr = block.getProperty("level");
		if (levelStr == null) return false;
		return Integer.parseInt(levelStr) >= 8;
	}
}
