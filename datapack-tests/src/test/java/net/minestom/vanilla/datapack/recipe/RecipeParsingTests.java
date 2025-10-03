package net.minestom.vanilla.datapack.recipe;

import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.Material;
import okio.Buffer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Recipe JSON parsing to ensure backward compatibility when converting to codecs.
 */
public class RecipeParsingTests {

    @Test
    public void testSimpleShapedRecipeFromJson() throws IOException {
        String json = """
                {
                    "type": "minecraft:crafting_shaped",
                    "group": "planks",
                    "pattern": [
                        "##",
                        "##"
                    ],
                    "key": {
                        "#": {
                            "item": "minecraft:oak_log"
                        }
                    },
                    "result": {
                        "item": "minecraft:oak_planks",
                        "count": 4
                    }
                }
                """;

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        Recipe recipe = Recipe.fromJson(reader);
        assertNotNull(recipe);
        assertTrue(recipe instanceof Recipe.Shaped);
        
        Recipe.Shaped shapedRecipe = (Recipe.Shaped) recipe;
        assertEquals(Key.key("minecraft:crafting_shaped"), shapedRecipe.type());
        assertEquals("planks", shapedRecipe.group());
        assertEquals(2, shapedRecipe.pattern().size());
        assertEquals("##", shapedRecipe.pattern().get(0));
        assertEquals("##", shapedRecipe.pattern().get(1));
        assertTrue(shapedRecipe.key().containsKey('#'));
        assertEquals(Material.OAK_PLANKS, shapedRecipe.result().id());
        assertEquals(4, shapedRecipe.result().count());
    }

    @Test
    public void testSimpleShapelessRecipeFromJson() throws IOException {
        String json = """
                {
                    "type": "minecraft:crafting_shapeless",
                    "group": "dyes",
                    "ingredients": [
                        {
                            "item": "minecraft:bone_meal"
                        },
                        {
                            "item": "minecraft:red_dye"
                        }
                    ],
                    "result": {
                        "item": "minecraft:pink_dye",
                        "count": 2
                    }
                }
                """;

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        Recipe recipe = Recipe.fromJson(reader);
        assertNotNull(recipe);
        assertTrue(recipe instanceof Recipe.Shapeless);
        
        Recipe.Shapeless shapelessRecipe = (Recipe.Shapeless) recipe;
        assertEquals(Key.key("minecraft:crafting_shapeless"), shapelessRecipe.type());
        assertEquals("dyes", shapelessRecipe.group());
        assertEquals(Material.PINK_DYE, shapelessRecipe.result().id());
        assertEquals(2, shapelessRecipe.result().count());
    }

    @Test
    public void testSmeltingRecipeFromJson() throws IOException {
        String json = """
                {
                    "type": "minecraft:smelting",
                    "group": "iron_ingot",
                    "ingredient": {
                        "item": "minecraft:iron_ore"
                    },
                    "result": "minecraft:iron_ingot",
                    "experience": 0.7,
                    "cookingtime": 200
                }
                """;

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        Recipe recipe = Recipe.fromJson(reader);
        assertNotNull(recipe);
        assertTrue(recipe instanceof Recipe.Smelting);
        
        Recipe.Smelting smeltingRecipe = (Recipe.Smelting) recipe;
        assertEquals(Key.key("minecraft:smelting"), smeltingRecipe.type());
        assertEquals("iron_ingot", smeltingRecipe.group());
        assertEquals(0.7, smeltingRecipe.experience());
        assertEquals(200, smeltingRecipe.cookingTime());
    }

    @Test
    public void testSpecialRecipeFromJson() throws IOException {
        String json = """
                {
                    "type": "minecraft:crafting_special_armordye",
                    "group": "armor_dye"
                }
                """;

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        Recipe recipe = Recipe.fromJson(reader);
        assertNotNull(recipe);
        assertTrue(recipe instanceof Recipe.Special.ArmorDye);
        
        Recipe.Special.ArmorDye armorDyeRecipe = (Recipe.Special.ArmorDye) recipe;
        assertEquals(Key.key("minecraft:crafting_special_armordye"), armorDyeRecipe.type());
        assertEquals("armor_dye", armorDyeRecipe.group());
    }

    @Test
    public void testIngredientFromJsonSingleItem() throws IOException {
        String json = "\"minecraft:iron_ingot\"";

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        Recipe.Ingredient ingredient = Recipe.Ingredient.fromJson(reader);
        assertNotNull(ingredient);
        assertTrue(ingredient instanceof Recipe.Ingredient.Single);
        
        Recipe.Ingredient.Single singleIngredient = (Recipe.Ingredient.Single) ingredient;
        assertTrue(singleIngredient instanceof Recipe.Ingredient.Item);
        
        Recipe.Ingredient.Item itemIngredient = (Recipe.Ingredient.Item) singleIngredient;
        assertEquals(Material.IRON_INGOT, itemIngredient.item());
    }

    @Test
    public void testIngredientFromJsonTag() throws IOException {
        String json = "\"#minecraft:logs\"";

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        Recipe.Ingredient ingredient = Recipe.Ingredient.fromJson(reader);
        assertNotNull(ingredient);
        assertTrue(ingredient instanceof Recipe.Ingredient.Single);
        
        Recipe.Ingredient.Single singleIngredient = (Recipe.Ingredient.Single) ingredient;
        assertTrue(singleIngredient instanceof Recipe.Ingredient.Tag);
        
        Recipe.Ingredient.Tag tagIngredient = (Recipe.Ingredient.Tag) singleIngredient;
        assertEquals(Key.key("minecraft:logs"), tagIngredient.tag());
    }

    @Test
    public void testIngredientFromJsonMultiple() throws IOException {
        String json = """
                [
                    "minecraft:iron_ingot",
                    "minecraft:gold_ingot"
                ]
                """;

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        Recipe.Ingredient ingredient = Recipe.Ingredient.fromJson(reader);
        assertNotNull(ingredient);
        assertTrue(ingredient instanceof Recipe.Ingredient.Multi);
        
        Recipe.Ingredient.Multi multiIngredient = (Recipe.Ingredient.Multi) ingredient;
        assertEquals(2, multiIngredient.items().size());
    }

    @Test
    public void testIngredientFromJsonNull() throws IOException {
        String json = "null";

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        Recipe.Ingredient ingredient = Recipe.Ingredient.fromJson(reader);
        assertNotNull(ingredient);
        assertTrue(ingredient instanceof Recipe.Ingredient.None);
    }

    @Test
    public void testUnknownRecipeTypeThrowsException() {
        String json = """
                {
                    "type": "minecraft:unknown_recipe_type",
                    "group": "test"
                }
                """;

        Buffer buffer = new Buffer().writeUtf8(json);
        JsonReader reader = JsonReader.of(buffer);
        
        assertThrows(IOException.class, () -> Recipe.fromJson(reader));
    }
}