package net.minestom.vanilla.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.logging.Level;
import net.minestom.vanilla.logging.Loading;
import net.minestom.vanilla.logging.Logger;
import net.minestom.vanilla.system.RayFastManager;
import net.minestom.vanilla.system.ServerProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

class VanillaServer {

    /**
     * A standard vanilla server launch used for testing purposes
     *
     * @param args arguments passed from console
     */
    public static void main(String[] args) {

        // Use the static server process
        MinecraftServer server = MinecraftServer.init();

        // Use SETUP logging level since this is a standalone server.
        Loading.level(Level.SETUP);
        Logger.info("Setting up vri...");
        VanillaReimplementation vri = VanillaReimplementation.hook(MinecraftServer.process());

        VanillaServer vanillaServer = new VanillaServer(server, vri, args);
        Logger.info("Vanilla Reimplementation (%s) is setup.", MinecraftServer.VERSION_NAME);

        vanillaServer.start("0.0.0.0", 25565);
    }

    private final MinecraftServer minecraftServer;
    private final @NotNull ServerProperties serverProperties;

    private final @NotNull VanillaReimplementation vri;

    // Instances
    private final @NotNull Instance overworld;

    public VanillaServer(@NotNull MinecraftServer minecraftServer, @NotNull VanillaReimplementation vri,
                         @Nullable String... args) {
        this.minecraftServer = minecraftServer;
        this.serverProperties = getOrGenerateServerProperties();
        this.vri = vri;


        // Register all dimension types before making the worlds:
        for (DimensionType dimension : VanillaDimensionTypes.values()) {
            vri.process().dimension().addDimension(dimension);
        }

        this.overworld = vri.createInstance(NamespaceID.from("world"), VanillaDimensionTypes.OVERWORLD);

        // Try to get server properties

        // Set up raycasting lib
        RayFastManager.init();

        EventNode<Event> eventHandler = MinecraftServer.getGlobalEventHandler();
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();

        vri.process().eventHandler()
                .addListener(AsyncPlayerConfigurationEvent.class, event -> {
                    event.setSpawningInstance(overworld);
                    event.getPlayer().setGameMode(GameMode.SPECTATOR);
                    overworld.loadChunk(0, 0).join();
                    // TODO: Find the first block that is not air
                    int y = overworld.getDimensionType().getMaxY();
                    while (Block.AIR.compare(overworld.getBlock(0, y, 0))) {
                        y--;
                        if (y == overworld.getDimensionType().getMinY()) {
                            break;
                        }
                    }
                    event.getPlayer().setRespawnPoint(new Pos(0, y, 0));
                });

        vri.process().scheduler().scheduleNextTick(OpenToLAN::open);

        // Register systems
        {
            // dimension types

            // Events
            VanillaEvents.register(this, serverProperties, eventHandler);
        }

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            connectionManager.getOnlinePlayers().forEach(player -> {
                // TODO: Saving
                player.kick("Server is closing.");
                connectionManager.removePlayer(player.getPlayerConnection());
            });
            OpenToLAN.close();
        });

        // Preload chunks
        long start = System.nanoTime();
        int radius = MinecraftServer.getChunkViewDistance();
        int total = radius * 2 * radius * 2;
        Loading.start("Preloading " + total + " chunks");
        CompletableFuture<?>[] chunkFutures = new CompletableFuture[total];
        AtomicInteger completed = new AtomicInteger(0);
        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                int index = (x + radius) + (z + radius) * radius;
                chunkFutures[index] = overworld.loadChunk(x, z)
                        .thenRun(() -> {
                            int completedCount = completed.incrementAndGet();
                            Loading.updater().progress((double) completedCount / (double) chunkFutures.length);
                        });
            }
        }
        for (CompletableFuture<?> future : chunkFutures) {
            if (future != null) future.join();
        }
        long end = System.nanoTime();
        Loading.finish();
        Logger.debug("Chunks per second: " + (total / ((end - start) / 1e9)));

        // Debug
        if (List.of(args).contains("-debug")) {
            Logger.info("Debug mode enabled.");
            Logger.info("To disable it, remove the -debug argument");
            VanillaDebug.hook(this);
        }
    }

    private ServerProperties getOrGenerateServerProperties() {
        // TODO: Load from file correctly
        try {
            return new ServerProperties("""
                    #Minecraft server properties from a fresh 1.16.1 server
                    #Generated on Mon Jul 13 17:23:48 CEST 2020
                    spawn-protection=16
                    max-tick-time=60000
                    query.port=25565
                    generator-settings=
                    sync-chunk-writes=true
                    force-gamemode=false
                    allow-nether=true
                    enforce-whitelist=false
                    gamemode=survival
                    broadcast-console-to-ops=true
                    enable-query=false
                    player-idle-timeout=0
                    difficulty=easy
                    broadcast-rcon-to-ops=true
                    spawn-monsters=true
                    op-permission-level=4
                    pvp=true
                    entity-broadcast-range-percentage=100
                    snooper-enabled=true
                    level-type=default
                    enable-status=true
                    hardcore=false
                    enable-command-block=false
                    max-players=20
                    network-compression-threshold=256
                    max-world-size=29999984
                    resource-pack-sha1=
                    function-permission-level=2
                    rcon.port=25575
                    server-port=25565
                    server-ip=
                    spawn-npcs=true
                    allow-flight=false
                    level-name=world
                    view-distance=10
                    resource-pack=
                    spawn-animals=true
                    white-list=false
                    rcon.password=
                    generate-structures=true
                    online-mode=false
                    max-build-height=256
                    level-seed=
                    prevent-proxy-connections=false
                    use-native-transport=true
                    enable-jmx-monitoring=false
                    motd=A Minecraft Server
                    enable-rcon=false
                    """);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public void start(String address, int port) {
        minecraftServer.start(address, port);
    }

    public VanillaReimplementation vri() {
        return vri;
    }

    public Instance overworld() {
        return overworld;
    }
}
