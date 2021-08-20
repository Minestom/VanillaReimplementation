package net.minestom.vanilla;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.items.ItemManager;
import net.minestom.vanilla.system.RayFastManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

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

    public VanillaServer(@NotNull MinecraftServer minecraftServer, @Nullable String... args) {
        this.minecraftServer = minecraftServer;

        RayFastManager.init();

        BlockManager blockManager = MinecraftServer.getBlockManager();
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();

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

    public void start(String address, int port) {
        minecraftServer.start(address, port);
    }

    public ItemManager getItemManager() {
        return itemManager;
    }
}
