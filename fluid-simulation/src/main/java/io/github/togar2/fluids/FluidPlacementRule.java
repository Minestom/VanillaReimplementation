package io.github.togar2.fluids;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidPlacementRule extends BlockPlacementRule {

    public FluidPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        if (placementState.instance() instanceof Instance instance) {
            MinestomFluids.scheduleTick(instance, placementState.placePosition(), placementState.block());
        }
        return placementState.block();
    }
}
