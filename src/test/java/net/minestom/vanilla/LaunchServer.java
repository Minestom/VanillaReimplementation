package net.minestom.vanilla;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.recipe.ShapelessRecipe;
import net.minestom.server.timer.TaskRunnable;
import net.minestom.vanilla.anvil.FileSystemStorage;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.commands.VanillaCommands;
import net.minestom.vanilla.gamedata.loottables.VanillaLootTables;
import net.minestom.vanilla.items.VanillaItems;
import net.minestom.vanilla.system.NetherPortal;

public class LaunchServer {

    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        BlockManager blockManager = MinecraftServer.getBlockManager();

        CommandManager commandManager = MinecraftServer.getCommandManager();
        VanillaCommands.registerAll(commandManager);
        VanillaItems.registerAll(MinecraftServer.getConnectionManager());
        VanillaBlocks.registerAll(MinecraftServer.getConnectionManager(), MinecraftServer.getBlockManager());
        NetherPortal.registerData(MinecraftServer.getDataManager());
        LootTableManager lootTableManager = MinecraftServer.getLootTableManager();
        VanillaLootTables.register(lootTableManager);

        MinecraftServer.getStorageManager().defineDefaultStorageSystem(FileSystemStorage::new);

        PlayerInit.init();

        RecipeManager recipeManager = MinecraftServer.getRecipeManager();
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe("test", "groupname") {
            @Override
            public boolean shouldShow(Player player) {
                return true;
            }
        };
        shapelessRecipe.setResult(new ItemStack(Material.STONE, (byte) 1));
        DeclareRecipesPacket.Ingredient ingredient = new DeclareRecipesPacket.Ingredient();
        ingredient.items = new ItemStack[]{new ItemStack(Material.STONE, (byte) 3)};
        shapelessRecipe.addIngredient(ingredient);
        recipeManager.addRecipe(shapelessRecipe);

        MinecraftServer.getSchedulerManager().addShutdownTask(new TaskRunnable() {
            @Override
            public void run() {
                ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
                connectionManager.getOnlinePlayers().forEach(player -> {
                    // TODO: Saving
                    player.kick("Server is closing.");
                    connectionManager.removePlayer(player.getPlayerConnection());
                });
            }
        });

        minecraftServer.start("localhost", 55555, (playerConnection, responseData) -> {
            responseData.setName("1.16.1");
            responseData.setMaxPlayer(100);
            responseData.setOnline(MinecraftServer.getConnectionManager().getOnlinePlayers().size());
            responseData.setDescription("Test server for Minestom vanilla reimplementation");
            responseData.setFavicon("data:image/png;base64,<data>");
        });
    }

}
