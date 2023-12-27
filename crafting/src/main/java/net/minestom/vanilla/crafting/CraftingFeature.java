package net.minestom.vanilla.crafting;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackLoadingFeature;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CraftingFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull HookContext context) {
        DatapackLoadingFeature datapackData = context.vri().feature(DatapackLoadingFeature.class);
        Datapack datapack = datapackData.vanilla();

        VriRecipeToMinestomRecipe recipeConverter = new VriRecipeToMinestomRecipe(datapack);
        datapack.namespacedData().forEach((namespace, data) -> {
            data.recipes().readAll().forEach((id, recipe) -> {
                Logger.debug("Registering recipe " + id + "...");
                var recipeManager = context.vri().process().recipe();

                net.minestom.server.recipe.Recipe minestomRecipe = recipeConverter.convert(id, recipe, player -> true);
                if (minestomRecipe == null) {
                    Logger.warn("Failed to convert recipe " + id + ", skipping.");
                    return;
                }
                recipeManager.addRecipe(minestomRecipe);
            });
        });
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
