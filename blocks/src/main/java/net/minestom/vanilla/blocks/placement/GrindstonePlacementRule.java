package net.minestom.vanilla.blocks.placement;

import net.minestom.server.instance.block.Block;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class GrindstonePlacementRule extends FacedFacingPlacementRule {

    public GrindstonePlacementRule(Block block) {
        super(block);
    }

    @Override
    public boolean needSupport() {
        return false;
    }
}
