package net.minestom.vanilla;

import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagWritable;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;
import net.minestom.vanilla.instance.SetupVanillaInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.tinylog.Logger;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

class VanillaReimplementationImpl implements VanillaReimplementation {

    private final ServerProcess process;
    private final Map<String, Instance> worlds = new ConcurrentHashMap<>();
    private final Map<EntityType, VanillaRegistry.EntitySpawner> entity2Spawner = new ConcurrentHashMap<>();

    private VanillaReimplementationImpl(@NotNull ServerProcess process) {
        this.process = process;
    }

    /**
     * Creates a new instance of {@link VanillaReimplementationImpl} and hooks into the server process.
     *
     * @param process the server process
     * @return the new instance
     */
    public static @NotNull VanillaReimplementationImpl hook(@NotNull ServerProcess process) {
        Logger.info("Setting up VanillaReimplementation...");
        VanillaReimplementationImpl vri = new VanillaReimplementationImpl(process);
        vri.INTERNAL_HOOK();
        Logger.info("VanillaReimplementation has been setup!");
        return vri;
    }

    // Vri Methods

    /**
     * @return the server process
     */
    public @NotNull ServerProcess process() {
        return process;
    }


    /**
     * Creates an {@link VanillaRegistry.EntityContext} for the given type and position
     *
     * @param type     the type of the entity
     * @param position the position of the entity at spawn
     * @return the context
     */
    public @NotNull VanillaRegistry.EntityContext entityContext(EntityType type, Point position) {
        return new EntityContextImpl(type, Pos.fromPoint(position));
    }

    /**
     * Creates an {@link VanillaRegistry.EntityContext} for the given type and position, with the
     * given tag values.
     *
     * @param type     the type of the entity
     * @param position the position of the entity at spawn
     * @return the context
     */
    public @NotNull VanillaRegistry.EntityContext entityContext(EntityType type, Point position,
                                                                @NotNull Consumer<TagWritable> tagWriter) {
        EntityContextImpl impl = new EntityContextImpl(type, Pos.fromPoint(position));
        tagWriter.accept(impl);
        return impl;
    }

    private record EntityContextImpl(@NotNull EntityType type, @NotNull Pos position,
                                     @NotNull TagHandler tagHandler) implements VanillaRegistry.EntityContext, TagWritable {
        public EntityContextImpl(@NotNull EntityType type, @NotNull Pos position) {
            this(type, position, TagHandler.newHandler());
        }

        @Override
        public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
            return tagHandler.getTag(tag);
        }

        @Override
        public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
            tagHandler.setTag(tag, value);
        }
    }

    /**
     * Creates a new vanilla entity, using the specified context, returning null if the entity type is not implemented.
     *
     * @param context the context
     * @return the new entity
     */
    public @Nullable Entity createEntity(@NotNull VanillaRegistry.EntityContext context) {
        // Get the spawner
        VanillaRegistry.EntitySpawner spawner = entity2Spawner.get(context.type());

        // Create the entity
        return spawner != null ? spawner.spawn(context) : null;
    }

    /**
     * Creates a new vanilla entity, using the specified context, returning a dummy entity if the entity type is not
     * implemented.
     *
     * @param context the context
     * @return the new entity
     */
    public @NotNull Entity createEntityOrDummy(@NotNull VanillaRegistry.EntityContext context) {
        Entity entity = createEntity(context);
        return entity != null ? entity : new DummyEntity(context.type());
    }

    private static class DummyEntity extends Entity {
        public DummyEntity(@NotNull EntityType type) {
            super(type);
        }
    }

    /**
     * Creates a vanilla instance.
     */
    public @NotNull Instance createInstance(@NotNull String name, @NotNull DimensionType dimension) {
        InstanceContainer instance = process().instance().createInstanceContainer(dimension);
        worlds.put(name, instance);

        // Anvil directory
        AnvilLoader loader = new AnvilLoader(name);
        instance.setChunkLoader(loader);

        // Setup event
        SetupVanillaInstanceEvent event = new SetupVanillaInstanceEvent(instance);
        process().eventHandler().call(event);

        return instance;
    }

    final class VanillaRegistryImpl implements VanillaRegistry {

        @Override
        public void register(@NotNull EntityType type, @NotNull EntitySpawner supplier) {
            entity2Spawner.put(type, supplier);
        }
    }

    private void INTERNAL_HOOK() {
        // Create the registry
        VanillaRegistry registry = new VanillaRegistryImpl();

        // Hook this core library
        hookCoreLibrary();

        // Load all the features and hook them
        Logger.info("Loading features...");
        for (Feature feature : ServiceLoader.load(Feature.class)) {
            Logger.info("Hooking feature: " + feature.namespaceID());
            feature.hook(this, registry);
        }
        Logger.info("All features have been loaded!");
    }

    private void hookCoreLibrary() {
        VanillaDimensionTypes.registerAll(process().dimension());
    }
}
