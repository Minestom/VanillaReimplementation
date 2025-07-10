package net.minestom.vanilla.blocks.group.block;

import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
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
