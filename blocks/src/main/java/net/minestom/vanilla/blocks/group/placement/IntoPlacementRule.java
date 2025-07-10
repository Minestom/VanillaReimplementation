package net.minestom.vanilla.blocks.group.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

/**
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
