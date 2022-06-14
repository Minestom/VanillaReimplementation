package net.minestom.vanilla;

import net.minestom.server.ServerProcess;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;
import net.minestom.vanilla.instance.SetupVanillaInstanceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class VanillaReimplementation {

    private static final Logger logger = Logger.getLogger("VanillaReimplementation");

    private final ServerProcess process;
    private final Map<String, Instance> worlds = new ConcurrentHashMap<>();

    private VanillaReimplementation(@NotNull ServerProcess process) {
        this.process = process;
    }

    /**
     * Creates a new instance of {@link VanillaReimplementation} and hooks into the server process.
     *
     * @param process the server process
     * @return the new instance
     */
    public static @NotNull VanillaReimplementation hook(@NotNull ServerProcess process) {
        logger.info("Setting up VanillaReimplementation...");
        VanillaReimplementation vri = new VanillaReimplementation(process);
        vri.INTERNAL_HOOK();
        logger.info("VanillaReimplementation has been setup!");
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

    private void INTERNAL_HOOK() {
        // Hook this core library
        hookCoreLibrary();
        // Load all the features and hook them
        var iterator = ServiceLoader.load(Feature.class).iterator();
        while (iterator.hasNext()) {
            var feature = iterator.next();
            logger.info("Hooking feature: " + feature.namespaceID());
            feature.hook(this);
        }
    }

    private void hookCoreLibrary() {
        VanillaDimensionTypes.registerAll(process().dimension());
    }

    /**
     * A feature is a collection of logic that can be hooked into a server process.
     */
    public interface Feature {

        /**
         * Hooks into this server process.
         *
         * @param vri the vanilla reimplementation object
         */
        void hook(@NotNull VanillaReimplementation vri);

        /**
         * @return a unique {@link NamespaceID} for this feature
         */
        @NotNull NamespaceID namespaceID();
    }
}
