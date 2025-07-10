package net.minestom.vanilla.blocks.group.block;

import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 *
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 * <p>
 * A block group that combines multiple child block groups
 */
public class AggregateTagBlockGroup implements BlockGroup {
    private final BlockGroup[] children;

    /**
     * Creates a new aggregate block group
     * @param children The child block groups to aggregate
     */
    public AggregateTagBlockGroup(BlockGroup... children) {
        this.children = children;
    }

    @Override
    public Collection<Block> allMatching() {
        Collection<Block> result = new ArrayList<>();
        for (BlockGroup child : children) {
            result.addAll(child.allMatching());
        }
        return result;
    }
}
