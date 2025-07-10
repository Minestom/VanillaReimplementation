package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.utils.DirectionUtils;
import net.minestom.vanilla.common.utils.FluidUtils;
import org.jetbrains.annotations.NotNull;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class CampfireBlockPlacementRule extends BlockPlacementRule {

    public CampfireBlockPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        BlockFace facing = BlockFace.fromDirection(DirectionUtils.getNearestHorizontalLookingDirection(placementState).opposite());
        boolean waterlogged = FluidUtils.isWater(placementState.instance().getBlock(placementState.placePosition()));
        Block blockBelow = placementState.instance().getBlock(placementState.placePosition().add(0.0, -1.0, 0.0));
        boolean signalFire = blockBelow.compare(Block.HAY_BLOCK);

        return placementState.block()
            .withProperty("facing", facing.name().toLowerCase())
            .withProperty("waterlogged", String.valueOf(waterlogged))
            .withProperty("lit", String.valueOf(!waterlogged))
            .withProperty("signal_fire", String.valueOf(signalFire));
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        Block blockBelow = updateState.instance().getBlock(updateState.blockPosition().add(0.0, -1.0, 0.0));
        boolean signalFire = blockBelow.compare(Block.HAY_BLOCK);

        return updateState.currentBlock()
            .withProperty("signal_fire", String.valueOf(signalFire));
    }
}
