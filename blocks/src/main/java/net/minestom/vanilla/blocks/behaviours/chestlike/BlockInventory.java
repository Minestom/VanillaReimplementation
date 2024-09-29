package net.minestom.vanilla.blocks.behaviours.chestlike;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import net.minestom.vanilla.blocks.behaviours.InventoryBlockBehaviour;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockInventory extends Inventory {
    private static final Map<Point, BlockInventory> BLOCK_INVENTORY_MAP = new ConcurrentHashMap<>();

    protected final ItemStack[] items;
    protected final Instance instance;
    protected final Point pos;

    private BlockInventory(Instance instance, Point pos, InventoryType inventoryType, Component title) {
        super(inventoryType, title);

        this.instance = instance;
        this.pos = pos;

        // Set items
        List<ItemStack> itemsList = instance.getBlock(pos).getTag(InventoryBlockBehaviour.TAG_ITEMS);
        if (itemsList == null) {
            ItemStack[] newItems = new ItemStack[inventoryType.getSize()];
            Arrays.fill(newItems, ItemStack.AIR);
            this.items = newItems;
        } else {
            this.items = itemsList.toArray(ItemStack[]::new);
        }
    }

    public static BlockInventory from(Instance instance, Point pos, InventoryType inventoryType, Component title) {
        BlockInventory inv = BLOCK_INVENTORY_MAP.get(pos);
        if (inv == null) {
            inv = new BlockInventory(instance, pos, inventoryType, title);
            BLOCK_INVENTORY_MAP.put(pos, inv);
        }
        if (inv.getInventoryType() != inventoryType) {
            throw new IllegalStateException("Inventory type mismatch");
        }
        if (!inv.getTitle().equals(title)) {
            throw new IllegalStateException("Inventory title mismatch");
        }
        return inv;
    }

    public static @NotNull List<ItemStack> remove(Instance instance, Point pos) {
        BlockInventory inv = BLOCK_INVENTORY_MAP.get(pos);
        if (inv == null) {
            return List.of();
        }
        BLOCK_INVENTORY_MAP.remove(pos);
        return inv.itemStacks();
    }

    @Override
    public @NotNull ItemStack getItemStack(int slot) {
        ItemStack item = items[slot];

        if (item == null) {
            return ItemStack.AIR;
        }

        return item;
    }

    @Override
    public void setItemStack(int slot, @NotNull ItemStack itemStack) {
        items[slot] = itemStack;
        instance.setBlock(pos, instance.getBlock(pos).withTag(InventoryBlockBehaviour.TAG_ITEMS, List.of(items)));
    }

    @Override
    @Deprecated
    public ItemStack @NotNull [] getItemStacks() {
        return itemStacks().toArray(ItemStack[]::new);
    }

    public @NotNull List<ItemStack> itemStacks() {
        return List.of(items);
    }
}
