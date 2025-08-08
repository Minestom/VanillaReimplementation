package net.minestom.vanilla.common.utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import org.jetbrains.annotations.NotNull;

public class TagHelper {

  private static final TagHelper INSTANCE = new TagHelper();
  private static final @NotNull Registry<Block> staticRegistry = Block.staticRegistry();

  private TagHelper() {

  }

  public static TagHelper getInstance() {
    return INSTANCE;
  }

  public boolean hasTag(Block element, String tag) {
    RegistryTag<Block> data = staticRegistry.getTag(Key.key(tag));
    if (data == null) return false;
    return data.contains(element);
  }

  public Set<Block> getHashed(String tag) {
    RegistryTag<Block> data = staticRegistry.getTag(TagKey.ofHash(tag));
    if (data == null) return new HashSet<>();

    return StreamSupport.stream(data.spliterator(), false)
        .map(obj -> Block.fromKey(obj.key()))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  public Set<Block> getTaggedWith(String tag) {
    RegistryTag<Block> data = staticRegistry.getTag(Key.key(tag));
    if (data == null) return new HashSet<>();

    return StreamSupport.stream(data.spliterator(), false)
        .map(obj -> Block.fromKey(obj.key()))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }




}
