package net.minestom.vanilla.crafting.data;

import net.minestom.vanilla.crafting.VanillaRecipe;
import org.jetbrains.annotations.ApiStatus;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minestom.vanilla.crafting.data.VanillaRecipeBuilder.Stages.*;

@ApiStatus.Experimental
public sealed interface VanillaRecipeBuilder permits VanillaRecipeBuilderImpl {

    @ApiStatus.Experimental
    static VanillaRecipeBuilder create(String group) {
        return new VanillaRecipeBuilderImpl(group);
    }

    BlastingStage blasting();
    CampfireCookingStage campfireSmelting();
    CraftingShapedStage craftingShaped();
    CraftingShapelessStage craftingShapeless();
    SmeltingStage smelting();
    SmithingStage smithing();
    SmokingStage smoking();
    StonecuttingStage stonecutting();

    interface IngredientBuilder {

        IngredientBuilder.Item item();
        default IngredientBuilder.Item.DataOrFinishStage item(String name) {
            return item().name(name);
        }
        IngredientBuilder.Tag tag();
        default IngredientBuilder.Tag.FinishStage tag(String name) {
            return tag().name(name);
        }

        interface Finisher {
            VanillaRecipe.Ingredient build();
        }

        interface ItemCreator extends Function<IngredientBuilder.Item, IngredientBuilder.Item.FinishStage> {
        }

        interface IngredientCreator extends Function<IngredientBuilder, IngredientBuilder.Finisher> {
        }

        interface Item {
            DataOrFinishStage name(String name);
            interface DataOrFinishStage extends ExtraDataStage, FinishStage {
            }
            interface ExtraDataStage {
                FinishStage extraData(Map<String, NBTCompound> extraData);
            }
            interface FinishStage extends Finisher {
                VanillaRecipe.Ingredient.Item build();
            }
        }

        interface Tag {
            FinishStage name(String name);
            interface FinishStage extends Finisher {
                VanillaRecipe.Ingredient.Tag build();
            }
        }
    }

    interface CookingStages<N> extends Ingredient<Result<Experience<CookingTime<N>>>> {
        interface Hidden<N> extends Result<Experience<CookingTime<N>>>, Experience<CookingTime<N>>, CookingTime<N> {
        }
    }

    interface BlastingStage extends CookingStages<BlastingStage.Finish> {
        interface Finish {
            VanillaRecipe.Blasting build();
        }
    }

    interface CampfireCookingStage extends CookingStages<CampfireCookingStage.Finish> {
        interface Finish {
            VanillaRecipe.CampfireCooking build();
        }
    }

    interface CraftingShapedStage extends ShapedIngredients<Result<CraftingShapedStage.Finish>>, Result<CraftingShapedStage.Finish> {
        interface Finish {
            VanillaRecipe.CraftingShaped build();
        }
    }

    interface CraftingShapelessStage extends UnshapedIngredients<Result<CraftingShapelessStage.Finish>>, Result<CraftingShapelessStage.Finish> {
        interface Finish {
            VanillaRecipe.CraftingShapeless build();
        }
    }

    interface SmeltingStage extends CookingStages<SmeltingStage.Finish> {
        interface Finish {
            VanillaRecipe.Smelting build();
        }
    }

    interface SmithingStage extends IngredientBase<IngredientAddition<Result<SmithingStage.Finish>>>, IngredientAddition<Result<SmithingStage.Finish>>, Result<SmithingStage.Finish> {
        interface Finish {
            VanillaRecipe.Smithing build();
        }
    }

    interface SmokingStage extends CookingStages<SmokingStage.Finish> {
        interface Finish {
            VanillaRecipe.Smoking build();
        }
    }

    interface StonecuttingStage extends IngredientList<Result<StonecuttingStage.Finish>>, Result<StonecuttingStage.Finish> {
        interface Finish {
            VanillaRecipe.Stonecutting build();
        }
    }

    interface Stages {
        interface Ingredient<N> {
            N ingredient(IngredientBuilder.IngredientCreator ingredientBuilder);
        }
        interface IngredientBase<N> {
            N base(IngredientBuilder.IngredientCreator ingredientBuilder);
        }
        interface IngredientAddition<N> {
            N addition(IngredientBuilder.IngredientCreator ingredientBuilder);
        }
        interface UnshapedIngredients<N> {
            N ingredients(Consumer<UnshapedIngredientAccumulator> ingredientBuilders);
            interface UnshapedIngredientAccumulator {
                UnshapedIngredientAccumulator add(int count, IngredientBuilder.IngredientCreator... ingredientBuilders);
            }
        }
        interface IngredientList<N> {
            N ingredients(Collection<IngredientBuilder.IngredientCreator> ingredientBuilder);
        }
        interface Result<N> {
            N result(int count, IngredientBuilder.ItemCreator ingredientBuilder);
        }
        interface Experience<N> {
            N experience(double experience);
        }
        interface CookingTime<N> {
            N cookingTime(int cookingTime);
            default N defaultCookingTime() {
                return cookingTime(-1);
            }
        }

        interface ShapedIngredients<N> {
            N ingredients(Consumer<ShapedIngredientAccumulator> ingredientBuilders);
            interface ShapedIngredientAccumulator {
                ShapedIngredientAccumulator put(VanillaRecipe.Slot slot, IngredientBuilder.IngredientCreator... ingredientBuilders);
            }
        }
    }
}
