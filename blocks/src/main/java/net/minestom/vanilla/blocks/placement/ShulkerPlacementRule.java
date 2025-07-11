package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class ShulkerPlacementRule extends BlockPlacementRule {

    public ShulkerPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        BlockFace facing = determineFacing(placementState);
        return placementState.block().withProperty("facing", facing.toDirection().name().toLowerCase());
    }

    private BlockFace determineFacing(PlacementState placementState) {
        BlockFace blockFace = placementState.blockFace();
        if (blockFace != null) {
            return blockFace;
        }
        return BlockFace.NORTH;
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        return updateState.currentBlock();
    }
}
