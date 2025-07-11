package net.minestom.vanilla.fluids.common;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.fluids.MinestomFluids;

public record FluidState(Block block, Fluid fluid) {
	public static FluidState of(Block block) {
		return new FluidState(block, MinestomFluids.get(block));
	}
	
	public boolean isSource() {
		return isSource(block);
	}
	
	public int getLevel() {
		return getLevel(block);
	}
	
	public boolean isFalling() {
		return isFalling(block);
	}
	
	public boolean canBeWaterLogged() {
		return canBeWaterlogged(block);
	}
	
	public boolean isWaterlogged() {
		return isWaterlogged(block);
	}
	
	public FluidState setWaterlogged(boolean waterlogged) {
		return of(setWaterlogged(block, waterlogged));
	}
	
	public boolean isEmpty() {
		return fluid.isEmpty();
	}
	
	public boolean isWater() {
		return fluid == MinestomFluids.WATER;
	}
	
	public boolean isLava() {
		return fluid == MinestomFluids.LAVA;
	}
	
	public boolean sameFluid(FluidState other) {
		return fluid == other.fluid;
	}
	
	public boolean sameFluid(Block other) {
		return fluid == MinestomFluids.get(other);
	}
	
	public double getHeight(Instance instance, BlockVec point) {
		return fluid.getHeight(this, instance, point);
	}
	
	public FluidState asFlowing(int level, boolean falling) {
		return new FluidState(block.withProperty("level", String.valueOf((falling ? 8 : 0) + (level == 0 ? 0 : 8 - level))), fluid);
	}
	
	public FluidState asSource(boolean falling) {
		return new FluidState(block.withProperty("level", falling ? "8" : "0"), fluid);
	}
	
	public static boolean isSource(Block block) {
		if (isWaterlogged(block)) return true;
		String levelStr = block.getProperty("level");
		return levelStr != null && Integer.parseInt(levelStr) == 0;
	}
	
	public static int getLevel(Block block) {
		if (isWaterlogged(block)) return 8;
		String levelStr = block.getProperty("level");
		if (levelStr == null) return 0;
		int level = Integer.parseInt(levelStr);
		if (level >= 8) return 8; // Falling water
		return 8 - level;
	}
	
	public static boolean isFalling(Block block) {
		String levelStr = block.getProperty("level");
		if (levelStr == null) return false;
		return Integer.parseInt(levelStr) >= 8;
	}
	
	public static boolean canBeWaterlogged(Block block) {
		return block.properties().containsKey("waterlogged");
	}
	
	public static boolean isWaterlogged(Block block) {
		String waterlogged = block.getProperty("waterlogged");
		return waterlogged != null && waterlogged.equals("true");
	}
	
	public static Block setWaterlogged(Block block, boolean waterlogged) {
		return block.withProperty("waterlogged", waterlogged ? "true" : "false");
	}
}
