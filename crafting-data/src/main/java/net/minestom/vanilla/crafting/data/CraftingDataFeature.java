package net.minestom.vanilla.crafting.data;

import io.github.pesto.MojangDataFeature;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.crafting.VanillaRecipe;
import net.minestom.vanilla.files.ByteArray;
import net.minestom.vanilla.files.FileSystem;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CraftingDataFeature implements VanillaReimplementation.Feature {

    private final boolean isDebug = System.getProperty("minestom.vri.debug") != null;

    private Map<String, VanillaRecipe> id2Recipe = null;

    @Override
    public void hook(@NotNull HookContext context) {

        MojangDataFeature mojangData = context.vri().feature(MojangDataFeature.class);
        FileSystem<Reader> recipeData = mojangData.latestAssets()
                .folder("minecraft")
                .folder("recipes")
                .map(bytes -> new InputStreamReader(bytes.toStream()));

        JsonRecipeReader recipeReader = new JsonRecipeReader(isDebug);
        Map<String, VanillaRecipe> recipes = new HashMap<>();

        try {
            recipeData.readAll().forEach((name, reader) -> {
                VanillaRecipe recipe = recipeReader.read(reader);
                recipes.put(name, recipe);
            });
        } catch (Throwable e) {
            Logger.warn(e, "Failed to load recipes from mojang-data/recipes, skipping crafting feature.");
        }

        this.id2Recipe = Map.copyOf(recipes);
        Logger.info("Loaded %s recipes%n", recipes.size());
    }

    public @Nullable VanillaRecipe getRecipe(String id) {
        return getAllRecipes().get(id);
    }

    public Map<String, VanillaRecipe> getAllRecipes() {
        if (id2Recipe == null) {
            throw new IllegalStateException("The CraftingDataFeature has not been hooked yet!");
        }
        return id2Recipe;
    }

    @Override
    public @NotNull NamespaceID namespaceId() {
        return NamespaceID.from("vri:crafting_data");
    }

    @Override
    public @NotNull Set<Class<? extends VanillaReimplementation.Feature>> dependencies() {
        return Set.of(MojangDataFeature.class);
    }
}
