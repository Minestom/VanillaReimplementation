package net.minestom.vanilla.common.tag;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.RegistryTag;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A tag provider that uses Minestom's built-in tag system for blocks
 */
public class MinestomBlockTagProvider implements TagProvider<Block> {

    private static final MinestomBlockTagProvider INSTANCE = new MinestomBlockTagProvider();

    private MinestomBlockTagProvider() {
        // Private constructor for singleton
    }

    public static MinestomBlockTagProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean hasTag(Block element, String tag) {
        RegistryTag<Block> data = Block.staticRegistry().getTag(Key.key(tag));
        if (data == null) return false;
        return data.contains(element);
    }

    @Override
    public Set<Block> getTaggedWith(String tag) {
        RegistryTag<Block> data = Block.staticRegistry().getTag(Key.key(tag));
        if (data == null) return new HashSet<>();

        return StreamSupport.stream(data.spliterator(), false)
          .map(obj -> Block.fromKey(obj.key()))
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());
    }
}
