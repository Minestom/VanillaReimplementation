package net.minestom.vanilla.blocks.behaviours.chestlike;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;

/**
 * The key difference between BlockItems and BlockInventory is that BlockItems
 * does not require specifying an inventoryType or title. It is designed for simpler
 * container block management.
 */
public class BlockItems {
    private static final Tag<List<ItemStack>> TAG_ITEMS = Tag.ItemStack("vri:container_items").list();
    public static final Tag<BlockItems> BLOCK_ITEMS_TAG = Tag.Transient("vri:block_items");

    protected final Instance instance;
    protected final Point pos;

    private BlockItems(Instance instance, Point pos) {
        this.instance = instance;
        this.pos = pos;
    }

    public static BlockItems from(Instance instance, Point pos, int size) {
        Block block = instance.getBlock(pos);
        BlockItems container = block.getTag(BLOCK_ITEMS_TAG);
        if (container == null) {
            container = new BlockItems(instance, pos);
            instance.setBlock(pos, block.withTag(BLOCK_ITEMS_TAG, container));
        }
        if (container.getSize() != size) {
            throw new IllegalStateException("Inventory size mismatch");
        }
        return container;
    }

    public static @NotNull List<ItemStack> remove(Instance instance, Point pos) {
        Block block = instance.getBlock(pos);
        var container = block.getTag(BLOCK_ITEMS_TAG);
        if (container == null) {
            return List.of();
        }
        instance.setBlock(pos, block.withTag(BLOCK_ITEMS_TAG, null));
        return container.itemStacks();
    }

    private List<ItemStack> items() {
        return instance.getBlock(pos).getTag(TAG_ITEMS);
    }

    public int getSize() {
        return items().size();
    }

    public Block setItemStack(int slot, @NotNull ItemStack itemStack) {
        List<ItemStack> newItems = new ArrayList<>(items());
        newItems.set(slot, itemStack);
        Block block = instance.getBlock(pos).withTag(TAG_ITEMS, newItems);
        instance.setBlock(pos, block);
        return block;
    }

    public @UnmodifiableView @NotNull List<ItemStack> itemStacks() {
        return Collections.unmodifiableList(items());
    }
}
