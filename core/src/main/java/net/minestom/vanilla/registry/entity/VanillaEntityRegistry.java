package net.minestom.vanilla.registry.entity;

import net.minestom.server.entity.EntityType;
import net.minestom.vanilla.entity.EntitySpawner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public sealed interface VanillaEntityRegistry permits VanillaEntityRegistryImpl {

  void bindEntitySpawner(@NotNull EntityType type, @NotNull EntitySpawner spawner);

  @Nullable EntitySpawner getEntitySpawner(@NotNull EntityType type);

  default @NotNull Optional<EntitySpawner> optionalEntitySpawner(@NotNull EntityType type) {
    return Optional.ofNullable(getEntitySpawner(type));
  }
}