package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class ObserverPlacementRule extends BlockPlacementRule {

    public ObserverPlacementRule(Block block) {
        super(block);
    }

    @Override
    public Block blockPlace(PlacementState placementState) {
        if (placementState.blockFace() == null) {
            return placementState.block();
        }

        return placementState.block()
            .withProperty("facing", placementState.blockFace().name().toLowerCase())
            .withProperty("powered", "false");
    }
}
