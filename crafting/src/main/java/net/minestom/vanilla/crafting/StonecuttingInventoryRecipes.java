package net.minestom.vanilla.crafting;

import dev.goldenstack.window.InventoryView;
import dev.goldenstack.window.Views;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientClickWindowButtonPacket;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.recipe.Recipe;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record StonecuttingInventoryRecipes(Datapack datapack, VanillaReimplementation vri) {

    static final Views.Stonecutter stonecutter = Views.stonecutter();
    static final InventoryView.Singular input = stonecutter.input();
    static final InventoryView.Singular output = stonecutter.output();
    public EventNode<Event> init() {
        EventNode<Event> node = EventNode.all("vri:stone-cutting-inventory-recipes");

        // TODO: shift-click mass crafting and take out.

        node.addListener(InventoryClickEvent.class, event -> {
            int slot = event.getSlot();
            if (event.getInventory() == null) return;
            Inventory inv = event.getInventory();
            if (event.getInventory().getInventoryType() != InventoryType.STONE_CUTTER) return;

            if (input.isValidExternal(slot)) {
                if (!event.getClickedItem().material().equals(event.getCursorItem().material())) {
                    // the player has changed the input id. the output should be cleared.
                    output.set(inv, ItemStack.AIR);
                }
            }

            if (output.isValidExternal(slot)) {
                Recipe.Stonecutting recipe = getRecipe(input.get(inv).material(), event.getClickedItem().material(), event.getClickedItem().amount());
                if (recipe == null) {
                    Logger.warn("Didn't find the recipe! what's going on? this is undefined behaviour");
                    return;
                }
                input.set(inv, input.get(inv).withAmount(prev -> prev - 1));
                if (input.get(inv).amount() > 0) {
                    output.set(inv, ItemStack.of(recipe.result().id(), recipe.result().count()));
                }
            }

        });

        // this is just to remove the warnings in console about unhandled packets, this packet is actually handled in the
        // PlayerPacketEvent listener below.
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientClickWindowButtonPacket.class, (packet, player) -> {});

        node.addListener(PlayerPacketEvent.class, event -> {
            if (!(event.getPacket() instanceof ClientClickWindowButtonPacket packet)) return;
            Inventory inv = event.getPlayer().getOpenInventory();
            if (inv == null) return;
            if (inv.getInventoryType() != InventoryType.STONE_CUTTER) return;
            int index = packet.buttonId();

            var recipes = getRecipes(input.get(inv).material());
            if (index < 0 || index >= recipes.size()) return;
            Recipe.Stonecutting recipe = recipes.get(packet.buttonId());

            output.set(inv, ItemStack.of(recipe.result().id(), recipe.result().count()));
        });

        CraftingUtils.addOutputSlotEventHandler(node, Views.stonecutter().output(), InventoryType.STONE_CUTTER);

        return node;
    }
    private @NotNull List<Recipe.Stonecutting> getRecipes(Material material) {
        CraftingUtils utils = new CraftingUtils(datapack);
        List<Recipe.Stonecutting> recipes = new ArrayList<>();

        for (var entry : datapack.namespacedData().entrySet()) {
            Datapack.NamespacedData data = entry.getValue();

            for (String file : data.recipes().files()) {
                Recipe recipe = data.recipes().file(file);

                if (recipe instanceof Recipe.Stonecutting stonecutting) {
                    var ingredientMatches = stonecutting.ingredient().list().stream()
                            .flatMap(ingredient -> utils.ingredientToMaterials(ingredient).stream())
                            .anyMatch(material::equals);
                    if (ingredientMatches) {
                        recipes.add(stonecutting);
                    }
                }
            }
        }

        recipes.sort(Comparator.comparing(o -> o.result().id().name()));

        return List.copyOf(recipes);
    }

    private @Nullable Recipe.Stonecutting getRecipe(Material input, Material output, int count) {
        for (Recipe.Stonecutting recipe : getRecipes(input)) {
            if (output.equals(recipe.result().id()) && recipe.result().count() == count) {
                return recipe;
            }
        }
        return null;
    }
}
