package net.minestom.vanilla.entity;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface EntitySpawner {
  @NotNull Entity spawn(@NotNull EntityContext context);
}