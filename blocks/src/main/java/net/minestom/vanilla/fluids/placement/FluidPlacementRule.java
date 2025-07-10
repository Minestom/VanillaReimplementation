package net.minestom.vanilla.fluids.placement;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.fluids.MinestomFluids;
import net.minestom.vanilla.fluids.common.FluidState;
import org.jetbrains.annotations.NotNull;

public class FluidPlacementRule extends BlockPlacementRule {
	public FluidPlacementRule(@NotNull Block block) {
		super(block);
	}
	
	@Override
	public @NotNull Block blockUpdate(@NotNull UpdateState updateState) {
		String waterlogged = updateState.currentBlock().properties().get("waterlogged");
		if (waterlogged == null || waterlogged.equals("true")) {
			Instance instance = (Instance) updateState.instance();
			BlockVec blockVec = new BlockVec(updateState.blockPosition());
			MinestomFluids.scheduleTick(
					instance, blockVec,
					FluidState.of(updateState.currentBlock())
			);
		}
		return super.blockUpdate(updateState);
	}
	
	@Override
	public @NotNull Block blockPlace(@NotNull PlacementState placementState) {
		String waterlogged = placementState.block().properties().get("waterlogged");
		if (waterlogged == null || waterlogged.equals("true")) {
			Instance instance = (Instance) placementState.instance();
			BlockVec blockVec = new BlockVec(placementState.placePosition());
			MinestomFluids.scheduleTick(
					instance, blockVec,
					FluidState.of(placementState.block())
			);
		}
		return placementState.block();
	}
}
