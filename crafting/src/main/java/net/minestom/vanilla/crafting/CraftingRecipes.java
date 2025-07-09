package net.minestom.vanilla.crafting;

import net.goldenstack.window.InventoryView;
import net.goldenstack.window.Views;
import net.minestom.server.ServerProcess;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public record CraftingRecipes(@NotNull CraftingFeature.Recipes recipes, @NotNull ServerProcess process) {

    public EventNode<Event> init() {
        EventNode<Event> node = EventNode.all("vri:recipes-inventory");

        node.addListener(InventoryItemChangeEvent.class, event -> {
            if (event.getInventory() instanceof PlayerInventory inv) {
                var crafting = Views.player().crafting();

                InventoryView input = crafting.input();
                InventoryView.Singular output = crafting.output();

                if (!crafting.isValidExternal(event.getSlot())) return;

                Recipe.Crafting recipe = searchRecipe(2, 2, input.collect(inv));

                output.set(inv, recipe != null ? recipe.result() : ItemStack.AIR);
            } else if (event.getInventory() instanceof Inventory inv && inv.getInventoryType() == InventoryType.CRAFTING) {
                var crafting = Views.craftingTable();

                InventoryView input = crafting.input();
                InventoryView.Singular output = crafting.output();

                if (!crafting.isValidExternal(event.getSlot())) return;

                Recipe.Crafting recipe = searchRecipe(3, 3, input.collect(inv));

                output.set(inv, recipe != null ? recipe.result() : ItemStack.AIR);
            }
        }).addListener(InventoryPreClickEvent.class, addOutputSlot(
                inv -> inv instanceof PlayerInventory,
                        Views.player().crafting().input(),
                        Views.player().crafting().output()
                ))
        .addListener(InventoryPreClickEvent.class, addOutputSlot(
                inv -> inv instanceof Inventory crafting && crafting.getInventoryType() == InventoryType.CRAFTING,
                Views.craftingTable().input(),
                Views.craftingTable().output()
        ));

        return node;
    }

    private static @NotNull Consumer<InventoryPreClickEvent> addOutputSlot(@NotNull Predicate<AbstractInventory> predicate, @NotNull InventoryView input, @NotNull InventoryView.Singular output) {
        return event -> {
            final AbstractInventory inv = event.getInventory();

            if (!predicate.test(inv)) return;
            if (!output.isValidExternal(event.getSlot())) return;

            final ItemStack clicked = output.get(inv);

            if (clicked.isAir()) {
                event.setCancelled(true);
                return;
            }

            final PlayerInventory playerInv = event.getPlayer().getInventory();
            final ItemStack cursor = playerInv.getCursorItem();

            event.setCancelled(true);

            if (clicked.isSimilar(cursor)) {
                if (clicked.amount() + cursor.amount() > cursor.maxStackSize()) {
                    return;
                }

                playerInv.setCursorItem(cursor.withAmount(cursor.amount() + clicked.amount()));
            } else if (cursor.isAir()) {
                playerInv.setCursorItem(clicked);
            } else {
                return;
            }

            output.set(inv, ItemStack.AIR);

            for (int i = 0; i < input.size(); i++) {
                input.set(inv, i, input.get(inv, i).consume(1));
            }
        };
    }

    public @Nullable Recipe.Crafting searchRecipe(int width, int height, @NotNull List<ItemStack> ingredients) {
        final CraftingFeature.Recipes.Crafting crafting = recipes.crafting();

        List<ItemStack> nonAir = new ArrayList<>();
        for (ItemStack item : ingredients) if (!item.isAir()) nonAir.add(item);

        // Try shapeless
        for (Recipe.Crafting.Shapeless shapeless : crafting.shapeless().values()) {
            if (tryShapeless(shapeless, nonAir)) return shapeless;
        }

        // Try shaped
        for (Recipe.Crafting.Shaped shaped : crafting.shaped().values()) {
            if (tryShaped(shaped, width, height, ingredients)) return shaped;
        }

        // Try transmute

        if (nonAir.size() == 2) {
            for (Recipe.Crafting.Transmute transmute : crafting.transmute().values()) {
                if (tryTransmute(transmute, nonAir.getFirst(), nonAir.get(1))) return transmute;
            }
        }

        // TODO: Implement special recipes
//        for (Recipe recipe : recipes.special().values()) {
//
//        }

        return null;
    }

    public boolean tryShapeless(@NotNull Recipe.Crafting.Shapeless recipe, @NotNull List<ItemStack> ingredients) {
        if (recipe.ingredients().size() != ingredients.size()) return false;

        // Taking the first valid one strategically has potentially invalid behaviour if we, for example, take an entire
        // tag instead of a single item, potentially invalidating an otherwise valid recipe had an individual material
        // been chosen.
        // We amend this by taking the smallest item first, in terms of the number of options. This is technically not
        // correct, but should be in enough cases. The correct solution for this is probably some variant of the
        // knapsack problem, but that might be too much effort for this.
        // This algorithm should have time complexity O(n^2), but n is always microscopic (n <= 9) so it doesn't really
        // matter here.

        boolean[] used = new boolean[ingredients.size()];

        // For each material...
        for (ItemStack item : ingredients) {
            int indexOfSmallest = -1;

            // Find the smallest unused tag that fulfills this material
            for (int index = 0; index < ingredients.size(); index++) {
                RegistryTag<Material> current = recipe.ingredients().get(index);

                // Only pick ones that fulfill this one and aren't already used
                if (used[index] || !current.contains(item.material())) continue;

                if (indexOfSmallest == -1 || current.size() < recipe.ingredients().get(indexOfSmallest).size()) {
                    indexOfSmallest = index;
                }

                // Quick exit if smallest
                if (current.size() == 1) break;
            }

            // If there's nothing for this material, invalid.
            if (indexOfSmallest == -1) return false;

            // Mark it as true so it isn't reused.
            used[indexOfSmallest] = true;
        }

        return true;
    }

    /**
     * Tries a shaped recipe. Assumes that {@code materials} has a length of {@code width * height}.
     */
    public boolean tryShaped(@NotNull Recipe.Crafting.Shaped shaped, int width, int height, @NotNull List<ItemStack> ingredients) {
        // First, find the size of the recipe inside the grid.
        int minCol = Integer.MAX_VALUE;
        int minRow = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;
        int maxRow = Integer.MIN_VALUE;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (ingredients.get(row * height + col).isAir()) continue;

                if (row < minRow) minRow = row;
                if (col < minCol) minCol = col;
                if (row > maxRow) maxRow = row;
                if (col > maxCol) maxCol = col;
            }
        }

        // There are no items in the grid if anything is still default, but if there's any value at all, it will set all
        // four values, so we only need to check one.
        if (minCol == Integer.MAX_VALUE) return false;

        final int shapeWidth = maxCol - minCol + 1;
        final int shapeHeight = maxRow - minRow + 1;

        if (shapeHeight != shaped.pattern().size()) return false;
        if (shapeWidth != shaped.pattern().getFirst().length()) return false;

        return tryShapedInPosition(shaped, ingredients, width, height, minRow, minCol);
    }

    // Assumes the shaped recipe can fit in the material grid and that nothing has 0 size.
    private boolean tryShapedInPosition(@NotNull Recipe.Crafting.Shaped shaped, @NotNull List<ItemStack> ingredients, int width, int height, int startRow, int startCol) {
        for (int row = 0; row < shaped.pattern().size(); row++) {
            final String rowPattern = shaped.pattern().get(row);

            for (int col = 0; col < rowPattern.length(); col++) {
                final char charKey = rowPattern.charAt(col);
                final Material existing = ingredients.get((row + startRow) * height + (col + startCol)).material();

                final String key = String.valueOf(charKey);
                if (!shaped.key().getOrDefault(key, RegistryTag.direct(Material.AIR)).contains(existing)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Tries a transmute recipe.
     */
    public boolean tryTransmute(@NotNull Recipe.Crafting.Transmute transmute, @NotNull ItemStack first, @NotNull ItemStack second) {
        return (transmute.input().contains(first.material()) && transmute.material().contains(second.material()))
                || (transmute.material().contains(first.material()) && transmute.input().contains(second.material()));
    }
}

