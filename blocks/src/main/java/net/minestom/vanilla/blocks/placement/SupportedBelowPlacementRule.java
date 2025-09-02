package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import org.jetbrains.annotations.NotNull;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class SupportedBelowPlacementRule extends BlockPlacementRule {

    public SupportedBelowPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!placementState.instance().getBlock(placementState.placePosition().add(0.0, -1.0, 0.0))
                .registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            return null;
        }
        return placementState.block();
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        if (!updateState.instance().getBlock(updateState.blockPosition().add(0.0, -1.0, 0.0))
                .registry().collisionShape().isFaceFull(BlockFace.TOP)) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return updateState.currentBlock();
    }
}
