package net.minestom.vanilla.crafting;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.crafting.smelting.BlastingInventoryRecipes;
import net.minestom.vanilla.crafting.smelting.SmeltingInventoryRecipes;
import net.minestom.vanilla.crafting.smelting.SmokingInventoryRecipes;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackLoadingFeature;
import net.minestom.vanilla.datapack.recipe.Recipe;
import net.minestom.vanilla.files.FileSystem;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CraftingFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull HookContext context) {
        DatapackLoadingFeature datapackData = context.vri().feature(DatapackLoadingFeature.class);
        Datapack datapack = datapackData.current();

        VriRecipeToMinestomRecipe recipeConverter = new VriRecipeToMinestomRecipe(datapack);
        datapack.namespacedData().forEach((namespace, data) -> {
            FileSystem<Recipe> recipeFileSystem = data.recipes();
            recipeFileSystem.files().stream().collect(Collectors.toMap(Function.identity(), recipeFileSystem::file)).forEach((id, recipe) -> {
                var recipeManager = context.vri().process().recipe();

                net.minestom.server.recipe.Recipe minestomRecipe = recipeConverter.convert(id, recipe, player -> true);
                if (minestomRecipe == null) {
                    return;
                }
                recipeManager.addRecipe(minestomRecipe);
            });
        });

        EventNode<Event> survival = new SurvivalInventoryRecipes(datapack, context.vri()).init();
        context.vri().process().eventHandler().addChild(survival);

        EventNode<Event> crafting = new CraftingInventoryRecipes(datapack, context.vri()).init();
        context.vri().process().eventHandler().addChild(crafting);

        EventNode<Event> smelting = new SmeltingInventoryRecipes(datapack, context.vri()).init();
        context.vri().process().eventHandler().addChild(smelting);

        EventNode<Event> smoking = new SmokingInventoryRecipes(datapack, context.vri()).init();
        context.vri().process().eventHandler().addChild(smoking);

        EventNode<Event> blasting = new BlastingInventoryRecipes(datapack, context.vri()).init();
        context.vri().process().eventHandler().addChild(blasting);

        EventNode<Event> stonecutting = new StonecuttingInventoryRecipes(datapack, context.vri()).init();
        context.vri().process().eventHandler().addChild(stonecutting);

        EventNode<Event> smithing = new SmithingInventoryRecipes(datapack, context.vri()).init();
        context.vri().process().eventHandler().addChild(smithing);
    }

    @Override
    public @NotNull NamespaceID namespaceId() {
        return NamespaceID.from("vri:crafting");
    }

    @Override
    public @NotNull Set<Class<? extends VanillaReimplementation.Feature>> dependencies() {
        return Set.of(DatapackLoadingFeature.class);
    }
}
