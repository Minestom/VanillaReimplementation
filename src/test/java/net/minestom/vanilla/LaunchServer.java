package net.minestom.vanilla;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerLoginEvent;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.recipe.ShapelessRecipe;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.commands.GamemodeCommand;
import net.minestom.vanilla.commands.VanillaCommands;

public class LaunchServer {

    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        BlockManager blockManager = MinecraftServer.getBlockManager();

        CommandManager commandManager = MinecraftServer.getCommandManager();
        VanillaCommands.registerAll(commandManager);
        VanillaBlocks.registerAll(MinecraftServer.getConnectionManager(), MinecraftServer.getBlockManager());

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

        minecraftServer.start("localhost", 55555, (playerConnection, responseData) -> {
            responseData.setName("1.15.2");
            responseData.setProtocol(578);
            responseData.setMaxPlayer(100);
            responseData.setOnline(MinecraftServer.getConnectionManager().getOnlinePlayers().size());
            responseData.setDescription("Test server for Minestom vanilla reimplementation");
            responseData.setFavicon("data:image/png;base64,<data>");
        });
    }

}
