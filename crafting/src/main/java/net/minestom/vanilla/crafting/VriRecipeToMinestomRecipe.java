package net.minestom.vanilla.crafting;

import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.recipe.Ingredient;
import net.minestom.server.recipe.RecipeBookCategory;
import net.minestom.server.recipe.RecipeProperty;
import net.minestom.server.recipe.display.RecipeDisplay;
import net.minestom.server.recipe.display.SlotDisplay;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackUtils;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.recipe.Recipe;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

record VriRecipeToMinestomRecipe(Datapack datapack) {

    private <V extends Recipe> net.minestom.server.recipe.Recipe use(Recipe recipe, Function<V, net.minestom.server.recipe.Recipe> action) {
        //noinspection unchecked
        return action.apply((V) recipe);
    }

    private record MinestomRecipeImpl(
            RecipeDisplay recipeDisplay,
            String group,
            RecipeBookCategory category,
            List<Ingredient> craftingRequirements
    ) implements net.minestom.server.recipe.Recipe {
        @Override
        public @NotNull List<RecipeDisplay> createRecipeDisplays() {
            return List.of(recipeDisplay);
        }

        @Override
        public @NotNull Map<RecipeProperty, List<Material>> itemProperties() {
            return net.minestom.server.recipe.Recipe.super.itemProperties(); // TODO
        }

        @Override
        public @Nullable String recipeBookGroup() {
            return group;
        }

        @Override
        public @Nullable RecipeBookCategory recipeBookCategory() {
            return category;
        }

        @Override
        public @Nullable List<Ingredient> craftingRequirements() {
            return craftingRequirements;
        }
    }

    public net.minestom.server.recipe.Recipe convert(String id, Recipe vr) {
        String group = Objects.requireNonNullElseGet(vr.group(), () -> "core" + ThreadLocalRandom.current().nextInt());

        return switch (vr.type().value()) {
            case "blasting" -> use(vr, (Recipe.Blasting recipe) -> new MinestomRecipeImpl(
                    new RecipeDisplay.Furnace(
                            slotDisplayOfFirst(recipe.ingredient()),
                            SlotDisplay.AnyFuel.INSTANCE,
                            new SlotDisplay.Item(recipe.result().id()),
                            new SlotDisplay.Item(Material.BLAST_FURNACE),
                            recipe.cookingTime(),
                            (float) recipe.experience()
                    ),
                    group,
                    recipe.category().equals("blocks") ? RecipeBookCategory.BLAST_FURNACE_BLOCKS : RecipeBookCategory.BLAST_FURNACE_MISC,
                    toMinestomIngredientList(recipe.ingredient())
            ));
            case "campfire_cooking" -> use(vr, (Recipe.CampfireCooking recipe) -> new MinestomRecipeImpl(
                    new RecipeDisplay.Furnace(
                            slotDisplayOfFirst(recipe.ingredient()),
                            SlotDisplay.AnyFuel.INSTANCE,
                            new SlotDisplay.Item(recipe.result().id()),
                            new SlotDisplay.Item(Material.CAMPFIRE),
                            recipe.cookingTime(),
                            (float) recipe.experience()
                    ),
                    group,
                    RecipeBookCategory.CAMPFIRE,
                    toMinestomIngredientList(recipe.ingredient())
            ));
            case "crafting_shaped" -> use(vr, (Recipe.Shaped recipe) -> {
                int minRow = Integer.MAX_VALUE;
                int maxRow = Integer.MIN_VALUE;

                int minCol = Integer.MAX_VALUE;
                int maxCol = Integer.MIN_VALUE;

                List<String> pattern = recipe.pattern();
                Map<Character, Recipe.Ingredient> key = recipe.key();

                Map<Integer, Recipe.Ingredient> pos2ingredient = new HashMap<>();

                for (int row = 0; row < pattern.size(); row++) {
                    String rowPattern = pattern.get(row);
                    for (int col = 0; col < rowPattern.length(); col++) {

                        char c = rowPattern.charAt(col);
                        int index = row * rowPattern.length() + col;
                        pos2ingredient.put(index, key.get(c));

                        if (!key.containsKey(c)) {
                            continue;
                        }

                        Recipe.Ingredient ingredient = key.get(c);
                        if (ingredient instanceof Recipe.Ingredient.None) {
                            continue;
                        }

                        minRow = Math.min(minRow, row);
                        maxRow = Math.max(maxRow, row);
                        minCol = Math.min(minCol, col);
                        maxCol = Math.max(maxCol, col);
                    }
                }

                int colCount = maxCol - minCol + 1;
                int rowCount = maxRow - minRow + 1;

                List<Ingredient> ingredients = new ArrayList<>(rowCount * colCount);
                for (int row = 0; row < rowCount; row++) {
                    for (int col = 0; col < colCount; col++) {
                        // int index = row * rowPattern.length() + col;
                        int slotIndex = (minRow + row) * colCount + (minCol + col);
                        var ingredient = pos2ingredient.getOrDefault(slotIndex, new Recipe.Ingredient.None());
                        List<Material> materials = toMinestom(ingredient);
                        if (materials == null || materials.isEmpty()) ingredients.add(null);
                        else ingredients.add(toMinestomIngredient(ingredient));
                    }
                }

                List<SlotDisplay> slots = new ArrayList<>(ingredients.size());
                for (Ingredient ingredient : ingredients) {
                    if (ingredient == null) {
                        slots.add(SlotDisplay.Empty.INSTANCE);
                        continue;
                    }
                    if (ingredient.items().isEmpty()) continue;
                    if (ingredient.items().size() == 1) {
                        slots.add(new SlotDisplay.Item(ingredient.items().getFirst()));
                    } else {
                        slots.add(new SlotDisplay.Composite(
                                ingredient.items().stream().map(SlotDisplay.Item::new).collect(Collectors.toUnmodifiableList()))
                        );
                    }
                }

                return new MinestomRecipeImpl(
                        new RecipeDisplay.CraftingShaped(
                                colCount, rowCount,
                                slots,
                                slotDisplay(recipe.result()),
                                new SlotDisplay.Item(Material.CRAFTING_TABLE)
                        ),
                        group,
                        switch (recipe.category()) {
                            case "equipment" -> RecipeBookCategory.CRAFTING_EQUIPMENT;
                            case "building" -> RecipeBookCategory.CRAFTING_BUILDING_BLOCKS;
                            case "redstone" -> RecipeBookCategory.CRAFTING_REDSTONE;
                            default -> RecipeBookCategory.CRAFTING_MISC;
                        },
                        ingredients.stream().filter(Objects::nonNull).toList()
                );
            });

            case "crafting_shapeless" -> use(vr, (Recipe.Shapeless recipe) -> new MinestomRecipeImpl(
                    new RecipeDisplay.CraftingShapeless(
                            recipe.ingredients().stream().map(this::slotDisplay).toList(),
                            slotDisplay(recipe.result()),
                            new SlotDisplay.Item(Material.CRAFTING_TABLE)
                    ),
                    group,
                    switch (recipe.category()) {
                        case "equipment" -> RecipeBookCategory.CRAFTING_EQUIPMENT;
                        case "building" -> RecipeBookCategory.CRAFTING_BUILDING_BLOCKS;
                        case "redstone" -> RecipeBookCategory.CRAFTING_REDSTONE;
                        default -> RecipeBookCategory.CRAFTING_MISC;
                    },
                    toMinestomIngredientList(recipe.ingredients())
            ));

            case "crafting_transmute" -> use(vr, (Recipe.Transmute recipe) -> new MinestomRecipeImpl(
                    new RecipeDisplay.CraftingShapeless(
                            List.of(slotDisplayOfFirst(recipe.material()), slotDisplayOfFirst(recipe.input())),
                            slotDisplay(recipe.result()),
                            new SlotDisplay.Item(Material.CRAFTING_TABLE)
                    ),
                    group,
                    switch (recipe.category()) {
                        case "equipment" -> RecipeBookCategory.CRAFTING_EQUIPMENT;
                        case "building" -> RecipeBookCategory.CRAFTING_BUILDING_BLOCKS;
                        case "redstone" -> RecipeBookCategory.CRAFTING_REDSTONE;
                        default -> RecipeBookCategory.CRAFTING_MISC;
                    },
                    toMinestomIngredientList(List.of(recipe.material().getFirst(), recipe.input().getFirst()))
            ));

            case "smelting" -> use(vr, (Recipe.Smelting recipe) -> new MinestomRecipeImpl(
                    new RecipeDisplay.Furnace(
                            slotDisplayOfFirst(recipe.ingredient()),
                            SlotDisplay.AnyFuel.INSTANCE,
                            new SlotDisplay.Item(recipe.result().id()),
                            new SlotDisplay.Item(Material.FURNACE),
                            recipe.cookingTime(),
                            (float) recipe.experience()
                    ),
                    group,
                    switch (recipe.category()) {
                        case "food" -> RecipeBookCategory.FURNACE_FOOD;
                        case "blocks" -> RecipeBookCategory.FURNACE_BLOCKS;
                        default -> RecipeBookCategory.FURNACE_MISC;
                    },
                    toMinestomIngredientList(recipe.ingredient())
            ));
            case "smoking" -> use(vr, (Recipe.Smoking recipe) -> new MinestomRecipeImpl(
                    new RecipeDisplay.Furnace(
                            slotDisplayOfFirst(recipe.ingredient()),
                            SlotDisplay.AnyFuel.INSTANCE,
                            new SlotDisplay.Item(recipe.result().id()),
                            new SlotDisplay.Item(Material.SMOKER),
                            recipe.cookingTime(),
                            (float) recipe.experience()
                    ),
                    group,
                    RecipeBookCategory.SMOKER_FOOD,
                    toMinestomIngredientList(recipe.ingredient())
            ));
            case "stonecutting" -> use(vr, (Recipe.Stonecutting recipe) -> new MinestomRecipeImpl(
                    new RecipeDisplay.Stonecutter(
                            slotDisplayOfFirst(recipe.ingredient()),
                            slotDisplay(recipe.result()),
                            new SlotDisplay.Item(Material.STONECUTTER)
                    ),
                    group,
                    RecipeBookCategory.STONECUTTER,
                    toMinestomIngredientList(recipe.ingredient())
            ));
            case "smithing_transform" -> use(vr, (Recipe.SmithingTransform recipe) -> new MinestomRecipeImpl(
                    new RecipeDisplay.Smithing(
                            slotDisplay(recipe.template()),
                            slotDisplay(recipe.base()),
                            slotDisplay(recipe.addition()),
                            slotDisplay(recipe.result()),
                            new SlotDisplay.Item(Material.STONECUTTER)
                    ),
                    group,
                    RecipeBookCategory.SMITHING,
                    toMinestomIngredientList(List.of(recipe.template(), recipe.base(), recipe.addition()))
            ));
            case "smithing_trim" -> use(vr, (Recipe.SmithingTrim recipe) -> new MinestomRecipeImpl(
                    new RecipeDisplay.Smithing(
                            slotDisplay(recipe.template()),
                            slotDisplay(recipe.base()),
                            slotDisplay(recipe.addition()),
                            SlotDisplay.Empty.INSTANCE, // TODO
                            new SlotDisplay.Item(Material.STONECUTTER)
                    ),
                    group,
                    RecipeBookCategory.SMITHING,
                    toMinestomIngredientList(List.of(recipe.template(), recipe.base(), recipe.addition()))
            ));
            case "native" -> {
                Logger.warn("Native recipes are not supported yet, skipping.");
                yield null;
            }
            default -> {
                Logger.debug("Unknown recipe type " + vr.type().value() + ", skipping.");
                yield null;
            }
        };
    }

    public List<Ingredient> toMinestomIngredientList(List<Recipe.Ingredient> ingredients) {
        if (ingredients.isEmpty()) {
            return List.of();
        }

        List<Material> materials = ingredients.stream()
                .map(this::toMinestom)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .toList();

        if (materials.isEmpty()) return List.of();

        return List.of(new Ingredient(materials));
    }

    public Ingredient toMinestomIngredient(Recipe.Ingredient ingredient) {
        return new Ingredient(toMinestom(ingredient));
    }

    public SlotDisplay slotDisplay(Recipe.Result result) {
        return new SlotDisplay.ItemStack(ItemStack.of(result.id(), result.count() != null ? result.count() : 1));
    }

    public SlotDisplay slotDisplayOfFirst(JsonUtils.SingleOrList<Recipe.Ingredient> ingredients) {
        return slotDisplay(ingredients.getFirst());
    }

    public SlotDisplay slotDisplay(@Nullable Recipe.Ingredient ingredient) {
        switch (ingredient) {
            case Recipe.Ingredient.Multi multi -> {
                return new SlotDisplay.Composite(
                        multi.items().stream().map(this::slotDisplay).toList()
                );
            }
            case Recipe.Ingredient.Single single -> {
                List<Material> materials = toMinestom(single);
                if (materials == null || materials.isEmpty()) {
                    return SlotDisplay.Empty.INSTANCE;
                }
                if (materials.size() == 1) {
                    return new SlotDisplay.Item(materials.getFirst());
                } else {
                    return new SlotDisplay.Composite(
                            materials.stream().map(SlotDisplay.Item::new).collect(Collectors.toUnmodifiableList())
                    );
                }
            }
            case null, default -> {
                return SlotDisplay.Empty.INSTANCE;
            }
        }
    }

    public @Nullable List<Material> toMinestom(@Nullable Recipe.Ingredient ingredient) {
        if (ingredient instanceof Recipe.Ingredient.Item item) {
            return List.of(item.item());
        } else if (ingredient instanceof Recipe.Ingredient.Tag tag) {
            return DatapackUtils.findTags(datapack, "item", tag.tag()).stream()
                    .map(Key::key)
                    .map(Material::fromKey)
                    .filter(Objects::nonNull)
                    .toList();
        } else if (ingredient instanceof Recipe.Ingredient.Multi multi) {
            return multi.items()
                    .stream()
                    .map(this::toMinestom)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .toList();
        } else if (ingredient instanceof Recipe.Ingredient.None) {
            return List.of();
        } else if (ingredient == null) {
            return null;
        } else {
            throw new UnsupportedOperationException("Unknown ingredient type " + ingredient.getClass().getName());
        }
    }
}
