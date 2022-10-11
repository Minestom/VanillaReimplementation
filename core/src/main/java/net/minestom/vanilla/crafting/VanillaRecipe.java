package net.minestom.vanilla.crafting;

import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public sealed interface VanillaRecipe {

    Type type();
    String group();

    /**
     * Represents a recipe in a blast furnace.
     * The default cooking time is 100 ticks, or 5 seconds.
     */
    record Blasting(Ingredient ingredient, Result result, double experience,
                    int cookingTime, String group) implements CookingRecipe {

        public Blasting(Ingredient ingredient, Result result, double experience, String group) {
            this(ingredient, result, experience, 100, group);
        }

        @Override
        public Type type() {
            return Type.BLASTING;
        }
    }

    /**
     * Represents a recipe in a campfire.
     * The default cooking time is 100 ticks, or 5 seconds, even though all vanilla campfire cooking recipes have a
     * cook time of 600 ticks, or 30 seconds. Campfire recipes do not trigger the recipe_unlocked criteria.
     */
    record CampfireCooking(Ingredient ingredient, Result result, double experience, int cookingTime, String group) implements CookingRecipe {

        public CampfireCooking(Ingredient ingredient, Result result, double experience, String group) {
            this(ingredient, result, experience, 100, group);
        }

        @Override
        public Type type() {
            return Type.CAMPFIRE_COOKING;
        }
    }

    /**
     * Represents a shaped crafting recipe in a crafting table.
     * The key used in the pattern may be any single character except the space character, which is reserved for empty
     * slots in a recipe.
     */
    record CraftingShaped(Map<Slot, Set<Ingredient>> pattern, Result result, String group) implements VanillaRecipe {
        public CraftingShaped(Map<Slot, Character> pattern, Map<Character, Set<Ingredient>> palette, Result result,
                              String group) {
            this(pattern.entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), palette.get(entry.getValue())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)), result, group);
        }
        @Override
        public Type type() {
            return Type.CRAFTING_SHAPED;
        }
    }

    /**
     * Represents a shapeless crafting recipe in a crafting table.
     * The ingredients list must have at least one and at most nine entries.
     */
    record CraftingShapeless(Map<Set<Ingredient>, Integer> ingredients, Result result, String group) implements VanillaRecipe {
        @Override
        public Type type() {
            return Type.CRAFTING_SHAPELESS;
        }
    }

    /**
     * Represents a recipe in a furnace.
     * The default cooking time is 200 ticks, or 10 seconds.
     */
    record Smelting(Ingredient ingredient, Result result, double experience, int cookingTime,
                    String group) implements CookingRecipe {

        @Override
        public Type type() {
            return Type.SMELTING;
        }
    }

    /**
     * Represents a recipe in a smithing table.
     * The resulting item copies the NBT tags of the base item.
     */
    record Smithing(Ingredient base, Ingredient addition, Result result, String group) implements VanillaRecipe {

        @Override
        public Type type() {
            return Type.SMITHING;
        }
    }

    /**
     * Represents a recipe in a smoker.
     * The default cooking time is 100 ticks, or 5 seconds.
     */
    record Smoking(Ingredient ingredient, Result result, double experience, int cookingTime,
                   String group) implements CookingRecipe {

        @Override
        public Type type() {
            return Type.SMOKING;
        }
    }

    /**
     * Represents a recipe in a stonecutter.
     * Unlike the  count field in shaped and shapeless crafting recipes, this count field here is required.
     */
    record Stonecutting(Set<Ingredient> ingredients, Result result, String group) implements VanillaRecipe {
        @Override
        public Type type() {
            return Type.STONECUTTING;
        }
    }

    enum Type {
        BLASTING("minecraft:blasting"),
        CAMPFIRE_COOKING("minecraft:campfire_cooking"),
        CRAFTING_SHAPED("minecraft:crafting_shaped"),
        CRAFTING_SHAPELESS("minecraft:crafting_shapeless"),
        SMELTING("minecraft:smelting"),
        SMITHING("minecraft:smithing"),
        SMOKING("minecraft:smoking"),
        STONECUTTING("minecraft:stonecutting"),
        ;

        private final NamespaceID namespace;

        Type(String namespace) {
            this.namespace = NamespaceID.from(namespace);
        }

        public NamespaceID namespace() {
            return namespace;
        }
    }

    sealed interface Ingredient {
        record Item(String item, Map<String, NBTCompound> extraData) implements Ingredient {
            public Item {
                extraData = Map.copyOf(extraData);
            }
        }

        record Tag(String tag) implements Ingredient {
        }
    }

    record Result(int count, Ingredient.Item item)  {
    }

    enum Slot {
        A__(0, 0), _A_(0, 1), __A(0, 2),
        B__(1, 0), _B_(1, 1), __B(1, 2),
        C__(2, 0), _C_(2, 1), __C(2, 2),
        ;

        private final int row;
        private final int column;

        Slot(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public int row() {
            return row;
        }

        public int column() {
            return column;
        }

        public boolean isWithin2x2() {
            return row < 2 && column < 2;
        }
    }

    sealed interface CookingRecipe extends VanillaRecipe {
        Ingredient ingredient();
        Result result();
        double experience();

        /**
         * @return cooking time in ticks
         */
        int cookingTime();
    }
}
