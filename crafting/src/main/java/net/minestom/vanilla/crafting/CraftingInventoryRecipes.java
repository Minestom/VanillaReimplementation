package net.minestom.vanilla.crafting;

import dev.goldenstack.window.InventoryView;
import dev.goldenstack.window.v1_19.Views;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.recipe.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record CraftingInventoryRecipes(Datapack datapack, VanillaReimplementation vri) {

    public EventNode<Event> init() {
        EventNode<Event> node = EventNode.all("vri:crafting-inventory-recipes");

        node.addListener(InventoryClickEvent.class, event -> {
            int slot = event.getSlot();
            if (event.getInventory() == null) return;
            Inventory inv = event.getInventory();
            if (event.getInventory().getInventoryType() != InventoryType.CRAFTING) return;

            var table = Views.CRAFTING_TABLE;

            InventoryView input = table.input();
            InventoryView.Singular output = table.output();

            // if we are clicking on the output slot, remove one from all of the input slots
            if (output.isValidExternal(slot)) {
                for (int i = 0; i < input.size(); i++) {
                    input.set(inv, i, input.get(inv, i).withAmount(prev -> prev - 1));
                }
            }

            Material topLeft = input.get(inv, 0).material();
            Material topMid = input.get(inv, 1).material();
            Material topRight = input.get(inv, 2).material();
            Material midLeft = input.get(inv, 3).material();
            Material midMid = input.get(inv, 4).material();
            Material midRight = input.get(inv, 5).material();
            Material bottomLeft = input.get(inv, 6).material();
            Material bottomMid = input.get(inv, 7).material();
            Material bottomRight = input.get(inv, 8).material();

            ItemStack result = tryRecipe(topLeft, topMid, topRight,
                    midLeft, midMid, midRight,
                    bottomLeft, bottomMid, bottomRight);

            output.set(inv, Objects.requireNonNullElse(result, ItemStack.AIR));
        });

        CraftingUtils.addOutputSlotEventHandler(node, Views.craftingTable().output(), InventoryType.CRAFTING);

        return node;
    }

    private @Nullable ItemStack tryRecipe(
            Material topLeft, Material topMid, Material topRight,
            Material midLeft, Material midMid, Material midRight,
            Material bottomLeft, Material bottomMid, Material bottomRight) {
        CraftingUtils utils = new CraftingUtils(datapack);

        Map<Slot, Material> materials = Map.of(
                Slot.TOP_LEFT, topLeft,
                Slot.TOP_MID, topMid,
                Slot.TOP_RIGHT, topRight,
                Slot.MID_LEFT, midLeft,
                Slot.MID_MID, midMid,
                Slot.MID_RIGHT, midRight,
                Slot.BOTTOM_LEFT, bottomLeft,
                Slot.BOTTOM_MID, bottomMid,
                Slot.BOTTOM_RIGHT, bottomRight
        );
        Collection<Material> materialValues = Collections.unmodifiableCollection(materials.values());

        for (var entry : datapack.namespacedData().entrySet()) {
            Datapack.NamespacedData data = entry.getValue();

            for (String file : data.recipes().files()) {
                Recipe recipe = data.recipes().file(file);

                if (recipe instanceof Recipe.Shapeless shapeless) {
                    if (utils.recipeMatchesShapeless(shapeless, materialValues)) {
                        return ItemStack.of(shapeless.result().item(), shapeless.result().count() == null ? 1 : shapeless.result().count());
                    }
                }

                if (recipe instanceof Recipe.Shaped shaped) {
                    if (utils.recipeMatchesShapedNxN(shaped, materials, 3)) {
                        return ItemStack.of(shaped.result().item(), shaped.result().count() == null ? 1 : shaped.result().count());
                    }
                }
            }
        }
        return null;
    }
}
