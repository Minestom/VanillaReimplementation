package net.minestom.vanilla.crafting;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public sealed interface VanillaRecipe {

    @NotNull Type type();
    @Nullable String group();

    /**
     * Represents a recipe in a blast furnace.
     * The default cooking time is 100 ticks, or 5 seconds.
     */
    record Blasting(Ingredient ingredient, Result result, double experience,
                    int cookingTime, String group) implements CookingRecipe {

        public static final int DEFAULT_COOKING_TIME = 100;

        public Blasting(Ingredient ingredient, Result result, double experience, String group) {
            this(ingredient, result, experience, DEFAULT_COOKING_TIME, group);
        }

        @Override
        public int defaultCookingTime() {
            return DEFAULT_COOKING_TIME;
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

        public static final int DEFAULT_COOKING_TIME = 100;

        public CampfireCooking(Ingredient ingredient, Result result, double experience, String group) {
            this(ingredient, result, experience, DEFAULT_COOKING_TIME, group);
        }

        @Override
        public int defaultCookingTime() {
            return DEFAULT_COOKING_TIME;
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
    record CraftingShaped(Map<Slot, Ingredient> pattern, Result result, String group) implements VanillaRecipe {
        public CraftingShaped(Map<Slot, Character> pattern, Map<Character, Ingredient> palette, Result result,
                              String group) {
            this(pattern.entrySet().stream()
                    .filter(entry -> entry.getValue() != ' ') // ignore spaces
                    .map(entry -> Map.entry(entry.getKey(), palette.get(entry.getValue())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)), result, group);
        }

        public CraftingShaped {
            pattern = Map.copyOf(pattern);
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
    record CraftingShapeless(Map<Ingredient, Integer> ingredients, Result result, String group) implements VanillaRecipe {
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

        public static final int DEFAULT_COOKING_TIME = 200;

        public Smelting(Ingredient ingredient, Result result, double experience, String group) {
            this(ingredient, result, experience, DEFAULT_COOKING_TIME, group);
        }

        @Override
        public int defaultCookingTime() {
            return DEFAULT_COOKING_TIME;
        }

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

        public static final int DEFAULT_COOKING_TIME = 100;

        public Smoking(Ingredient ingredient, Result result, double experience, String group) {
            this(ingredient, result, experience, DEFAULT_COOKING_TIME, group);
        }

        @Override
        public int defaultCookingTime() {
            return DEFAULT_COOKING_TIME;
        }

        @Override
        public Type type() {
            return Type.SMOKING;
        }
    }

    /**
     * Represents a recipe in a stonecutter.
     * Unlike the  count field in shaped and shapeless crafting recipes, this count field here is required.
     */
    record Stonecutting(Ingredient ingredients, Result result, String group) implements VanillaRecipe {
        @Override
        public Type type() {
            return Type.STONECUTTING;
        }
    }

    /**
     * Represents a native recipe.
     * Native recipes are not defined in the vanilla data pack, but are instead defined in the server code.
     */
    record Native(NamespaceID id, String group) implements VanillaRecipe {
        @Override
        public Type type() {
            return Type.NATIVE;
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
        NATIVE("minecraft:native"),
        ;

        private final NamespaceID namespace;

        Type(String namespace) {
            this.namespace = NamespaceID.from(namespace);
        }

        public static @Nullable Type from(NamespaceID namespace) {
            return Arrays.stream(values())
                    .filter(type -> type.namespace.equals(namespace))
                    .findAny()
                    .orElse(null);
        }

        public NamespaceID namespace() {
            return namespace;
        }
    }

    sealed interface Ingredient {
        static Ingredient anyOf(Collection<Ingredient> multi) {
            if (multi.isEmpty()) {
                throw new IllegalArgumentException("multi must not be empty");
            }
            if (multi.size() == 1) {
                return multi.iterator().next();
            }
            return new AnyOf(multi);
        }

        record Item(String item, Map<String, NBTCompound> extraData) implements Ingredient {
            public Item(String item) {
                this(item, Map.of());
            }
            public Item {
                extraData = Map.copyOf(extraData);
            }
        }

        record Tag(String tag) implements Ingredient {
        }

        record AnyOf(Set<Ingredient> ingredients) implements Ingredient {
            public AnyOf(Ingredient... ingredients) {
                this(Set.of(ingredients));
            }
            public AnyOf(Collection<Ingredient> ingredients) {
                this(Set.copyOf(ingredients));
            }
            public AnyOf {
                ingredients = Set.copyOf(ingredients);
            }
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

        public static @NotNull Slot from(int row, int column) {
            return Arrays.stream(values())
                    .filter(slot -> slot.row == row && slot.column == column)
                    .findAny()
                    .orElseThrow();
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

        int defaultCookingTime();
    }
}
