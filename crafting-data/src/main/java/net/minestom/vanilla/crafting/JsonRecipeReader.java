package net.minestom.vanilla.crafting;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.tinylog.Logger;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonRecipeReader {

    private final Gson gson = new Gson();
    private final boolean debugPrinting;

    public JsonRecipeReader(boolean debugPrinting) {
        this.debugPrinting = debugPrinting;
    }

    public JsonRecipeReader() {
        this(false);
    }

    private void log(Object message) {
        if (debugPrinting) {
            Logger.info(message);
        }
    }

    public VanillaRecipe read(Reader reader) {
        JsonObject object = gson.fromJson(reader, JsonObject.class);

        VanillaRecipe.Type type;
        {
            JsonElement strType = object.get("type");
            if (strType == null) throw new IllegalArgumentException("Recipe type is not specified");
            if (!strType.isJsonPrimitive()) throw new IllegalArgumentException("Recipe type is not a string");

            type = VanillaRecipe.Type.from(NamespaceID.from(strType.getAsString()));
            if (type == null) {
                log("Unknown recipe type: " + strType.getAsString());
                type = VanillaRecipe.Type.NATIVE;
            }
        }

        return switch (type) {
            case BLASTING -> readBlasting(object);
            case CAMPFIRE_COOKING -> readCampfireCooking(object);
            case CRAFTING_SHAPED -> readCraftingShaped(object);
            case CRAFTING_SHAPELESS -> readCraftingShapeless(object);
            case SMELTING -> readSmelting(object);
            case SMITHING -> readSmithing(object);
            case SMOKING -> readSmoking(object);
            case STONECUTTING -> readStonecutting(object);
            case NATIVE -> readNative(object);
        };
    }

    private VanillaRecipe.Blasting readBlasting(JsonObject object) {
        log("Blasting");
        log(object);

        int cookingTime = orDefault(object.get("cookingtime"), JsonElement::getAsInt, VanillaRecipe.Blasting.DEFAULT_COOKING_TIME);
        double experience = required(object.get("experience"), JsonElement::getAsDouble, "experience", "blasting");
        String group = orDefault(object.get("group"), JsonElement::getAsString, null);
        VanillaRecipe.Ingredient ingredient = required(object.get("ingredient"), this::readIngredient, "ingredient", "blasting");
        VanillaRecipe.Result result = required(object.get("result"), this::readResult, "result", "blasting");

        return new VanillaRecipe.Blasting(ingredient, result, experience, cookingTime, group);
    }

    private VanillaRecipe.CampfireCooking readCampfireCooking(JsonObject object) {
        log("CampfireCooking");
        log(object);

        int cookingTime = orDefault(object.get("cookingtime"), JsonElement::getAsInt, VanillaRecipe.CampfireCooking.DEFAULT_COOKING_TIME);
        double experience = required(object.get("experience"), JsonElement::getAsDouble, "experience", "campfire_cooking");
        String group = orDefault(object.get("group"), JsonElement::getAsString, null);
        VanillaRecipe.Ingredient ingredient = required(object.get("ingredient"), this::readIngredient, "ingredient", "campfire_cooking");
        VanillaRecipe.Result result = required(object.get("result"), this::readResult, "result", "campfire_cooking");

        return new VanillaRecipe.CampfireCooking(ingredient, result, experience, cookingTime, group);
    }

    private VanillaRecipe.CraftingShaped readCraftingShaped(JsonObject object) {
        log("CraftingShaped");
        log(object);

        JsonObject jsonPalette = required(object.get("key"), JsonElement::getAsJsonObject, "key", "crafting_shaped");
        JsonArray jsonPattern = required(object.get("pattern"), JsonElement::getAsJsonArray, "pattern", "crafting_shaped");
        VanillaRecipe.Result result = required(object.get("result"), this::readResult, "result", "crafting_shaped");
        String group = orDefault(object.get("group"), JsonElement::getAsString, null);

        Map<Character, VanillaRecipe.Ingredient> palette = jsonPalette.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey().charAt(0), readIngredient(entry.getValue())))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        List<String> patternLines = StreamSupport.stream(jsonPattern.spliterator(), false)
                .map(JsonElement::getAsString)
                .toList();

        Map<VanillaRecipe.Slot, Character> pattern = new HashMap<>();
        for (int row = 0; row < patternLines.size(); row++) {
            String line = patternLines.get(row);
            if (line.length() > 3) throw new IllegalArgumentException("Pattern line " + row + " is too long");

            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);
                if (c != ' ' && !palette.containsKey(c))
                    throw new IllegalArgumentException("Unknown pattern character: " + c);

                pattern.put(VanillaRecipe.Slot.from(row, col), c);
            }
        }

        return new VanillaRecipe.CraftingShaped(pattern, palette, result, group);
    }

    private VanillaRecipe.CraftingShapeless readCraftingShapeless(JsonObject object) {
        JsonArray jsonIngredients = required(object.get("ingredients"), JsonElement::getAsJsonArray, "ingredients", "crafting_shapeless");
        VanillaRecipe.Result result = required(object.get("result"), this::readResult, "result", "crafting_shapeless");
        String group = orDefault(object.get("group"), JsonElement::getAsString, null);

        Map<VanillaRecipe.Ingredient, Integer> ingredients = new HashMap<>();
        StreamSupport.stream(jsonIngredients.spliterator(), false)
                .map(this::readIngredient)
                .forEach(ingredient -> ingredients.merge(ingredient, 1, Integer::sum));

        return new VanillaRecipe.CraftingShapeless(ingredients, result, group);
    }

    private VanillaRecipe.Smelting readSmelting(JsonObject object) {
        log("Smelting");
        log(object);

        int cookingTime = orDefault(object.get("cookingtime"), JsonElement::getAsInt, VanillaRecipe.Smelting.DEFAULT_COOKING_TIME);
        double experience = required(object.get("experience"), JsonElement::getAsDouble, "experience", "smelting");
        String group = orDefault(object.get("group"), JsonElement::getAsString, null);
        VanillaRecipe.Ingredient ingredient = required(object.get("ingredient"), this::readIngredient, "ingredient", "smelting");
        VanillaRecipe.Result result = required(object.get("result"), this::readResult, "result", "smelting");

        return new VanillaRecipe.Smelting(ingredient, result, experience, cookingTime, group);
    }

    private VanillaRecipe.Smithing readSmithing(JsonObject object) {
        log("Smithing");
        log(object);

        VanillaRecipe.Ingredient base = required(object.get("base"), this::readIngredient, "base", "smithing");
        VanillaRecipe.Ingredient addition = required(object.get("addition"), this::readIngredient, "addition", "smithing");
        VanillaRecipe.Result result = required(object.get("result"), this::readResult, "result", "smithing");
        String group = orDefault(object.get("group"), JsonElement::getAsString, null);

        return new VanillaRecipe.Smithing(base, addition, result, group);
    }

    private VanillaRecipe.Smoking readSmoking(JsonObject object) {
        log("Smoking");
        log(object);

        int cookingTime = orDefault(object.get("cookingtime"), JsonElement::getAsInt, VanillaRecipe.Smoking.DEFAULT_COOKING_TIME);
        double experience = required(object.get("experience"), JsonElement::getAsDouble, "experience", "smoking");
        String group = orDefault(object.get("group"), JsonElement::getAsString, null);
        VanillaRecipe.Ingredient ingredient = required(object.get("ingredient"), this::readIngredient, "ingredient", "smoking");
        VanillaRecipe.Result result = required(object.get("result"), this::readResult, "result", "smoking");

        return new VanillaRecipe.Smoking(ingredient, result, experience, cookingTime, group);
    }

    private VanillaRecipe.Stonecutting readStonecutting(JsonObject object) {
        log("Stonecutting");
        log(object);

        VanillaRecipe.Ingredient ingredient = required(object.get("ingredient"), this::readIngredient, "ingredient", "stonecutting");
        VanillaRecipe.Result result = required(object.get("result"), this::readResult, "result", "stonecutting");
        String group = orDefault(object.get("group"), JsonElement::getAsString, null);

        return new VanillaRecipe.Stonecutting(ingredient, result, group);
    }

    private VanillaRecipe readNative(JsonObject object) {
        String type = required(object.get("type"), JsonElement::getAsString, "type", "native");
        String group = orDefault(object.get("group"), JsonElement::getAsString, null);

        return new VanillaRecipe.Native(NamespaceID.from(type), group);
    }

    private VanillaRecipe.Result readResult(JsonElement element) {
        log("Result");
        log(element);

        if (!element.isJsonObject()) {
            String resultId = required(element, JsonElement::getAsString, "result", "read_result");
            return new VanillaRecipe.Result(1, new VanillaRecipe.Ingredient.Item(resultId));
        }

        JsonObject object = element.getAsJsonObject();
        int count = orDefault(object.get("count"), JsonElement::getAsInt, 1);
        String resultId = required(object.get("item"), JsonElement::getAsString, "item", "read_result");

        return new VanillaRecipe.Result(count, new VanillaRecipe.Ingredient.Item(resultId));
    }

    private VanillaRecipe.Ingredient readIngredient(JsonElement element) {
        log("Ingredient");
        log(element);

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            return new VanillaRecipe.Ingredient.AnyOf(StreamSupport.stream(array.spliterator(), false)
                    .map(this::readIngredient)
                    .collect(Collectors.toSet()));
        }

        if (!(element instanceof JsonObject obj)) {
            log("non-object ingredient: " + element);
            String itemId = required(element, JsonElement::getAsString, "ingredient", "read_ingredient");
            return new VanillaRecipe.Ingredient.Item(itemId);
        }

        String itemId = orDefault(obj.get("item"), JsonElement::getAsString, null);
        String tagId = orDefault(obj.get("tag"), JsonElement::getAsString, null);

        if (itemId != null && tagId != null) throw new IllegalArgumentException("Ingredient cannot have both item and tag");
        if (itemId != null) return new VanillaRecipe.Ingredient.Item(itemId);
        if (tagId != null) return new VanillaRecipe.Ingredient.Tag(tagId);

        throw new IllegalArgumentException("Ingredient must have either item or tag");
    }

    private <T> @NotNull T required(@Nullable JsonElement element, Function<JsonElement, T> mapper, String key, String category) {
        if (element == null) throw new IllegalArgumentException("Required '" + key + "' value is required but not specified in " + category + " recipe.");
        return mapper.apply(element);
    }

    private <T> @UnknownNullability T orDefault(@Nullable JsonElement element, @NotNull Function<JsonElement, T> function, @Nullable T defaultValue) {
        if (element == null) return defaultValue;
        return function.apply(element);
    }
}
