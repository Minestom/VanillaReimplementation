package net.minestom.vanilla.blocks.group.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.common.tag.BlockTags;

import java.util.Collection;

/**
 *
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 * <p>
 * A block group that contains blocks matching a specific tag
 */
public class TagBlockGroup implements BlockGroup {
    private final Key key;

    /**
     * Creates a new block group for blocks with a specific tag
     * @param key The tag key
     */
    public TagBlockGroup(Key key) {
        this.key = key;
    }

    @Override
    public Collection<Block> allMatching() {
        return BlockTags.getInstance().getTaggedWith(key.asString());
    }
}
