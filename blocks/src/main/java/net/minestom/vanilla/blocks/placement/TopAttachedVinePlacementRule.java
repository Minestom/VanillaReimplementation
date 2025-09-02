package net.minestom.vanilla.blocks.placement;

import net.minestom.server.coordinate.Point;
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
public class TopAttachedVinePlacementRule extends BlockPlacementRule {

    public TopAttachedVinePlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (!validatePosition(placementState.instance(), placementState.placePosition())) {
            return null;
        }
        return placementState.block();
    }

    @Override
    public @NotNull Block blockUpdate(UpdateState updateState) {
        if (!validatePosition(updateState.instance(), updateState.blockPosition())) {
            DroppedItemFactory.maybeDrop(updateState);
            return Block.AIR;
        }
        return updateState.currentBlock();
    }

    public boolean validatePosition(Block.Getter instance, Point position) {
        Block above = instance.getBlock(position.add(0.0, 1.0, 0.0));

        if (above.registry().collisionShape().isFaceFull(BlockFace.BOTTOM)) {
            return true;
        }
        if (above.compare(block)) {
            return true;
        }
        if (above.key().value().substring(0, 3).equals(block.key().value().substring(0, 3))) {
            return true;
        }
        return false;
    }
}
