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
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.crafting.VanillaRecipe;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;
import net.minestom.vanilla.instance.SetupVanillaInstanceEvent;
import net.minestom.vanilla.logging.Level;
import net.minestom.vanilla.logging.Loading;
import net.minestom.vanilla.logging.Logger;
import net.minestom.vanilla.logging.StatusUpdater;
import net.minestom.vanilla.utils.DependencySorting;
import net.minestom.vanilla.utils.MinestomResources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class VanillaReimplementationImpl implements VanillaReimplementation {

    private final ServerProcess process;
    private final Map<NamespaceID, Instance> worlds = new ConcurrentHashMap<>();
    private final Map<EntityType, VanillaRegistry.EntitySpawner> entity2Spawner = new ConcurrentHashMap<>();
    private final Map<String, VanillaRecipe> id2Recipe = new ConcurrentHashMap<>();
    private final Map<Object, Random> randoms = Collections.synchronizedMap(new WeakHashMap<>());

    private VanillaReimplementationImpl(@NotNull ServerProcess process) {
        this.process = process;
    }

    /**
     * Creates a new instance of {@link VanillaReimplementationImpl} and hooks into the server process.
     *
     * @param process   the server process
     * @param predicate a predicate to determine which features to enable
     * @return the new instance
     */
    public static @NotNull VanillaReimplementationImpl hook(@NotNull ServerProcess process, Predicate<Feature> predicate) {
        Loading.start("Initialising");

        Loading.start("Initialising Minestom Resources...");
        MinestomResources.initialize();
        Loading.finish();

        Loading.updater().progress(0.33);

        long start = System.currentTimeMillis();

        Loading.start("Instantiating vri");
        VanillaReimplementationImpl vri = new VanillaReimplementationImpl(process);
        Loading.finish();

        Loading.updater().progress(0.66);
        vri.INTERNAL_HOOK(predicate);
        Loading.updater().progress(1);

        Loading.finish();

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
    public @NotNull Instance createInstance(@NotNull NamespaceID name, @NotNull DimensionType dimension) {
        InstanceContainer instance = process().instance().createInstanceContainer(dimension);
        worlds.put(name, instance);

        // Anvil directory
        AnvilLoader loader = new AnvilLoader(name.value());
        instance.setChunkLoader(loader);

        // Setup event
        SetupVanillaInstanceEvent event = new SetupVanillaInstanceEvent(instance);
        process().eventHandler().call(event);

        return instance;
    }

    @Override
    public @NotNull Instance getInstance(NamespaceID dimensionId) {
        return worlds.get(dimensionId);
    }

    @Override
    public @NotNull Random random(@NotNull Object key) {
        return randoms.computeIfAbsent(key, k -> new Random(key.hashCode()));
    }

    final class VanillaRegistryImpl implements VanillaRegistry {
        @Override
        public void register(@NotNull EntityType type, @NotNull EntitySpawner supplier) {
            entity2Spawner.put(type, supplier);
        }

        @Override
        public void register(@NotNull String recipeId, @NotNull VanillaRecipe recipe) {
            id2Recipe.put(recipeId, recipe);
        }
    }

    private void INTERNAL_HOOK(Predicate<Feature> predicate) {
        // Create the registry
        VanillaRegistry registry = new VanillaRegistryImpl();

        // Hook this core library
        Loading.start("Hooking Core Library");
        hookCoreLibrary();
        Loading.finish();

        // Load all the features and hook them
        Loading.start("Loading features from classpath");
        Set<Feature> features = ServiceLoader.load(Feature.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toUnmodifiableSet());
        Loading.finish();

        Loading.start("Validating dependencies");
        for (Feature feature : features) {
            try {
                for (Class<? extends Feature> dependency : feature.dependencies()) {
                    Objects.requireNonNull(dependency, "Dependency cannot be null!");
                }
            } catch (Exception e) {
                Logger.error("Failed to load features! Does one of your features have a missing dependency feature?", e);
                throw new RuntimeException(e);
            }
        }
        Loading.finish();

        Loading.start("Sorting features by dependencies");
        List<Feature> sortedByDependencies = DependencySorting.sort(features);
        Loading.finish();

        for (Feature feature : sortedByDependencies) {
            if (!predicate.test(feature)) {
                Logger.info("Skipping feature %s...%n", feature.namespaceId());
                continue;
            }

            try {
                instructHook(feature, registry);
            } catch (Exception e) {
                Logger.error("Failed to load feature: " + feature.namespaceId(), e);
                throw new RuntimeException(e);
            }
        }
    }

    private void instructHook(Feature feature, VanillaRegistry registry) {
        try {
            Loading.start("" + feature.namespaceId());

            Feature.HookContext context = new HookContextImpl(this, registry, Loading.updater());
            feature.hook(context);
        } catch (Exception e) {
            Logger.error(e, "Failed to load feature: %s%n", feature.namespaceId());
            throw new RuntimeException(e);
        } finally {
            Loading.finish();
        }
    }

    private record HookContextImpl(VanillaReimplementation vri,
                                       VanillaRegistry registry,
                                       StatusUpdater status) implements Feature.HookContext {
    }

    private void hookCoreLibrary() {
        VanillaDimensionTypes.registerAll(process().dimension());
    }
}
