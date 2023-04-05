package net.minestom.vanilla;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.NotNull;

/**
 * This registry object is used to register data and logic.
 */
public sealed interface VanillaRegistry permits VanillaReimplementationImpl.VanillaRegistryImpl {

    /**
     * Registers an entity type to its entity spawner.
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
