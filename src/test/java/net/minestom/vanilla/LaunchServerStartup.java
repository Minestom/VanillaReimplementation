package net.minestom.vanilla;

import java.io.File;
import java.io.IOException;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.vanilla.anvil.FileSystemStorage;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.commands.VanillaCommands;
import net.minestom.vanilla.gamedata.loottables.VanillaLootTables;
import net.minestom.vanilla.generation.VanillaWorldgen;
import net.minestom.vanilla.items.VanillaItems;
import net.minestom.vanilla.system.NetherPortal;
import net.minestom.vanilla.system.ServerProperties;

class LaunchServerStartup {

    public static void main(String[] args) throws IOException {
        MinecraftServer minecraftServer = MinecraftServer.init();

        // BlockManager blockManager = MinecraftServer.getBlockManager();

        CommandManager commandManager = MinecraftServer.getCommandManager();
        VanillaWorldgen.prepareFiles();
        VanillaWorldgen.registerAllBiomes(MinecraftServer.getBiomeManager());
        VanillaCommands.registerAll(commandManager);
        VanillaItems.registerAll(MinecraftServer.getConnectionManager());
        VanillaBlocks.registerAll(MinecraftServer.getConnectionManager(), MinecraftServer.getBlockManager());
        NetherPortal.registerData(MinecraftServer.getDataManager());
        LootTableManager lootTableManager = MinecraftServer.getLootTableManager();
        VanillaLootTables.register(lootTableManager);

        MinecraftServer.getStorageManager().defineDefaultStorageSystem(FileSystemStorage::new);

        ServerProperties properties = new ServerProperties(new File(".", "server.properties"));
        PlayerInit.init(properties);


        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
            connectionManager.getOnlinePlayers().forEach(player -> {
                // TODO: Saving
                player.kick("Server is closing.");
                connectionManager.removePlayer(player.getPlayerConnection());
            });
        });

        minecraftServer.start(properties.get("server-ip"), Integer.parseInt(properties.get("server-port")), (playerConnection, responseData) -> {
            responseData.setName(MinecraftServer.VERSION_NAME);
            responseData.setMaxPlayer(Integer.parseInt(properties.get("max-players")));
            responseData.setOnline(MinecraftServer.getConnectionManager().getOnlinePlayers().size());
            responseData.setDescription(properties.get("motd"));
            responseData.setFavicon("data:image/png;base64,<data>");
        });
    }
}
