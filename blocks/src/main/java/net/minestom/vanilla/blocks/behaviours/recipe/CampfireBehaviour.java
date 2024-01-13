package net.minestom.vanilla.blocks.behaviours.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import net.minestom.server.coordinate.Point;
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
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagSerializer;
import net.minestom.server.tag.TagWritable;
import net.minestom.vanilla.blocks.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.inventory.InventoryManipulation;

public class CampfireBehaviour extends VanillaBlockBehaviour {

    public static final Tag<List<ItemStack>> ITEMS_KEY = Tag.View(new CampfireItemsSerializer());
    private final RecipeManager recipeManager;

    public CampfireBehaviour(VanillaBlocks.@NotNull BlockContext context) {
        super(context);
        this.recipeManager = context.vri().process().recipe();
    }

    public @Nullable List<ItemStack> getItems(Block block) {
        return block.getTag(ITEMS_KEY);
    }

    public @NotNull Block withItems(Block block, @NotNull List<ItemStack> items) {
        if (items.size() > 4)
            throw new IllegalArgumentException("Items size is more than 4 in CampfireBehaviour#withItems.");
        if (items.stream().anyMatch(item -> findCampfireCookingRecipe(item).isEmpty()))
            throw new IllegalArgumentException("Items passed with CampfireBehaviour#withItems contains item that doesn't have CAMPFIRE_COOKING recipe.");
        return block.withTag(ITEMS_KEY, items);
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
        return List.of(ITEMS_KEY);
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

    private static class CampfireItemsSerializer implements TagSerializer<List<ItemStack>> {

        private final Tag<NBT> internal = Tag.NBT("Items");

        @Override
        public @Nullable List<ItemStack> read(@NotNull TagReadable reader) {
            NBTList<NBTCompound> item = (NBTList<NBTCompound>) reader.getTag(internal);
            if (item == null)
                return null;
            List<ItemStack> result = new ArrayList<>();
            item.forEach(nbtCompound -> {
                byte amount = nbtCompound.getAsByte("Count");
                String id = nbtCompound.getString("id");
                Material material = Material.fromNamespaceId(id);
                result.add(ItemStack.of(material, amount));
            });
            return result;
        }

        @Override
        public void write(@NotNull TagWritable writer, @Nullable List<ItemStack> value) {
            if (value == null) {
                writer.removeTag(internal);
                return;
            }
            writer.setTag(internal, NBT.List(
                    NBTType.TAG_Compound,
                    IntStream.range(0, value.size())
                            .mapToObj(itemIndex -> NBT.Compound(nbt -> {
                                ItemStack item = value.get(itemIndex);
                                nbt.setByte("Count", (byte) item.amount());
                                nbt.setByte("Slot", (byte) itemIndex);
                                nbt.setString("id", item.material().name());
                            }))
                            .toList()
            ));
        }

    }

}
