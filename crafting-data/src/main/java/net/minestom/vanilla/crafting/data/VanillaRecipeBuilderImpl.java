package net.minestom.vanilla.crafting.data;

import net.minestom.vanilla.crafting.VanillaRecipe;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class VanillaRecipeBuilderImpl implements VanillaRecipeBuilder {

    private final String group;

    public VanillaRecipeBuilderImpl(String group) {
        this.group = group;
    }


    @Override
    public BlastingStage blasting() {
        return new BlastingStageImpl();
    }

    @Override
    public CampfireCookingStage campfireSmelting() {
        return new CampfireCookingStageImpl();
    }

    @Override
    public CraftingShapedStage craftingShaped() {
        return new CraftingShapedStageImpl();
    }

    @Override
    public CraftingShapelessStage craftingShapeless() {
        return new CraftingShapelessStageImpl();
    }

    @Override
    public SmeltingStage smelting() {
        return new SmeltingStageImpl();
    }

    @Override
    public SmithingStage smithing() {
        return new SmithingStageImpl();
    }

    @Override
    public SmokingStage smoking() {
        return new SmokingStageImpl();
    }

    @Override
    public StonecuttingStage stonecutting() {
        return new StonecuttingStageImpl();
    }

    private static VanillaRecipe.Ingredient ingredientFromBuilder(IngredientBuilder.IngredientCreator ingredientBuilder) {
        return ingredientBuilder.apply(new IngredientBuilderImpl()).build();
    }

    private static VanillaRecipe.Ingredient.Item itemFromBuilder(IngredientBuilder.ItemCreator itemBuilder) {
        return itemBuilder.apply(new IngredientBuilderImpl().item()).build();
    }

    public class BlastingStageImpl implements BlastingStage, CookingStages.Hidden<BlastingStage.Finish>, BlastingStage.Finish {
        @Override
        public BlastingStageImpl ingredient(IngredientBuilder.IngredientCreator ingredientBuilder) {
            this.ingredient = ingredientFromBuilder(ingredientBuilder);
            return this;
        }

        @Override
        public BlastingStageImpl result(int count, IngredientBuilder.ItemCreator ingredientBuilder) {
            this.result = new VanillaRecipe.Result(count, itemFromBuilder(ingredientBuilder));
            return this;
        }

        @Override
        public BlastingStageImpl experience(double experience) {
            this.experience = experience;
            return this;
        }

        @Override
        public Finish cookingTime(int cookingTime) {
            this.cookingTime = cookingTime;
            return this;
        }

        VanillaRecipe.Ingredient ingredient;
        VanillaRecipe.Result result;
        double experience;
        int cookingTime;

        @Override
        public VanillaRecipe.Blasting build() {
            return new VanillaRecipe.Blasting(ingredient, result, experience, cookingTime, group);
        }
    }

    public class CampfireCookingStageImpl implements CampfireCookingStage, CookingStages.Hidden<CampfireCookingStage.Finish>, CampfireCookingStage.Finish {
        @Override
        public CampfireCookingStageImpl ingredient(IngredientBuilder.IngredientCreator ingredientBuilder) {
            this.ingredient = ingredientFromBuilder(ingredientBuilder);
            return this;
        }

        @Override
        public CampfireCookingStageImpl result(int count, IngredientBuilder.ItemCreator ingredientBuilder) {
            this.result = new VanillaRecipe.Result(count, itemFromBuilder(ingredientBuilder));
            return this;
        }

        @Override
        public CampfireCookingStageImpl experience(double experience) {
            this.experience = experience;
            return this;
        }

        @Override
        public Finish cookingTime(int cookingTime) {
            this.cookingTime = cookingTime;
            return this;
        }

        VanillaRecipe.Ingredient ingredient;
        VanillaRecipe.Result result;
        double experience;
        int cookingTime;

        @Override
        public VanillaRecipe.CampfireCooking build() {
            return new VanillaRecipe.CampfireCooking(ingredient, result, experience, cookingTime, group);
        }
    }

    public class CraftingShapedStageImpl implements CraftingShapedStage, CraftingShapedStage.Finish {

        @Override
        public Stages.Result<Finish> ingredients(Consumer<Stages.ShapedIngredients.ShapedIngredientAccumulator> ingredientBuilders) {
            Map<VanillaRecipe.Slot, Set<VanillaRecipe.Ingredient>> pattern = new HashMap<>();
            Stages.ShapedIngredients.ShapedIngredientAccumulator accumulator = new Stages.ShapedIngredients.ShapedIngredientAccumulator() {
                @Override
                public Stages.ShapedIngredients.ShapedIngredientAccumulator put(VanillaRecipe.Slot slot, IngredientBuilder.IngredientCreator... creators) {
                    Set<VanillaRecipe.Ingredient> ingredients = Stream.of(creators)
                            .map(VanillaRecipeBuilderImpl::ingredientFromBuilder)
                            .collect(Collectors.toSet());
                    pattern.put(slot, ingredients);
                    return this;
                }
            };
            ingredientBuilders.accept(accumulator);
            this.pattern = pattern;
            return null;
        }

        @Override
        public CraftingShapedStageImpl result(int count, IngredientBuilder.ItemCreator ingredientBuilder) {
            this.result = new VanillaRecipe.Result(count, itemFromBuilder(ingredientBuilder));
            return this;
        }
        Map<VanillaRecipe.Slot, Set<VanillaRecipe.Ingredient>> pattern;
        VanillaRecipe.Result result;

        @Override
        public VanillaRecipe.CraftingShaped build() {
            var anyOfPattern = pattern.entrySet()
                    .stream()
                    .map(entry -> Map.entry(entry.getKey(), VanillaRecipe.Ingredient.anyOf(entry.getValue())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new VanillaRecipe.CraftingShaped(anyOfPattern, result, group);
        }
    }

    public class CraftingShapelessStageImpl implements CraftingShapelessStage, CraftingShapelessStage.Finish {

        @Override
        public CraftingShapelessStageImpl ingredients(Consumer<Stages.UnshapedIngredients.UnshapedIngredientAccumulator> ingredientBuilders) {
            Map<Set<VanillaRecipe.Ingredient>, Integer> ingredients = new HashMap<>();
            Stages.UnshapedIngredients.UnshapedIngredientAccumulator accumulator = new Stages.UnshapedIngredients.UnshapedIngredientAccumulator() {
                @Override
                public Stages.UnshapedIngredients.UnshapedIngredientAccumulator add(int count, IngredientBuilder.IngredientCreator... creators) {
                    Set<VanillaRecipe.Ingredient> ingredientSet = new HashSet<>();
                    for (IngredientBuilder.IngredientCreator creator : creators) {
                        ingredientSet.add(ingredientFromBuilder(creator));
                        ingredients.put(ingredientSet, count);
                    }
                    return this;
                }
            };
            ingredientBuilders.accept(accumulator);
            this.ingredients = ingredients;
            return this;
        }

        @Override
        public CraftingShapelessStageImpl result(int count, IngredientBuilder.ItemCreator ingredientBuilder) {
            this.result = new VanillaRecipe.Result(count, itemFromBuilder(ingredientBuilder));
            return this;
        }
        Map<Set<VanillaRecipe.Ingredient>, Integer> ingredients;

        VanillaRecipe.Result result;

        @Override
        public VanillaRecipe.CraftingShapeless build() {
            var anyOfIngredients = ingredients.entrySet()
                    .stream()
                    .map(entry -> Map.entry(VanillaRecipe.Ingredient.anyOf(entry.getKey()), entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new VanillaRecipe.CraftingShapeless(anyOfIngredients, result, group);
        }
    }

    public class SmeltingStageImpl implements SmeltingStage, CookingStages.Hidden<SmeltingStage.Finish>, SmeltingStage.Finish {

        @Override
        public SmeltingStageImpl ingredient(IngredientBuilder.IngredientCreator ingredientBuilder) {
            this.ingredient = ingredientFromBuilder(ingredientBuilder);
            return this;
        }

        @Override
        public SmeltingStageImpl result(int count, IngredientBuilder.ItemCreator ingredientBuilder) {
            this.result = new VanillaRecipe.Result(count, itemFromBuilder(ingredientBuilder));
            return this;
        }

        @Override
        public SmeltingStageImpl experience(double experience) {
            this.experience = experience;
            return this;
        }

        @Override
        public Finish cookingTime(int cookingTime) {
            this.cookingTime = cookingTime;
            return this;
        }

        VanillaRecipe.Ingredient ingredient;
        VanillaRecipe.Result result;
        double experience;
        int cookingTime;

        @Override
        public VanillaRecipe.Smelting build() {
            return new VanillaRecipe.Smelting(ingredient, result, experience, cookingTime, group);
        }
    }

    public class SmithingStageImpl implements SmithingStage, SmithingStage.Finish {
        @Override
        public SmithingStageImpl base(IngredientBuilder.IngredientCreator ingredientBuilder) {
            this.base = ingredientFromBuilder(ingredientBuilder);
            return this;
        }

        @Override
        public SmithingStageImpl addition(IngredientBuilder.IngredientCreator ingredientBuilder) {
            this.addition = ingredientFromBuilder(ingredientBuilder);
            return this;
        }

        @Override
        public Finish result(int count, IngredientBuilder.ItemCreator ingredientBuilder) {
            this.result = new VanillaRecipe.Result(count, itemFromBuilder(ingredientBuilder));
            return this;
        }

        VanillaRecipe.Ingredient base;
        VanillaRecipe.Ingredient addition;
        VanillaRecipe.Result result;

        @Override
        public VanillaRecipe.Smithing build() {
            return new VanillaRecipe.Smithing(base, addition, result, group);
        }
    }

    public class SmokingStageImpl implements SmokingStage, CookingStages.Hidden<SmokingStage.Finish>, SmokingStage.Finish {
        @Override
        public SmokingStageImpl ingredient(IngredientBuilder.IngredientCreator ingredientBuilder) {
            this.ingredient = ingredientFromBuilder(ingredientBuilder);
            return this;
        }

        @Override
        public SmokingStageImpl result(int count, IngredientBuilder.ItemCreator ingredientBuilder) {
            this.result = new VanillaRecipe.Result(count, itemFromBuilder(ingredientBuilder));
            return this;
        }

        @Override
        public SmokingStageImpl experience(double experience) {
            this.experience = experience;
            return this;
        }

        @Override
        public Finish cookingTime(int cookingTime) {
            this.cookingTime = cookingTime;
            return this;
        }

        VanillaRecipe.Ingredient ingredient;
        VanillaRecipe.Result result;
        double experience;
        int cookingTime;

        @Override
        public VanillaRecipe.Smoking build() {
            return new VanillaRecipe.Smoking(ingredient, result, experience, cookingTime, group);
        }
    }

    public class StonecuttingStageImpl implements StonecuttingStage, StonecuttingStage.Finish {
        @Override
        public StonecuttingStageImpl ingredients(Collection<IngredientBuilder.IngredientCreator> ingredientBuilder) {
            this.ingredients = ingredientBuilder.stream()
                    .map(VanillaRecipeBuilderImpl::ingredientFromBuilder)
                    .collect(Collectors.toSet());
            return this;
        }

        @Override
        public StonecuttingStageImpl result(int count, IngredientBuilder.ItemCreator ingredientBuilder) {
            this.result = new VanillaRecipe.Result(count, itemFromBuilder(ingredientBuilder));
            return this;
        }

        Set<VanillaRecipe.Ingredient> ingredients;
        VanillaRecipe.Result result;

        @Override
        public VanillaRecipe.Stonecutting build() {
            return new VanillaRecipe.Stonecutting(VanillaRecipe.Ingredient.anyOf(ingredients), result, group);
        }
    }
}
