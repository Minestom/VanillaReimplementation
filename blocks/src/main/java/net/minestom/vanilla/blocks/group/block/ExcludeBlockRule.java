package net.minestom.vanilla.blocks.group.block;

import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * A block group that excludes blocks from another group
 */
public class ExcludeBlockRule implements BlockGroup {
    private final BlockGroup blocks;
    private final BlockGroup excludedBlocks;

    /**
     * Creates a new block group with exclusions
     * @param blocks The base block group
     * @param excludedBlocks The blocks to exclude
     */
    public ExcludeBlockRule(BlockGroup blocks, BlockGroup excludedBlocks) {
        this.blocks = blocks;
        this.excludedBlocks = excludedBlocks;
    }

    @Override
    public Collection<Block> allMatching() {
        Collection<Block> excludedMatching = excludedBlocks.allMatching();
        Collection<Block> baseMatching = blocks.allMatching();

        // Create a new collection that contains blocks from baseMatching not in excludedMatching
        Collection<Block> result = new ArrayList<>();
        for (Block block : baseMatching) {
            if (!excludedMatching.contains(block)) {
                result.add(block);
            }
        }

        return result;
    }
}
