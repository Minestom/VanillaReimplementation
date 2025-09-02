package net.minestom.vanilla.blocks.group.block;

import net.minestom.server.instance.block.Block;

import java.util.Collection;
import java.util.Collections;

/**
 *
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 * <p>
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
