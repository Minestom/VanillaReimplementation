package net.minestom.vanilla;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;
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

class VanillaServer {

    /**
     * A standard vanilla server launch used for testing purposes
     *
     * @param args arguments passed from console
     */
    public static void main(String[] args) {
        VanillaServer vanillaServer = new VanillaServer(MinecraftServer.init(), args);

        vanillaServer.start("localhost", 25565);
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

        BlockManager blockManager = MinecraftServer.getBlockManager();
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();

        // Register dimension types
        VanillaDimensionTypes.registerAll(MinecraftServer.getDimensionTypeManager());

        // Register Vanilla Events
        VanillaEvents.register(serverProperties, MinecraftServer.getGlobalEventHandler());
        // Register item events
        itemManager.registerEvents(MinecraftServer.getGlobalEventHandler());

        VanillaBlocks.registerAll(blockManager);

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
        File relativePath = new File("server.properties");

        if (relativePath.isFile()) {
            try {
                return new ServerProperties(relativePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        relativePath.setWritable(true);
        File defaultConfig = new File("server.properties.default");

        if (defaultConfig.isFile()) {
            try {
                Files.copy(defaultConfig.toPath(), relativePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return new ServerProperties(relativePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new FileNotFoundException("server.properties not found, and replacement couldn't be generated.").printStackTrace();
        System.exit(1);
        return null;
    }

    public void start(String address, int port) {
        minecraftServer.start(address, port);
    }

    public ItemManager getItemManager() {
        return itemManager;
    }
}
