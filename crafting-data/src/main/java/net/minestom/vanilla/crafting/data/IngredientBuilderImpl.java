package net.minestom.vanilla.crafting.data;

import net.minestom.vanilla.crafting.VanillaRecipe;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

class IngredientBuilderImpl implements VanillaRecipeBuilder.IngredientBuilder {
    @Override
    public Item item() {
        return new ItemBuilderImpl();
    }

    @Override
    public Tag tag() {
        return new TagBuilderImpl();
    }

    private static class ItemBuilderImpl implements Item, Item.DataOrFinishStage {
        @Override
        public DataOrFinishStage name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Item.FinishStage extraData(Map<String, NBTCompound> extraData) {
            this.extraData = extraData;
            return this;
        }

        String name;
        Map<String, NBTCompound> extraData;

        @Override
        public VanillaRecipe.Ingredient.Item build() {
            return new VanillaRecipe.Ingredient.Item(name, extraData);
        }
    }

    private static class TagBuilderImpl implements Tag, Tag.FinishStage {
        @Override
        public Tag.FinishStage name(String name) {
            this.name = name;
            return this;
        }

        String name;

        @Override
        public VanillaRecipe.Ingredient.Tag build() {
            return new VanillaRecipe.Ingredient.Tag(name);
        }
    }
}
