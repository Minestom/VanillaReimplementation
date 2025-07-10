package net.minestom.vanilla.blocks.group.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.common.tag.BlockTags;

import java.util.Collection;

/**
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
