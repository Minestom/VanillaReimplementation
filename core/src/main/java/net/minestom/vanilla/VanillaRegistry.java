package net.minestom.vanilla;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagWritable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * This registry object is used to register data and logic.
 */
public interface VanillaRegistry {

    /**
     * Registers an entity to its entity spawner.
     *
     * @param type     the type of the entity
     * @param supplier the entity spawner of the entity
     */
    void register(@NotNull EntityType type, @NotNull EntitySpawner supplier);

    interface EntitySpawner {
        @NotNull Entity spawn(@NotNull VanillaRegistry.EntityContext context);

    }

    interface EntityContext extends TagReadable {
        @NotNull EntityType type();
        @NotNull Pos position();
    }
}
