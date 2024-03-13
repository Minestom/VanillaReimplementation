package net.minestom.vanilla.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.NotNull;

public interface EntityContext extends TagReadable {
  @NotNull EntityType type();

  @NotNull Pos position();
}
