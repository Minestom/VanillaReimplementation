package net.minestom.vanilla.blocks.behaviours.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.IntStream;

import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.recipe.CampfireCookingRecipe;
import net.minestom.server.recipe.Recipe.Type;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.tag.Tag;
import net.minestom.vanilla.blocks.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.blocks.behaviours.chestlike.BlockItems;
import net.minestom.vanilla.inventory.InventoryManipulation;
import net.minestom.vanilla.tag.Tags.Blocks.Campfire;

public class CampfireBehaviour extends VanillaBlockBehaviour {

    private static final int CONTAINER_SIZE = 4;
    private static final Random RNG = new Random();
    private final RecipeManager recipeManager;

    public CampfireBehaviour(VanillaBlocks.@NotNull BlockContext context) {
        super(context);
        this.recipeManager = context.vri().process().recipe();
    }

    public BlockItems getBlockItems(Block block) {
        return BlockItems.from(block, CONTAINER_SIZE);
    }

    public @NotNull Block withCookingProgress(Block block, int slotIndex, int cookingTime) {
        List<Integer> cookingProgress = new ArrayList<>(block.getTag(Campfire.COOKING_PROGRESS));
        cookingProgress.set(slotIndex, cookingTime);
        return block.withTag(Campfire.COOKING_PROGRESS, cookingProgress);
    }

    /**
     * Appends an item to the first available slot in the campfire.
     * @return the index of the slot the item was appended to.
     */
    public int appendItem(BlockItems items, @NotNull CampfireCookingRecipe recipe) {
        OptionalInt freeSlot = findFirstFreeSlot(items.itemStacks());

        if (freeSlot.isEmpty())
            throw new IllegalArgumentException("Campfire doesn't have free slot for appending an item in CampfireBehaviour#appendItem");

        List<ItemStack> ingredients = recipe.getIngredient().items();

        if (ingredients == null || ingredients.size() != 1)
            throw new IllegalArgumentException("CampfireCookingRecipe has invalid ingredients in CampfireBehaviour#appendItem.");

        int index = freeSlot.getAsInt();
        items.set(index, ingredients.get(0));

        return index;
    }

    @Override
    public boolean onInteract(@NotNull BlockHandler.Interaction interaction) {
        Instance instance = interaction.getInstance();
        Point pos = interaction.getBlockPosition();
        Block block = interaction.getBlock();
        Player player = interaction.getPlayer();
        ItemStack input = player.getItemInHand(interaction.getHand());
        Optional<CampfireCookingRecipe> recipeOptional = findCampfireCookingRecipe(input);

        if (recipeOptional.isEmpty())
            return true;

        BlockItems items = getBlockItems(block);
        if (findFirstFreeSlot(items.itemStacks()).isEmpty())
            return true;

        boolean itemNotConsumed = !InventoryManipulation.consumeItemIfNotCreative(player, interaction.getHand(), 1);

        if (itemNotConsumed)
            return true;

        CampfireCookingRecipe recipe = recipeOptional.get();
        int index = appendItem(items, recipe);
        block = withCookingProgress(block, index, recipe.getCookingTime());
        instance.setBlock(pos, items.apply(block));
        return false;
    }

    @Override
    public void onDestroy(@NotNull BlockHandler.Destroy destroy) {
        Instance instance = destroy.getInstance();
        Point pos = destroy.getBlockPosition();
        Block block = destroy.getBlock();

        // TODO: Introduce a way to get the block this is getting replaced with, enabling us to remove the tick delay.
        instance.scheduleNextTick(ignored -> {
            Block newBlock = instance.getBlock(pos);
            if (newBlock.compare(block)) {
                // Same block, don't remove campfire
                return;
            }

            // Different block, remove campfire
            List<ItemStack> items = BlockItems.from(newBlock).itemStacks();

            for (ItemStack item : items) {

                if (item == null) {
                    continue;
                }

                dropItem(instance, pos, item);
            }
        });
    }

    @Override
    public void tick(@NotNull BlockHandler.Tick tick) {
        Instance instance = tick.getInstance();
        Block block = tick.getBlock();
        Point pos = tick.getBlockPosition();
        BlockItems items = getBlockItems(block);

        if (items.isAir())
            return;

        List<Integer> cookingProgress = new ArrayList<>(block.getTag(Campfire.COOKING_PROGRESS));

        boolean lit = Boolean.parseBoolean(block.getProperty("lit"));
        if (!lit) {
            for (ItemStack item : items.itemStacks())
                dropItem(instance, pos, item);
            items.setItems(Collections.nCopies(4, ItemStack.AIR));
            instance.setBlock(pos, items.apply(block));
            return;
        }

        for (ListIterator<Integer> i = cookingProgress.listIterator(); i.hasNext(); ) {
            int index = i.nextIndex();
            Integer progress = i.next();
            Material inputMaterial = items.get(index).material();

            if (Material.AIR.equals(inputMaterial))
                continue;

            if (progress <= 0) {
                endCampfireCookingProgress(tick.getInstance(), tick.getBlockPosition(), ItemStack.of(inputMaterial));
                items.set(index, ItemStack.AIR);
                continue;
            }

            progress -= 1;
            i.set(progress);
        }

        block = items.apply(block);
        block = block.withTag(Campfire.COOKING_PROGRESS, cookingProgress);
        instance.setBlock(pos, block);
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(Campfire.ITEMS);
    }

    @Override
    public boolean isTickable() {
        return true;
    }

    private void endCampfireCookingProgress(Instance instance, Point pos, ItemStack input) {
        Optional<CampfireCookingRecipe> recipeOptional = findCampfireCookingRecipe(input);
        if (recipeOptional.isEmpty())
            throw new IllegalArgumentException("Cannot end campfire cooking progress because input recipe doesn't found");
        dropItem(instance, pos, recipeOptional.get().getResult());
    }

    private void dropItem(Instance instance, Point pos, ItemStack item) {
        ItemEntity resultItemEntity = new ItemEntity(item);
        resultItemEntity.setInstance(instance);
        resultItemEntity.teleport(new Pos(pos.x() + RNG.nextDouble(), pos.y() + .5f, pos.z() + RNG.nextDouble()));
    }

    private OptionalInt findFirstFreeSlot(List<ItemStack> items) {
        return IntStream.range(0, items.size()).filter(index -> items.get(index).isAir()).findFirst();
    }

    private Optional<CampfireCookingRecipe> findCampfireCookingRecipe(ItemStack input) {
        if (input == null)
            return Optional.empty();
        return recipeManager
                .getRecipes().stream()
                .filter(recipe -> recipe.getRecipeType() == Type.CAMPFIRE_COOKING)
                .map(CampfireCookingRecipe.class::cast)
                .filter(recipe -> {
                    List<ItemStack> items = recipe.getIngredient().items();
                    if (items == null)
                        return false;
                    if (items.size() != 1)
                        return false;
                    return items.get(0).material().equals(input.material());
                }).findFirst();
    }

}
