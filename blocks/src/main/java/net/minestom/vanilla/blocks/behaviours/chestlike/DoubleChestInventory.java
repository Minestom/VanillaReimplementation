package net.minestom.vanilla.blocks.behaviours.chestlike;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class DoubleChestInventory extends Inventory {
    private final BlockInventory left;
    private final BlockInventory right;

    public DoubleChestInventory(BlockInventory left, BlockInventory right, String title) {
        super(InventoryType.CHEST_6_ROW, title);
        this.left = left;
        this.right = right;
    }

    @Override
    public @NotNull ItemStack getItemStack(int slot) {
        if (slot < left.getSize()) {
            return left.getItemStack(slot);
        }
        return right.getItemStack(slot - left.getSize());
    }

    @Override
    public void setItemStack(int slot, @NotNull ItemStack itemStack) {
        if (slot < left.getSize()) {
            left.setItemStack(slot, itemStack);
        } else {
            right.setItemStack(slot - left.getSize(), itemStack);
        }
    }

    @Override
    @Deprecated
    public ItemStack[] getItemStacks() {
        return itemStacks().toArray(ItemStack[]::new);
    }

    public List<ItemStack> itemStacks() {
        return Stream.of(left, right)
                .map(BlockInventory::getItemStacks)
                .map(List::of)
                .flatMap(Collection::stream)
                .toList();
    }
}
