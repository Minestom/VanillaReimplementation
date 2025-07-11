package net.minestom.vanilla.blocks.group.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

/**
 *
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 * <p>
 * Interface for objects that can create block placement rules
 */
public interface IntoPlacementRule {
    /**
     * Creates a block placement rule for the given block
     * @param block The block to create a rule for
     * @return The created block placement rule
     */
    BlockPlacementRule createRule(Block block);
}
