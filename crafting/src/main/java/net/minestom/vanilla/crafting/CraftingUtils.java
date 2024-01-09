package net.minestom.vanilla.crafting;

import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackUtils;
import net.minestom.vanilla.datapack.recipe.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public record CraftingUtils(Datapack datapack) {
    boolean recipeMatchesShapeless(Recipe.Shapeless recipe, Collection<Material> slots) {
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

    boolean recipeMatchesShapedNxN(Recipe.Shaped recipe, Map<Slot, Material> materials, int n) {
        Map<Slot, Recipe.Ingredient> ingredients = getSlotIngredientMap(recipe);

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

        int rowsSpace = maxRow - minRow + 1;
        int colsSpace = maxCol - minCol + 1;

        // for each slot, check if the recipe will fit in it using Slot#tryGetSpace
        for (Slot slot : Slot.craftingNxN(n)) {
            if (!slot.hasSpaceNxN(rowsSpace, colsSpace, n)) continue;

            Map<Slot, Material> translatedMaterials = new HashMap<>();
            for (var entry : materials.entrySet()) {
                var matSlot = entry.getKey();
                var mat = entry.getValue();

                var translatedKey = matSlot.translate(slot);
                if (translatedKey == null) {
                    // if we are removing a material by translating it, we can't continue with this translation
                    if (mat != Material.AIR) {
                        translatedMaterials = null;
                        break;
                    }
                    continue;
                }
                translatedMaterials.put(translatedKey, mat);
            }

            if (translatedMaterials == null) continue;
            translatedMaterials = Collections.unmodifiableMap(translatedMaterials);

            if (tryMatch3x3(translatedMaterials, ingredients)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    private static Map<Slot, Recipe.Ingredient> getSlotIngredientMap(Recipe.Shaped recipe) {
        var key = recipe.key();
        var pattern = recipe.pattern();

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
        return Collections.unmodifiableMap(ingredients);
    }

    private @Nullable Material materialsContainsIngredients(Recipe.Ingredient ingredient, Set<Material> materials) {
        return ingredientToMaterials(ingredient)
                .stream()
                .filter(materials::contains)
                .findFirst()
                .orElse(null);
    }

    // tries to match the given slot materials to the given ingredients
    private boolean tryMatch3x3(Map<Slot, Material> materials, Map<Slot, Recipe.Ingredient> ingredients) {
        System.out.println("Materials: " + materials);
        System.out.println("Ingredients: " + ingredients);
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

    public @NotNull Set<Material> ingredientToMaterials(Recipe.Ingredient ingredient) {
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
