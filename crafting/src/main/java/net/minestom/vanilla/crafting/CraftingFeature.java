package net.minestom.vanilla.crafting;

import net.kyori.adventure.key.Key;
import net.minestom.server.ServerProcess;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.vanilla.datapack.Datapacks;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class CraftingFeature {

    public record Recipes(@NotNull Crafting crafting,
                          @NotNull Smelting smelting,
                          @NotNull Smithing smithing,
                          @NotNull Map<Key, Recipe.Stonecutting> stonecutting,
                          @NotNull Map<Key, Recipe> special) {
        public Recipes {
            stonecutting = Map.copyOf(stonecutting);
            special = Map.copyOf(special);
        }

        public static @NotNull Recipes fromRaw(@NotNull Map<Key, Recipe> recipes) {
            Set<Map.Entry<Key, Recipe>> entries = recipes.entrySet();

            Recipes parsedRecipes = new Recipes(
                    new Recipes.Crafting(
                            filterRecipes(entries, Recipe.Crafting.Shaped.class),
                            filterRecipes(entries, Recipe.Crafting.Shapeless.class),
                            filterRecipes(entries, Recipe.Crafting.Transmute.class)
                    ),
                    new Recipes.Smelting(
                            filterRecipes(entries, Recipe.Cooking.Smelting.class),
                            filterRecipes(entries, Recipe.Cooking.Smoking.class),
                            filterRecipes(entries, Recipe.Cooking.Blasting.class),
                            filterRecipes(entries, Recipe.Cooking.Campfire.class)
                    ),
                    new Recipes.Smithing(
                            filterRecipes(entries, Recipe.Smithing.Transform.class),
                            filterRecipes(entries, Recipe.Smithing.Trim.class)
                    ),
                    filterRecipes(entries, Recipe.Stonecutting.class),
                    filterRecipes(entries, Recipe.class) // All remaining
            );

            if (!entries.isEmpty()) throw new RuntimeException("Entries should be empty!");

            return parsedRecipes;
        }

        @SuppressWarnings("unchecked")
        private static <T extends Recipe> @NotNull Map<Key, T> filterRecipes(@NotNull Iterable<Map.Entry<Key, Recipe>> entries, @NotNull Class<T> clazz) {
            Map<Key, T> map = new HashMap<>();

            var iter = entries.iterator();
            while (iter.hasNext()) {
                var entry = iter.next();

                if (clazz.isInstance(entry.getValue())) {
                    map.put(entry.getKey(), (T) entry.getValue());
                    iter.remove();
                }
            }

            return map;
        }

        public record Crafting(@NotNull Map<Key, Recipe.Crafting.Shaped> shaped,
                               @NotNull Map<Key, Recipe.Crafting.Shapeless> shapeless,
                               @NotNull Map<Key, Recipe.Crafting.Transmute> transmute) {
            public Crafting {
                shaped = Map.copyOf(shaped);
                shapeless = Map.copyOf(shapeless);
                transmute = Map.copyOf(transmute);
            }
        }

        public record Smelting(@NotNull Map<Key, Recipe.Cooking.Smelting> smelting,
                               @NotNull Map<Key, Recipe.Cooking.Smoking> smoking,
                               @NotNull Map<Key, Recipe.Cooking.Blasting> blasting,
                               @NotNull Map<Key, Recipe.Cooking.Campfire> campfire) {
            public Smelting {
                smelting = Map.copyOf(smelting);
                smoking = Map.copyOf(smoking);
                blasting = Map.copyOf(blasting);
                campfire = Map.copyOf(campfire);
            }
        }

        public record Smithing(@NotNull Map<Key, Recipe.Smithing.Transform> transform,
                               @NotNull Map<Key, Recipe.Smithing.Trim> trim) {
            public Smithing {
                transform = Map.copyOf(transform);
                trim = Map.copyOf(trim);
            }
        }
    }

    public static @NotNull Map<Key, Recipe> buildFromDatapack(@NotNull ServerProcess process) {
        final Path recipesPath = Path.of("/", "data", "minecraft", "recipe");

        Map<Key, Recipe> recipes;

        try {
            Path jar = Datapacks.ensureCurrentJarExists();

            recipes = Datapacks.buildRegistryFromJar(jar, recipesPath, process, ".json", Recipe.CODEC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Logger.info("Loaded and parsed " + recipes.size() + " recipes");
        return recipes;
    }

    public static @NotNull EventNode<Event> createEventNode(@NotNull Map<Key, Recipe> recipeMap, @NotNull ServerProcess process) {
        // Register recipes
        recipeMap.values().forEach(process.recipe()::addRecipe);

        // Parse recipes into usable form
        Recipes recipes = Recipes.fromRaw(recipeMap);

        return EventNode.all("vri:recipes")
                .addChild(new CraftingRecipes(recipes, process).init())
                .addListener(PlayerBlockInteractEvent.class, event -> {
                    if (event.getBlock().compare(Block.CRAFTING_TABLE)) event.getPlayer().openInventory(new Inventory(InventoryType.CRAFTING, "Crafting"));
                });
    }

}
