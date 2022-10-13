package net.minestom.vanilla.crafting;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class CraftingDataFeature implements VanillaReimplementation.Feature {

    private final boolean isDebug = System.getProperty("minestom.vri.debug") != null;

    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {
        JsonRecipeReader reader = new JsonRecipeReader(isDebug);

        Map<String, VanillaRecipe> recipes = new HashMap<>();

        walk(Path.of("mojang-data", "recipes"), (path, attributes) -> {
            if (!attributes.isRegularFile()) {
                return;
            }
            FileReader fileReader = new FileReader(path.toFile());
            VanillaRecipe recipe = reader.read(fileReader);
            recipes.put(path.getFileName().toString(), recipe);
            fileReader.close();
        });

        recipes.forEach(registry::register);
        Logger.info("Loaded {} recipes", recipes.size());
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
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("vri:crafting_data");
    }
}
