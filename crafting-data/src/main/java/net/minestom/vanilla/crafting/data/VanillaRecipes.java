package net.minestom.vanilla.crafting.data;

import net.minestom.vanilla.crafting.VanillaRecipe;
import org.jetbrains.annotations.ApiStatus;

/**
 * There are example recipes.
 */
@ApiStatus.Experimental
interface VanillaRecipes {
    VanillaRecipe.CraftingShapeless STICKS = VanillaRecipeBuilder.create("basic")
            .craftingShapeless()
            .ingredients(ingredients -> {
                ingredients.add(1, item -> item.tag().name("planks"));
            })
            .result(4, item -> item.name("sticks"))
            .build();

    VanillaRecipe.Smelting CHARCOAL = VanillaRecipeBuilder.create("cooking")
            .smelting()
            .ingredient(ingredient -> ingredient.tag("logs"))
            .result(1, item -> item.name("charcoal"))
            .experience(0.15)
            .defaultCookingTime()
            .build();

    VanillaRecipe.CraftingShaped TORCHES = VanillaRecipeBuilder.create("basic")
            .craftingShaped()
            .ingredients(ingredients -> {
                ingredients.put(VanillaRecipe.Slot.A__, item -> item.tag("coal")); // coal, charcoal
                ingredients.put(VanillaRecipe.Slot.B__, item -> item.item("sticks")); // sticks
            })
            .result(1, item -> item.name("torch"))
            .build();

    VanillaRecipe.Smelting BURN_THE_TABLE = VanillaRecipeBuilder.create("cooking")
            .smelting()
            .ingredient(ingredient -> ingredient.item("crafting_table"))
            .result(1, item -> item.name("charcoal"))
            .experience(0.15)
            .defaultCookingTime()
            .build();
}
