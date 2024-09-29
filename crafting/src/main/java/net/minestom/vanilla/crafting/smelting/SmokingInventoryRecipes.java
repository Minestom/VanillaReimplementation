package net.minestom.vanilla.crafting.smelting;

import dev.goldenstack.window.Views;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.crafting.CraftingUtils;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.recipe.Recipe;
import net.minestom.vanilla.events.SmokerTickEvent;
import net.minestom.vanilla.tag.Tags;

public record SmokingInventoryRecipes(Datapack datapack, VanillaReimplementation vri) {

    public EventNode<Event> init() {
        EventNode<Event> node = EventNode.all("vri:smoking-inventory-recipes");

        SmeltingHandler smeltingHandler = new SmeltingHandler(datapack, 2,
            Tags.Blocks.Smelting.COOKING_TICKS,
            Tags.Blocks.Smelting.LAST_COOKED_ITEM,
            Tags.Blocks.Smelting.COOKING_PROGRESS,
            Views.smoker().input(),
            Views.smoker().output(),
            Views.smoker().fuel());

        node.addListener(SmokerTickEvent.class, event -> {
            Block block = event.getBlock();
            Inventory inventory = event.getInventory();

            ItemStack input = Views.smoker().input().get(inventory, 0);
            Recipe.Smoking recipe = smeltingHandler.findRecipe(Recipe.Smoking.class, input.material());

            Block newBlock = recipe == null ?
                    smeltingHandler.handle(inventory, block, null, null) :
                    smeltingHandler.handle(inventory, block, recipe.result().id(), recipe.cookingTime());

            if (newBlock != null) {
                event.getInstance().setBlock(event.getBlockPosition(), newBlock);
            }
        });

        CraftingUtils.addOutputSlotEventHandler(node, Views.smoker().output(), InventoryType.SMOKER);

        return node;
    }
}
