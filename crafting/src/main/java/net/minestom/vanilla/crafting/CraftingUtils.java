package net.minestom.vanilla.crafting;

import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackUtils;
import net.minestom.vanilla.datapack.recipe.Recipe;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

record CraftingUtils(Datapack datapack) {
    boolean recipeMatchesShapeless(Recipe.Shapeless recipe, List<Material> slots) {
        Map<Material, Integer> materials = new HashMap<>();
        slots.forEach(mat -> materials.put(mat, materials.getOrDefault(mat, 0) + 1));

        return recipe.ingredients()
                .list()
                .stream()
                .allMatch(ingredient -> {
                    Material match = materialsContainsIngredients(ingredient, materials.keySet());
                    if (match == null) return false;
                    materials.put(match, materials.get(match) - 1);
                    if (materials.get(match) == 0) materials.remove(match);
                    return true;
                }) && materials.keySet().stream().allMatch(Material.AIR::equals);
    }

    private @Nullable Material materialsContainsIngredients(Recipe.Ingredient ingredient, Set<Material> materials) {
        return ingredientToMaterials(ingredient)
                .stream()
                .filter(materials::contains)
                .findFirst()
                .orElse(null);
    }

    boolean recipeMatchesShaped2x2(Recipe.Shaped recipe, List<Material> slots) {
        boolean isStick = Material.STICK.equals(recipe.result().item());
        var key = recipe.key();
        var pattern = recipe.pattern();

        // collect materials for pattern matching
        Map<Slot, Material> materials = Map.of(
                Slot.TOP_LEFT, slots.get(0),
                Slot.TOP_MID, slots.get(1),
                Slot.MID_LEFT, slots.get(2),
                Slot.MID_MID, slots.get(3)
        );

        if (isStick) Logger.info("Materials: " + materials);

        Map<Slot, Recipe.Ingredient> ingredients = new HashMap<>();
        for (int row = 0; row < pattern.size(); row++) {
            String rowStr = pattern.get(row);
            for (int col = 0; col < rowStr.length(); col++) {
                char c = rowStr.charAt(col);
                Recipe.Ingredient ingredient = key.get(c);
                if (ingredient == null) continue;
                Slot slot = Slot.from(row, col);
                if (slot == null) continue;
                ingredients.put(slot, ingredient);
            }
        }
        ingredients = Collections.unmodifiableMap(ingredients);

        if (isStick) Logger.info("Ingredients: " + ingredients);

        // find the shape of the ingredients
        int minRow = ingredients.keySet().stream()
                .mapToInt(Slot::row)
                .min()
                .orElseThrow();
        int maxRow = ingredients.keySet().stream()
                .mapToInt(Slot::row)
                .max()
                .orElseThrow();

        int minCol = ingredients.keySet().stream()
                .mapToInt(Slot::col)
                .min()
                .orElseThrow();
        int maxCol = ingredients.keySet().stream()
                .mapToInt(Slot::col)
                .max()
                .orElseThrow();

        if (isStick) Logger.info("minRow: " + minRow + ", maxRow: " + maxRow + ", minCol: " + minCol + ", maxCol: " + maxCol);

        int rowsSpace = maxRow - minRow + 1;
        int colsSpace = maxCol - minCol + 1;

        // for each 2x2 slot, check if the recipe will fit in it using Slot#tryGetSpace2x2
        for (Slot slot : Slot.crafting2x2()) {
            if (isStick) Logger.info("testing slot space" + slot);
            if (!slot.tryGetSpace2x2(rowsSpace, colsSpace)) continue;
            if (isStick) Logger.info("slot space " + slot + " passed");
            Map<Slot, Material> translatedMaterials = materials.entrySet().stream()
                    .map(entry -> {
                        var translatedKey = entry.getKey().translate(slot);
                        if (translatedKey == null) return null;
                        return Map.entry(translatedKey, entry.getValue());
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (isStick) Logger.info("translatedMaterials: " + translatedMaterials);

            if (tryMatch3x3(translatedMaterials, ingredients)) return true;
            if (isStick) Logger.info("slot " + slot + " failed to match");
        }

        return false;
    }

    // tries to match the given slot materials to the given ingredients
    private boolean tryMatch3x3(Map<Slot, Material> materials, Map<Slot, Recipe.Ingredient> ingredients) {
        Set<Slot> usedItemStacks = new HashSet<>();
        for (var entry : ingredients.entrySet()) {
            Slot slot = entry.getKey();
            Recipe.Ingredient ingredient = entry.getValue();

            Material material = materials.get(slot);
            if (material == null) return false;
            if (!ingredientToMaterials(ingredient).contains(material)) return false;
            usedItemStacks.add(slot);
        }

        return usedItemStacks.size() == materials.values()
                .stream()
                .filter(item -> !Material.AIR.equals(item)) // only count non-air items
                .count();
    }

    private @NotNull Set<Material> ingredientToMaterials(Recipe.Ingredient ingredient) {
        if (ingredient instanceof Recipe.Ingredient.Tag tag) {
            return DatapackUtils.findTags(datapack, "items", tag.tag())
                    .stream()
                    .map(NamespaceID::value)
                    .map(Material::fromNamespaceId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());
        }
        if (ingredient instanceof Recipe.Ingredient.Item item) {
            return Set.of(item.item());
        }
        if (ingredient instanceof Recipe.Ingredient.None) {
            return Set.of(Material.AIR);
        }
        if (ingredient instanceof Recipe.Ingredient.Multi multi) {
            return multi.items()
                    .stream()
                    .map(this::ingredientToMaterials)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toUnmodifiableSet());
        }
        throw new UnsupportedOperationException("Unknown ingredient type " + ingredient.getClass().getName());
    }
}
