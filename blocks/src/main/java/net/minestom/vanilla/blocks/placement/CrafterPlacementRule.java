package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.utils.DirectionUtils;
import org.jetbrains.annotations.NotNull;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class CrafterPlacementRule extends BlockPlacementRule {

    public CrafterPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        Direction direction = DirectionUtils.getNearestLookingDirection(placementState);
        Direction horizontalDirection = DirectionUtils.getNearestHorizontalLookingDirection(placementState);

        if (direction == Direction.DOWN) {
            return placementState.block()
                .withProperty("orientation", "down_" + horizontalDirection.name().toLowerCase());
        } else if (direction == Direction.UP) {
            return placementState.block()
                .withProperty("orientation", "up_" + horizontalDirection.opposite().name().toLowerCase());
        } else {
            return placementState.block()
                .withProperty("orientation", direction.name().toLowerCase() + "_up");
        }
    }
}
