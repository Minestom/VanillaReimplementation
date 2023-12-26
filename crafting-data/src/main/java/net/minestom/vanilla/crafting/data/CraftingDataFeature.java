package net.minestom.vanilla.crafting.data;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.crafting.VanillaRecipe;
import net.minestom.vanilla.files.ByteArray;
import net.minestom.vanilla.files.FileSystem;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;

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

public class CraftingDataFeature implements VanillaReimplementation.Feature {

    private final boolean isDebug = System.getProperty("minestom.vri.debug") != null;

    @Override
    public void hook(@NotNull HookContext context) {
        JsonRecipeReader recipeReader = new JsonRecipeReader(isDebug);
        Map<String, VanillaRecipe> recipes = new HashMap<>();
        Path recipePath = Path.of("mojang-data", "recipes");
        if (!Files.exists(recipePath)) {
            Logger.warn("Failed to load recipes from mojang-data/recipes, skipping crafting feature.");
            return;
        }

        try {
            FileSystem<Reader> fileSystem = FileSystem.fileSystem(recipePath)
                    .map(bytes -> new InputStreamReader(bytes.toStream()));

            fileSystem.readAll().forEach((name, reader) -> {
                VanillaRecipe recipe = recipeReader.read(reader);
                recipes.put(name, recipe);
            });
        } catch (Throwable e) {
            Logger.warn(e, "Failed to load recipes from mojang-data/recipes, skipping crafting feature.");
        }

        recipes.forEach(context.registry()::register);
        Logger.info("Loaded %s recipes%n", recipes.size());
    }

    @Override
    public @NotNull NamespaceID namespaceId() {
        return NamespaceID.from("vri:crafting_data");
    }
}
