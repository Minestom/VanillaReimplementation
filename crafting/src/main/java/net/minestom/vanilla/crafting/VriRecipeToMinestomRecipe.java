package net.minestom.vanilla.crafting;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import net.minestom.server.recipe.*;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackUtils;
import net.minestom.vanilla.datapack.recipe.Recipe;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;

record VriRecipeToMinestomRecipe(Datapack datapack) {

    private <V extends Recipe> net.minestom.server.recipe.Recipe use(Recipe recipe, Function<V, net.minestom.server.recipe.Recipe> action) {
        //noinspection unchecked
        return action.apply((V) recipe);
    }

    public net.minestom.server.recipe.Recipe convert(String id, Recipe vr, Predicate<Player> shouldShow) {
        String group = Objects.requireNonNullElseGet(vr.group(), () -> "core" + ThreadLocalRandom.current().nextInt());

        return switch (vr.type().value()) {
            case "blasting" -> use(vr, (Recipe.Blasting recipe) -> new BlastingRecipe(id, group, RecipeCategory.Cooking.MISC, ItemStack.of(recipe.result().id()), (float) recipe.experience(), recipe.cookingTime() == null ? 100 : recipe.cookingTime()) {
                {
                    this.setIngredient(toMinestom(recipe.ingredient()));
                }
                @Override
                public boolean shouldShow(@NotNull Player player) {
                    return shouldShow.test(player);
                }
            });
            case "campfire_cooking" -> use(vr, (Recipe.CampfireCooking recipe) -> new CampfireCookingRecipe(id, group, RecipeCategory.Cooking.MISC, toItemstack(recipe.result().id()), (float) recipe.experience(), recipe.cookingTime() == null ? 100 : recipe.cookingTime()) {
                {
                    this.setIngredient(toMinestom(recipe.ingredient()));
                }
                @Override
                public boolean shouldShow(@NotNull Player player) {
                    return shouldShow.test(player);
                }
            });
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

                List<DeclareRecipesPacket.Ingredient> ingredients = new ArrayList<>(rowCount * colCount);
                for (int row = 0; row < rowCount; row++) {
                    for (int col = 0; col < colCount; col++) {
                        // int index = row * rowPattern.length() + col;
                        int slotIndex = (minRow + row) * rowCount + (minCol + col);
                        var ingredient = pos2ingredient.getOrDefault(slotIndex, new Recipe.Ingredient.None());
                        ingredients.add(new DeclareRecipesPacket.Ingredient(toMinestom(ingredient)));
                    }
                }

                return new ShapedRecipe(id, colCount, rowCount, group, RecipeCategory.Crafting.MISC, ingredients, toItemstack(recipe.result()), true) {
                    @Override
                    public boolean shouldShow(@NotNull Player player) {
                        return shouldShow.test(player);
                    }
                };
            });
            case "crafting_shapeless" -> use(vr, (Recipe.Shapeless recipe) -> {
                var ingredients = recipe.ingredients()
                        .list()
                        .stream()
                        .map(this::toMinestom)
                        .map(DeclareRecipesPacket.Ingredient::new)
                        .toList();
                return new ShapelessRecipe(id, group, RecipeCategory.Crafting.MISC, ingredients, toItemstack(recipe.result())) {
                    @Override
                    public boolean shouldShow(@NotNull Player player) {
                        return shouldShow.test(player);
                    }
                };
            });
            case "smelting" -> use(vr, (Recipe.Smelting recipe) -> new SmeltingRecipe(id, group, RecipeCategory.Cooking.MISC, toItemstack(recipe.result().id()), (float) recipe.experience(), recipe.cookingTime() == null ? 100 : recipe.cookingTime()) {
                {
                    this.setIngredient(toMinestom(recipe.ingredient()));
                }
                @Override
                public boolean shouldShow(@NotNull Player player) {
                    return shouldShow.test(player);
                }
            });
            case "smoking" -> use(vr, (Recipe.Smoking recipe) -> new SmokingRecipe(id, group, RecipeCategory.Cooking.MISC, toItemstack(recipe.result().id()), (float) recipe.experience(), recipe.cookingTime() == null ? 100 : recipe.cookingTime()) {
                {
                    this.setIngredient(toMinestom(recipe.ingredient()));
                }
                @Override
                public boolean shouldShow(@NotNull Player player) {
                    return shouldShow.test(player);
                }
            });
            case "stonecutting" -> use(vr, (Recipe.Stonecutting recipe) -> new StonecutterRecipe(id, group, toMinestom(recipe.ingredient()), ItemStack.of(recipe.result().id(), recipe.result().count())) {
                @Override
                public boolean shouldShow(@NotNull Player player) {
                    return shouldShow.test(player);
                }
            });
            case "smithing_transform" -> use(vr, (Recipe.SmithingTransform recipe) -> new SmithingTransformRecipe(id, toMinestomIngredient(recipe.template()), toMinestomIngredient(recipe.base()), toMinestomIngredient(recipe.addition()), toItemstack(recipe.result())) {
                @Override
                public boolean shouldShow(@NotNull Player player) {
                    return shouldShow.test(player);
                }
            });
            case "smithing_trim" -> use(vr, (Recipe.SmithingTrim recipe) -> new SmithingTrimRecipe(id, toMinestomIngredient(recipe.template()), toMinestomIngredient(recipe.base()), toMinestomIngredient(recipe.addition())) {
                @Override
                public boolean shouldShow(@NotNull Player player) {
                    return shouldShow.test(player);
                }
            });
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

    private ItemStack toItemstack(Recipe.Result result) {
        return ItemStack.of(result.id(), result.count() == null ? 1 : result.count());
    }

    private ItemStack toItemstack(Material result) {
        return ItemStack.of(result);
    }

    public DeclareRecipesPacket.@NotNull Ingredient toMinestom(List<Recipe.Ingredient> ingredients) {
        if (ingredients.isEmpty()) {
            return new DeclareRecipesPacket.Ingredient((List<ItemStack>) null);
        }

        return new DeclareRecipesPacket.Ingredient(ingredients.stream()
                .map(this::toMinestom)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .toList());
    }

    public DeclareRecipesPacket.Ingredient toMinestomIngredient(Recipe.Ingredient ingredient) {
        return new DeclareRecipesPacket.Ingredient(toMinestom(ingredient));
    }

    public @Nullable List<ItemStack> toMinestom(@Nullable Recipe.Ingredient ingredient) {
        if (ingredient instanceof Recipe.Ingredient.Item item) {
            return List.of(toItemstack(item.item()));
        } else if (ingredient instanceof Recipe.Ingredient.Tag tag) {
            return DatapackUtils.findTags(datapack, "item", tag.tag()).stream()
                    .map(NamespaceID::from)
                    .map(Material::fromNamespaceId)
                    .filter(Objects::nonNull)
                    .map(ItemStack::of)
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
