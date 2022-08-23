package io.github.togar2.fluids;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

public class FluidPlacementRule extends BlockPlacementRule {
	
	public FluidPlacementRule(@NotNull Block block) {
		super(block);
	}
	
	@Override
	public @NotNull Block blockUpdate(@NotNull Instance instance, @NotNull Point blockPosition,
	                                  @NotNull Block currentBlock) {
		MinestomFluids.scheduleTick(instance, blockPosition, currentBlock);
		return currentBlock;
	}
	
	@Override
	public @NotNull Block blockPlace(@NotNull Instance instance, @NotNull Block block,
	                                 @NotNull BlockFace blockFace, @NotNull Point blockPosition,
	                                 @NotNull Player pl) {
		Point point = blockPosition.relative(blockFace);
		MinestomFluids.scheduleTick(instance, point, block);
		
		return block;
	}
}
