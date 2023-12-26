package net.minestom.vanilla.crafting;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import net.minestom.server.recipe.*;
import net.minestom.server.tag.Tag;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

class VriRecipeToMinestomRecipe {

    private <V extends VanillaRecipe> Recipe use(VanillaRecipe recipe, Function<V, Recipe> action) {
        //noinspection unchecked
        return action.apply((V) recipe);
    }

    public Recipe convert(String id, VanillaRecipe vr, Predicate<Player> shouldShow) {
        String group = Objects.requireNonNullElseGet(vr.group(), () -> "core" + ThreadLocalRandom.current().nextInt());

        return switch (vr.type()) {
            case BLASTING -> use(vr, (VanillaRecipe.Blasting recipe) -> {
                return new BlastingRecipe(id, group, RecipeCategory.Cooking.MISC, toItemstack(recipe.result()), (float) recipe.experience(), recipe.cookingTime()) {
                    {
                        this.setIngredient(toMinestom(recipe.ingredient()));
                    }
                    @Override
                    public boolean shouldShow(@NotNull Player player) {
                        return shouldShow.test(player);
                    }
                };
            });
            case CAMPFIRE_COOKING -> use(vr, (VanillaRecipe.CampfireCooking recipe) -> {
                return new CampfireCookingRecipe(id, group, RecipeCategory.Cooking.MISC, toItemstack(recipe.result()), (float) recipe.experience(), recipe.cookingTime()) {
                    {
                        this.setIngredient(toMinestom(recipe.ingredient()));
                    }
                    @Override
                    public boolean shouldShow(@NotNull Player player) {
                        return shouldShow.test(player);
                    }
                };
            });
            case CRAFTING_SHAPED -> use(vr, (VanillaRecipe.CraftingShaped recipe) -> {
                int minRow = Integer.MAX_VALUE;
                int maxRow = Integer.MIN_VALUE;

                int minCol = Integer.MAX_VALUE;
                int maxCol = Integer.MIN_VALUE;

                for (VanillaRecipe.Slot slot : recipe.pattern().keySet()) {
                    minRow = Math.min(minRow, slot.row());
                    maxRow = Math.max(maxRow, slot.row());
                    minCol = Math.min(minCol, slot.column());
                    maxCol = Math.max(maxCol, slot.column());
                }

                int colCount = maxCol - minCol + 1;
                int rowCount = maxRow - minRow + 1;

                List<DeclareRecipesPacket.Ingredient> ingredients = new ArrayList<>(rowCount * colCount);
                for (int row = 0; row < rowCount; row++) {
                    for (int col = 0; col < colCount; col++) {
                        var absSlot = VanillaRecipe.Slot.from(minRow + row, minCol + col);
                        var ingredient = recipe.pattern().getOrDefault(absSlot, new VanillaRecipe.Ingredient.None());
                        ingredients.add(toMinestom(ingredient));
                    }
                }

                return new ShapedRecipe(id, colCount, rowCount, group, RecipeCategory.Crafting.MISC, ingredients, toItemstack(recipe.result()), true) {
                    @Override
                    public boolean shouldShow(@NotNull Player player) {
                        return shouldShow.test(player);
                    }
                };
            });
            case CRAFTING_SHAPELESS -> use(vr, (VanillaRecipe.CraftingShapeless recipe) -> {
                var ingredients = recipe.ingredients()
                        .entrySet()
                        .stream()
                        .flatMap(entry -> {
                            var ingredient = entry.getKey();
                            return Stream.generate(() -> ingredient)
                                    .limit(entry.getValue());
                        })
                        .map(this::toMinestom)
                        .toList();
                return new ShapelessRecipe(id, group, RecipeCategory.Crafting.MISC, ingredients, toItemstack(recipe.result())) {
                    @Override
                    public boolean shouldShow(@NotNull Player player) {
                        return shouldShow.test(player);
                    }
                };
            });
            case SMELTING -> use(vr, (VanillaRecipe.Smelting recipe) -> {
                return new SmeltingRecipe(id, group, RecipeCategory.Cooking.MISC, toItemstack(recipe.result()), (float) recipe.experience(), recipe.cookingTime()) {
                    {
                        this.setIngredient(toMinestom(recipe.ingredient()));
                    }
                    @Override
                    public boolean shouldShow(@NotNull Player player) {
                        return shouldShow.test(player);
                    }
                };
            });
            case SMOKING -> use(vr, (VanillaRecipe.Smoking recipe) -> {
                return new SmokingRecipe(id, group, RecipeCategory.Cooking.MISC, toItemstack(recipe.result()), (float) recipe.experience(), recipe.cookingTime()) {
                    {
                        this.setIngredient(toMinestom(recipe.ingredient()));
                    }
                    @Override
                    public boolean shouldShow(@NotNull Player player) {
                        return shouldShow.test(player);
                    }
                };
            });
            case STONECUTTING -> use(vr, (VanillaRecipe.Stonecutting recipe) -> {
                return new StonecutterRecipe(id, group, toMinestom(recipe.ingredients()), toItemstack(recipe.result())) {
                    @Override
                    public boolean shouldShow(@NotNull Player player) {
                        return shouldShow.test(player);
                    }
                };
            });
            case SMITHING_TRANSFORM -> use(vr, (VanillaRecipe.SmithingTransform recipe) -> {
                return new SmithingTransformRecipe(id, toMinestom(recipe.template()), toMinestom(recipe.base()), toMinestom(recipe.addition()), toItemstack(recipe.result())) {
                    @Override
                    public boolean shouldShow(@NotNull Player player) {
                        return shouldShow.test(player);
                    }
                };
            });
            case SMITHING_TRIM -> use(vr, (VanillaRecipe.SmithingTrim recipe) -> {
                return new SmithingTrimRecipe(id, toMinestom(recipe.template()), toMinestom(recipe.base()), toMinestom(recipe.addition())) {
                    @Override
                    public boolean shouldShow(@NotNull Player player) {
                        return shouldShow.test(player);
                    }
                };
            });
            case NATIVE -> {
                Logger.warn("Native recipes are not supported yet");
                yield null;
            }
        };
    }

    public ItemStack toItemstack(VanillaRecipe.Result result) {
        return toItemstack(result.item()).withAmount(result.count());
    }

    public ItemStack toItemstack(VanillaRecipe.Ingredient.Item item) {
        String itemId = item.item();
        var itemNbt = item.extraData();
        return ItemStack.builder(Objects.requireNonNull(Material.fromNamespaceId(itemId), () -> "Unknown item " + itemId))
                .meta(builder -> {
                    itemNbt.forEach((key, value) -> {
                        builder.setTag(Tag.NBT(key), value);
                    });
                })
                .build();
    }

    public DeclareRecipesPacket.@NotNull Ingredient toMinestom(VanillaRecipe.Ingredient vanilla) {
        if (vanilla instanceof VanillaRecipe.Ingredient.Item item) {
            return new DeclareRecipesPacket.Ingredient(List.of(toItemstack(item)));
        } else if (vanilla instanceof VanillaRecipe.Ingredient.Tag tag) {
            // TODO: Implement tag support
            Logger.warn("Tag support for recipes are not implemented yet");
            return new DeclareRecipesPacket.Ingredient(List.of(
                    ItemStack.builder(Material.NAME_TAG)
                            .displayName(Component.text("Tag: " + tag.tag()))
                            .build()
            ));
        } else if (vanilla instanceof VanillaRecipe.Ingredient.AnyOf anyOf) {
            return new DeclareRecipesPacket.Ingredient(anyOf.ingredients()
                    .stream()
                    .map(this::toMinestom)
                    .flatMap(ingredient -> ingredient.items().stream())
                    .toList());
        } else if (vanilla instanceof VanillaRecipe.Ingredient.None) {
            return new DeclareRecipesPacket.Ingredient((List<ItemStack>) null);
        } else {
            throw new UnsupportedOperationException("Unknown ingredient type " + vanilla.getClass().getName());
        }
    }
}
