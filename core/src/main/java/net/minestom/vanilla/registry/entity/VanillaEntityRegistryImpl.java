package net.minestom.vanilla.registry.entity;

import net.minestom.server.entity.EntityType;
import net.minestom.vanilla.entity.EntitySpawner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class VanillaEntityRegistryImpl implements VanillaEntityRegistry {

  private final Map<EntityType, EntitySpawner> spawners = new ConcurrentHashMap<>();

  @Override
  public void bindEntitySpawner(@NotNull EntityType type, @NotNull EntitySpawner spawner) {
    this.spawners.put(type, spawner);
  }

  @Override
  public @Nullable EntitySpawner getEntitySpawner(@NotNull EntityType type) {
    return spawners.get(type);
  }
}