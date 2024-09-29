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
import net.minestom.vanilla.events.BlastingFurnaceTickEvent;
import net.minestom.vanilla.tag.Tags;

public record BlastingInventoryRecipes(Datapack datapack, VanillaReimplementation vri) {

    public EventNode<Event> init() {
        EventNode<Event> node = EventNode.all("vri:blasting-inventory-recipes");

        SmeltingHandler smeltingHandler = new SmeltingHandler(datapack, 2,
            Tags.Blocks.Smelting.COOKING_TICKS,
            Tags.Blocks.Smelting.LAST_COOKED_ITEM,
            Tags.Blocks.Smelting.COOKING_PROGRESS,
            Views.blastFurnace().input(),
            Views.blastFurnace().output(),
            Views.blastFurnace().fuel());

        node.addListener(BlastingFurnaceTickEvent.class, event -> {
            Block block = event.getBlock();
            Inventory inventory = event.getInventory();

            ItemStack input = Views.blastFurnace().input().get(inventory, 0);
            Recipe.Blasting recipe = smeltingHandler.findRecipe(Recipe.Blasting.class, input.material());

            Block newBlock = recipe == null ?
                    smeltingHandler.handle(inventory, block, null, null) :
                    smeltingHandler.handle(inventory, block, recipe.result().id(), recipe.cookingTime());

            if (newBlock != null) {
                event.getInstance().setBlock(event.getBlockPosition(), newBlock);
            }
        });

        CraftingUtils.addOutputSlotEventHandler(node, Views.blastFurnace().output(), InventoryType.BLAST_FURNACE);

        return node;
    }
}
