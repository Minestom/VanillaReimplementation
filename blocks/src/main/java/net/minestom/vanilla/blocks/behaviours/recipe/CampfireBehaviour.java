package net.minestom.vanilla.blocks.behaviours.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.recipe.CampfireCookingRecipe;
import net.minestom.server.recipe.Recipe.Type;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.tag.Tag;
import net.minestom.vanilla.blocks.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.inventory.InventoryManipulation;
import net.minestom.vanilla.tag.Tags.Blocks.Campfire;

public class CampfireBehaviour extends VanillaBlockBehaviour {

    private final RecipeManager recipeManager;

    public CampfireBehaviour(VanillaBlocks.@NotNull BlockContext context) {
        super(context);
        this.recipeManager = context.vri().process().recipe();
    }

    public @Nullable List<ItemStack> getItems(Block block) {
        List<Campfire.ItemWithSlot> items = block.getTag(Campfire.ITEMS);
        if(items == null)
            return null;
        return items.stream().map(item -> ItemStack.of(item.material())).collect(Collectors.toList());
    }

    public @NotNull Block withItems(Block block, @NotNull List<ItemStack> items) {
        if (items.size() > 4)
            throw new IllegalArgumentException("Items size is more than 4 in CampfireBehaviour#withItems.");
        if (items.stream().anyMatch(item -> findCampfireCookingRecipe(item).isEmpty()))
            throw new IllegalArgumentException("Items passed with CampfireBehaviour#withItems contains item that doesn't have CAMPFIRE_COOKING recipe.");
        List<Campfire.ItemWithSlot> slotItems = IntStream.range(0, items.size())
                .mapToObj(slot -> new Campfire.ItemWithSlot(items.get(slot).material(), slot))
                .toList();
        return block.withTag(Campfire.ITEMS, slotItems);
    }

    @Override
    public boolean onInteract(@NotNull BlockHandler.Interaction interaction) {
        Instance instance = interaction.getInstance();
        Point pos = interaction.getBlockPosition();
        Player player = interaction.getPlayer();
        Block campfire = interaction.getBlock();
        ItemStack input = player.getItemInHand(interaction.getHand());
        Optional<CampfireCookingRecipe> recipeOptional = findCampfireCookingRecipe(input);
        List<ItemStack> campfireItems = Optional.ofNullable(getItems(campfire)).orElse(new ArrayList<>());

        if (recipeOptional.isEmpty())
            return true;
        if (campfireItems.size() >= 4)
            return true;

        boolean itemNotConsumed = !InventoryManipulation.consumeItemIfNotCreative(player, interaction.getHand(), 1);

        if (itemNotConsumed)
            return true;

        campfireItems.add(input.withAmount(1));
        instance.setBlock(pos, withItems(campfire, campfireItems));
        return false;
    }

    @Override
    public void tick(@NotNull BlockHandler.Tick tick) {
        super.tick(tick);
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(Campfire.ITEMS);
    }

    @Override
    public boolean isTickable() {
        return true;
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
