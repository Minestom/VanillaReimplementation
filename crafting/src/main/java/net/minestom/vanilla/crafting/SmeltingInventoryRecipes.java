package net.minestom.vanilla.crafting;

import dev.goldenstack.window.InventoryView;
import dev.goldenstack.window.Views;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.recipe.Recipe;
import net.minestom.vanilla.events.FurnaceTickEvent;
import net.minestom.vanilla.logging.Logger;
import net.minestom.vanilla.tag.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public record SmeltingInventoryRecipes(Datapack datapack, VanillaReimplementation vri) {
    public EventNode<Event> init() {
        EventNode<Event> node = EventNode.all("vri:smelting-inventory-recipes");

        node.addListener(FurnaceTickEvent.class, event -> {
            Instance instance = event.getInstance();
            Point pos = event.getBlockPosition();
            Block block = event.getBlock();
            Inventory inventory = event.getInventory();
            Views.Furnace furnace = Views.furnace();

            ItemStack input = furnace.input().get(inventory, 0);
            ItemStack fuel = furnace.fuel().get(inventory, 0);
            ItemStack output = furnace.output().get(inventory, 0);

            Recipe.Smelting recipe = findRecipe(input.material());

            if (recipe == null) return;

            if (output.material() != Material.AIR && output.material() != recipe.result()) {
                // output slot is occupied by something other than the recipe result
                return;
            }

            int recipeProgress = Objects.requireNonNullElse(recipe.cookingTime(), 100);

            int cookingProgress = block.getTag(Tags.Blocks.Furnace.COOKING_PROGRESS);
            int cookingTicks = 100; // TODO: Handle fuel countdown/consumption

            if (cookingTicks == 0) return;

            cookingProgress++;
            Logger.info("Cooking progress: " + cookingProgress + "/" + recipeProgress);

            if (cookingProgress >= recipeProgress) {
                // recipe complete
                cookingProgress = 0;
                if (output.isAir()) {
                    furnace.output().set(inventory, ItemStack.of(recipe.result()));
                } else {
                    furnace.output().set(inventory, output.withAmount(output.amount() + 1));
                }
                Logger.info("Recipe complete");
                furnace.input().set(inventory, input.withAmount(input.amount() - 1));
                inventory.update();
            }

            // update block tags
            Block newBlock = block.withTag(Tags.Blocks.Furnace.COOKING_PROGRESS, cookingProgress);
            instance.setBlock(pos, newBlock);
        });

        CraftingUtils.addOutputSlotEventHandler(node, Views.furnace().output(), InventoryType.FURNACE);

        return node;
    }

    private @Nullable Recipe.Smelting findRecipe(Material inputMat) {
        CraftingUtils utils = new CraftingUtils(datapack);
        for (var entry : datapack().namespacedData().entrySet()) {
            var data = entry.getValue();

            for (String file : data.recipes().files()) {
                Recipe recipe = data.recipes().file(file);
                if (recipe instanceof Recipe.Smelting smelting) {
                    Set<Material> inputMats = smelting.ingredient().list().stream()
                            .map(utils::ingredientToMaterials)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toUnmodifiableSet());

                    if (!inputMats.contains(inputMat)) continue;

                    return smelting;
                }
            }
        }
        return null;
    }
}
