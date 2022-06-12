package net.minestom.vanilla.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.network.ConnectionManager;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.blocks.update.BlockUpdateManager;
import net.minestom.vanilla.commands.VanillaCommands;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;
import net.minestom.vanilla.instance.tickets.TicketManager;
import net.minestom.vanilla.items.ItemManager;
import net.minestom.vanilla.items.VanillaItems;
import net.minestom.vanilla.system.RayFastManager;
import net.minestom.vanilla.system.ServerProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class VanillaServer {

    /**
     * A standard vanilla server launch used for testing purposes
     *
     * @param args arguments passed from console
     */
    public static void main(String[] args) {
        VanillaServer vanillaServer = new VanillaServer(MinecraftServer.init(), args);
        vanillaServer.start("0.0.0.0", 25565);
    }

    private final MinecraftServer minecraftServer;
    private final @NotNull ItemManager itemManager;
    private final @NotNull ServerProperties serverProperties;

    public VanillaServer(@NotNull MinecraftServer minecraftServer, @Nullable String... args) {
        this.minecraftServer = minecraftServer;
        this.serverProperties = getOrGenerateServerProperties();

        // Try to get server properties


        // Set up raycasting lib
        RayFastManager.init();


        EventNode<Event> eventHandler = MinecraftServer.getGlobalEventHandler();
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
        CommandManager commandManager = MinecraftServer.getCommandManager();

        // Register systems
        {
            // dimension types
            VanillaDimensionTypes.registerAll(MinecraftServer.getDimensionTypeManager());

            // block update managers
            BlockUpdateManager.init(eventHandler);

            // Events
            VanillaEvents.register(serverProperties, eventHandler);

            // commands
            VanillaCommands.registerAll(commandManager);

            // item handlers
            itemManager = ItemManager.accumulate(accumulator -> {
                for (VanillaItems item : VanillaItems.values()) {
                    accumulator.accumulate(item.getMaterial(), item.getItemHandlerSupplier().get());
                }
            });
            itemManager.registerEvents(eventHandler);

            // blocks
            VanillaBlocks.registerAll(eventHandler);

            // chunk tickets
            TicketManager.init(eventHandler);
        }
//        CommandManager commandManager = MinecraftServer.getCommandManager();
//        VanillaWorldgen.prepareFiles();
//        VanillaWorldgen.registerAllBiomes(MinecraftServer.getBiomeManager());
//        VanillaCommands.registerAll(commandManager);
//        VanillaItems.registerAll(MinecraftServer.getConnectionManager());
//        NetherPortal.registerData(MinecraftServer.getDataManager());
        // LootTableManager lootTableManager = MinecraftServer.getLootTableManager();
        // VanillaLootTables.register(lootTableManager);

//        MinecraftServer.getStorageManager().defineDefaultStorageSystem(FileSystemStorage::new);

//        ServerProperties properties = new ServerProperties(new File(".", "server.properties"));
//        PlayerInit.init(properties);


        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> connectionManager.getOnlinePlayers().forEach(player -> {
            // TODO: Saving
            player.kick("Server is closing.");
            connectionManager.removePlayer(player.getPlayerConnection());
        }));
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
                    online-mode=true
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

    public @NotNull ItemManager getItemManager() {
        return itemManager;
    }
}
