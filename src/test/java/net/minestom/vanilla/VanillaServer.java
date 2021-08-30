package net.minestom.vanilla;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.blocks.update.BlockUpdateManager;
import net.minestom.vanilla.commands.VanillaCommands;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;
import net.minestom.vanilla.instance.tickets.TicketManager;
import net.minestom.vanilla.items.ItemManager;
import net.minestom.vanilla.system.RayFastManager;
import net.minestom.vanilla.system.ServerProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.Random;

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
    private final ItemManager itemManager = new ItemManager();
    private final @NotNull ServerProperties serverProperties;

    public VanillaServer(@NotNull MinecraftServer minecraftServer, @Nullable String... args) {
        this.minecraftServer = minecraftServer;
        this.serverProperties = getOrGenerateServerProperties();

        // Try get server properties


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

            // item events
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


        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            connectionManager.getOnlinePlayers().forEach(player -> {
                // TODO: Saving
                player.kick("Server is closing.");
                connectionManager.removePlayer(player.getPlayerConnection());
            });
        });
    }

    private ServerProperties getOrGenerateServerProperties() {
        // TODO: Load from file correctly
        try {
            return new ServerProperties("#Minecraft server properties from a fresh 1.16.1 server\n" +
                    "#Generated on Mon Jul 13 17:23:48 CEST 2020\n" +
                    "spawn-protection=16\n" +
                    "max-tick-time=60000\n" +
                    "query.port=25565\n" +
                    "generator-settings=\n" +
                    "sync-chunk-writes=true\n" +
                    "force-gamemode=false\n" +
                    "allow-nether=true\n" +
                    "enforce-whitelist=false\n" +
                    "gamemode=survival\n" +
                    "broadcast-console-to-ops=true\n" +
                    "enable-query=false\n" +
                    "player-idle-timeout=0\n" +
                    "difficulty=easy\n" +
                    "broadcast-rcon-to-ops=true\n" +
                    "spawn-monsters=true\n" +
                    "op-permission-level=4\n" +
                    "pvp=true\n" +
                    "entity-broadcast-range-percentage=100\n" +
                    "snooper-enabled=true\n" +
                    "level-type=default\n" +
                    "enable-status=true\n" +
                    "hardcore=false\n" +
                    "enable-command-block=false\n" +
                    "max-players=20\n" +
                    "network-compression-threshold=256\n" +
                    "max-world-size=29999984\n" +
                    "resource-pack-sha1=\n" +
                    "function-permission-level=2\n" +
                    "rcon.port=25575\n" +
                    "server-port=25565\n" +
                    "server-ip=\n" +
                    "spawn-npcs=true\n" +
                    "allow-flight=false\n" +
                    "level-name=world\n" +
                    "view-distance=10\n" +
                    "resource-pack=\n" +
                    "spawn-animals=true\n" +
                    "white-list=false\n" +
                    "rcon.password=\n" +
                    "generate-structures=true\n" +
                    "online-mode=true\n" +
                    "max-build-height=256\n" +
                    "level-seed=\n" +
                    "prevent-proxy-connections=false\n" +
                    "use-native-transport=true\n" +
                    "enable-jmx-monitoring=false\n" +
                    "motd=A Minecraft Server\n" +
                    "enable-rcon=false\n");
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public void start(String address, int port) {
        minecraftServer.start(address, port);
    }

    public ItemManager getItemManager() {
        return itemManager;
    }
}
