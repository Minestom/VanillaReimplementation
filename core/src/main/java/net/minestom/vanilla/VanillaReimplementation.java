package net.minestom.vanilla;

import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.TagWritable;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface VanillaReimplementation {

    /**
     * Creates a new instance of {@link VanillaReimplementation} and hooks into the server process.
     *
     * @param process the server process
     * @return the new instance
     */
    static @NotNull VanillaReimplementation hook(@NotNull ServerProcess process) {
        return VanillaReimplementationImpl.hook(process);
    }

    // Vri Methods

    /**
     * @return the server process
     */
    @NotNull ServerProcess process();

    /**
     * Creates an {@link net.minestom.vanilla.VanillaRegistry.EntityContext} for the given type and position
     *
     * @param type     the type of the entity
     * @param position the position of the entity at spawn
     * @return the context
     */
    default @NotNull VanillaRegistry.EntityContext entityContext(EntityType type, Point position) {
        return entityContext(type, position, writer -> {});
    }

    /**
     * Creates an {@link net.minestom.vanilla.VanillaRegistry.EntityContext} for the given type and position, with the
     * given tag values.
     *
     * @param type     the type of the entity
     * @param position the position of the entity at spawn
     * @return the context
     */
    @NotNull VanillaRegistry.EntityContext entityContext(EntityType type, Point position,
                                                         @NotNull Consumer<TagWritable> tagWriter);

    /**
     * Creates a new vanilla entity, using the specified context, returning null if the entity type is not implemented.
     *
     * @param context the context
     * @return the new entity
     */
    @Nullable Entity createEntity(@NotNull VanillaRegistry.EntityContext context);

    /**
     * Creates a new vanilla entity, using the specified context, returning a dummy entity if the entity type is not
     * implemented.
     *
     * @param context the context
     * @return the new entity
     */
    @NotNull Entity createEntityOrDummy(@NotNull VanillaRegistry.EntityContext context);

    /**
     * Creates a vanilla instance.
     */
    @NotNull Instance createInstance(@NotNull String name, @NotNull DimensionType dimension);

    /**
     * A feature is a collection of logic that can be hooked into a server process.
     */
    interface Feature {

        /**
         * Hooks into this server process.
         *
         * @param vri      the vanilla reimplementation object
         * @param registry the registry object
         */
        void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry);

        /**
         * @return a unique {@link NamespaceID} for this feature
         */
        @NotNull NamespaceID namespaceID();
    }
}
