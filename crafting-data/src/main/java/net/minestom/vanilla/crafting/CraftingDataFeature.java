package net.minestom.vanilla.crafting;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;
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
        try {
            JsonRecipeReader reader = new JsonRecipeReader(isDebug);
            Map<String, VanillaRecipe> recipes = new HashMap<>();
            Path recipePath = Path.of("mojang-data", "recipes");
            if (!Files.exists(recipePath)) {
                Logger.warn("Failed to load recipes from mojang-data/recipes, skipping crafting feature.");
                return;
            }
            walk(recipePath, (path, attributes) -> {
                if (!attributes.isRegularFile()) {
                    return;
                }
                FileReader fileReader = new FileReader(path.toFile());
                VanillaRecipe recipe = reader.read(fileReader);
                recipes.put(path.getFileName().toString(), recipe);
                fileReader.close();
            });
            recipes.forEach(context.registry()::register);
            Logger.info("Loaded %s recipes%n", recipes.size());
        } catch (Throwable e) {
            Logger.warn(e, "Failed to load recipes from mojang-data/recipes, skipping crafting feature.");
        }
    }

    private interface Walker {
        void walk(Path path, BasicFileAttributes attributes) throws IOException;
    }

    private void walk(Path path, Walker visitor) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    visitor.walk(file, attrs);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull NamespaceID namespaceId() {
        return NamespaceID.from("vri:crafting_data");
    }
}
