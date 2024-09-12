package net.minestom.vanilla.blocks.behaviours.chestlike;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minestom.server.item.Material;
import net.minestom.vanilla.tag.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;

/**
 * The key difference between BlockItems and BlockInventory is that BlockItems
 * does not require specifying an inventoryType or title. It is designed for simpler
 * container block management.
 */
public class BlockItems {

    private static final Tag<List<ItemStack>> FALLBACK_TAG_ITEMS = Tag.ItemStack("vri:container_items")
            .list();

    private static Map<String, Tag<List<ItemStack>>> TAG_ITEMS_BY_BLOCK;

    private static Tag<List<ItemStack>> getTagForBlock(Block block) {
        return TAG_ITEMS_BY_BLOCK.getOrDefault(block.name(), FALLBACK_TAG_ITEMS).defaultValue(List.of());
    }

    private final List<ItemStack> items;

    private BlockItems(List<ItemStack> items) {
        this.items = new ArrayList<>(items);
    }

    public static BlockItems from(Block block) {
        return new BlockItems(block.getTag(getTagForBlock(block)));
    }

    public static BlockItems from(Block block, int requireStacks) {
        BlockItems items = from(block);
        if (items.size() != requireStacks) {
            items.requireStacks(requireStacks);
        }
        return items;
    }

    private void requireStacks(int requireStacks) {
        // remove from the top, or add to the top.
        if (items.size() < requireStacks) {
            items.addAll(Collections.nCopies(requireStacks - items.size(), ItemStack.AIR));
        } else if (items.size() > requireStacks) {
            items.subList(requireStacks, items.size()).clear();
        }
    }

    public @UnmodifiableView @NotNull List<ItemStack> itemStacks() {
        return Collections.unmodifiableList(items);
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean isAir() {
        return items.isEmpty() || items.stream().allMatch(ItemStack::isAir);
    }

    public Block apply(Block block) {
        return block.withTag(getTagForBlock(block), items);
    }

    public void setItems(List<ItemStack> itemStacks) {
        items.clear();
        items.addAll(itemStacks);
    }

    public ItemStack get(int index) {
        return items.get(index);
    }

    public void set(int index, ItemStack item) {
        items.set(index, item);
    }

    static {
        Map<Block, Tag<List<ItemStack>>> tagItemsByBlock = new HashMap<>();

        // some blocks need to be represented by a specific nbt tag to be visible to the client.
        tagItemsByBlock.put(Block.CAMPFIRE, Tags.Blocks.Campfire.ITEMS);

        BlockItems.TAG_ITEMS_BY_BLOCK = Map.copyOf(tagItemsByBlock.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey().name(), entry.getValue()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
}
