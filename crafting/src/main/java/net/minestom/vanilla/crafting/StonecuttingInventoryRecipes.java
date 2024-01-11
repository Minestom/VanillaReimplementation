package net.minestom.vanilla.crafting;

import dev.goldenstack.window.InventoryView;
import dev.goldenstack.window.Views;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.StackingRule;
import net.minestom.server.network.packet.client.play.ClientClickWindowButtonPacket;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.recipe.Recipe;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;

public record StonecuttingInventoryRecipes(Datapack datapack, VanillaReimplementation vri) {

    static Views.Stonecutter stonecutter = Views.stonecutter();
    static InventoryView.Singular input = stonecutter.input();
    static InventoryView.Singular output = stonecutter.output();
    public EventNode<Event> init() {
        EventNode<Event> node = EventNode.all("vri:stone-cutting-inventory-recipes");

        // TODO: shift-click mass crafting and take out.

        node.addListener(InventoryPreClickEvent.class, event -> {
            // for stacking items from an output slot
            int slot = event.getSlot();
            if (event.getInventory() == null) return;
            if (event.getInventory().getInventoryType() != InventoryType.STONE_CUTTER) return;
            if (!output.isValidExternal(slot)) return;
            if (event.getClickedItem().isAir() || event.getCursorItem().isAir()) return;
            StackingRule stackingRule = StackingRule.get();
            if (!stackingRule.canBeStacked(event.getClickedItem(), event.getCursorItem())) return;
            if (stackingRule.getMaxSize(event.getClickedItem()) <
                    (event.getClickedItem().amount() + event.getCursorItem().amount())) {
                // cannot be merged. ignore the click
                event.setCancelled(true);
            } else {
                event.setClickedItem(event.getClickedItem().withAmount(
                        event.getClickedItem().amount() + event.getCursorItem().amount()
                ));
                event.setCursorItem(ItemStack.AIR);
            }
        });

        node.addListener(InventoryClickEvent.class, event -> {
            int slot = event.getSlot();
            if (event.getInventory() == null) return;
            Inventory inv = event.getInventory();
            if (event.getInventory().getInventoryType() != InventoryType.STONE_CUTTER) return;

            if (output.isValidExternal(slot)) {
                Recipe.Stonecutting recipe = getRecipe(input.get(inv).material(),event.getClickedItem().material(), event.getClickedItem().amount());
                if (recipe == null) {
                    Logger.warn("Didn't find the recipe! what's going on? this is undefined behaviour");
                    return;
                }
                input.set(inv, input.get(inv).withAmount(prev -> prev - 1));
                if (input.get(inv).amount() > 0) {
                    output.set(inv, ItemStack.of(recipe.result(), recipe.count()));
                }
            }

        });

        MinecraftServer.getPacketListenerManager().setListener(ClientClickWindowButtonPacket.class, (packet, player) -> {});

        node.addListener(PlayerPacketEvent.class, event -> {
            if (!(event.getPacket() instanceof ClientClickWindowButtonPacket packet)) return;
            Inventory inv = event.getPlayer().getOpenInventory();
            if (inv == null) return;
            if (inv.getInventoryType() != InventoryType.STONE_CUTTER) return;

            Recipe.Stonecutting recipe = getRecipes(input.get(inv).material()).get(packet.buttonId());

            output.set(inv, ItemStack.of(recipe.result(), recipe.count()));
        });

        return node;
    }
    private @NotNull ArrayList<Recipe.Stonecutting> getRecipes(Material material) {
        CraftingUtils utils = new CraftingUtils(datapack);
        ArrayList<Recipe.Stonecutting> recipes = new ArrayList<>();

        for (var entry : datapack.namespacedData().entrySet()) {
            Datapack.NamespacedData data = entry.getValue();

            for (String file : data.recipes().files()) {
                Recipe recipe = data.recipes().file(file);

                if (recipe instanceof Recipe.Stonecutting stonecutting) {
                    if (utils.ingredientToMaterials(stonecutting.ingredient().asObject()).contains(material)) {
                        recipes.add(stonecutting);
                    }
                }
            }
        }

        recipes.sort(Comparator.comparing(o -> o.result().name()));

        return recipes;
    }

    private @Nullable Recipe.Stonecutting getRecipe(Material input, Material output, int count) {
        for (Recipe.Stonecutting recipe : getRecipes(input)) {
            if (recipe.result() == output && recipe.count() == count) {
                return recipe;
            }
        }
        return null;
    }
}
