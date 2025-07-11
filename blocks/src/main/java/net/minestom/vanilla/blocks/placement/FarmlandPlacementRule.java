package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
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
public class FarmlandPlacementRule extends BlockPlacementRule {

    public FarmlandPlacementRule(Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        Point abovePosition = updateState.blockPosition().relative(BlockFace.TOP);
        Block aboveBlock = updateState.instance().getBlock(abovePosition);

        if (aboveBlock.isSolid()) {
            return Block.DIRT;
        }

        return updateState.currentBlock();
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        return placementState.block();
    }
}
