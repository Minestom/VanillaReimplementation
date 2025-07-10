package net.minestom.vanilla.blocks.group.block;

import net.minestom.server.instance.block.Block;

import java.util.Collection;
import java.util.Collections;

/**
 * A block group that contains a fixed list of blocks
 */
public class ListBlockGroup implements BlockGroup {
    private final Collection<Block> all;

    /**
     * Creates a new block group with the given blocks
     * @param all The blocks to include in this group
     */
    public ListBlockGroup(Collection<Block> all) {
        this.all = Collections.unmodifiableCollection(all);
    }

    @Override
    public Collection<Block> allMatching() {
        return all;
    }
}
