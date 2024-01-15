package net.minestom.vanilla.blocks.behaviours.chestlike;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;

// TODO: Update the tags within the blocks instead of using this map
/**
 * The key difference between BlockContainer and BlockInventory is that BlockContainer
 * does not require specifying an inventoryType or title. It is designed for simpler
 * container block management.
 */
public class BlockContainer {
    public static final Tag<List<ItemStack>> TAG_ITEMS = Tag.ItemStack("vri:container_items").list();
    private static final Map<Instance, Map<Point, BlockContainer>> INSTANCE2CONTAINERS = new ConcurrentHashMap<>();

    protected final Tag<List<ItemStack>> tag;
    protected final List<ItemStack> items;
    protected final Instance instance;
    protected final Point pos;

    private BlockContainer(Instance instance, Point pos, int size, Tag<List<ItemStack>> tag) {
        this.tag = tag;

        this.instance = instance;
        this.pos = pos;

        // Set items
        List<ItemStack> itemsList = instance.getBlock(pos).getTag(tag);
        this.items = Objects.requireNonNullElseGet(itemsList, () -> new ArrayList<>(Collections.nCopies(size, ItemStack.AIR)));
    }

    public static BlockContainer from(Instance instance, Point pos, int size) {
        return from(instance, pos, size, TAG_ITEMS);
    }

    public static BlockContainer from(Instance instance, Point pos, int size, Tag<List<ItemStack>> tag) {
        BlockContainer container = INSTANCE2CONTAINERS.computeIfAbsent(instance, k -> new WeakHashMap<>())
                .computeIfAbsent(pos, k -> new BlockContainer(instance, pos, size, tag));
        if (container.getSize() != size) {
            throw new IllegalStateException("Inventory size mismatch");
        }
        if (!container.getTag().getKey().equals(tag.getKey())) {
            throw new IllegalStateException("Inventory tag key mismatch");
        }
        return container;
    }

    public static @NotNull List<ItemStack> remove(Instance instance, Point pos) {
        return Optional.ofNullable(INSTANCE2CONTAINERS.computeIfAbsent(instance, k -> new WeakHashMap<>())
                .remove(pos))
                .map(BlockContainer::itemStacks)
                .orElse(Collections.emptyList());
    }

    public int getSize() {
        return items.size();
    }

    public Tag<List<ItemStack>> getTag() {
        return tag;
    }

    public @NotNull ItemStack getItemStack(int slot) {
        ItemStack item = items.get(slot);

        if (item == null) {
            return ItemStack.AIR;
        }

        return item;
    }

    public Block setItemStack(int slot, @NotNull ItemStack itemStack) {
        items.set(slot, itemStack);
        Block block = instance.getBlock(pos).withTag(tag, items);
        instance.setBlock(pos, block);
        return block;
    }

    public @UnmodifiableView @NotNull List<ItemStack> itemStacks() {
        return Collections.unmodifiableList(items);
    }
}
