package net.minestom.vanilla.inventory;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DoubleChestInventory extends Inventory {
    private final Inventory left;
    private final Inventory right;

    public DoubleChestInventory(Inventory left, Inventory right, String title) {
        super(InventoryType.CHEST_6_ROW, title);
        this.left = left;
        this.right = right;
    }

    @Override
    public ItemStack getItemStack(int slot) {
        if(slot < left.getSize()) {
            return left.getItemStack(slot);
        }
        return right.getItemStack(slot-left.getSize());
    }

    @Override
    public void setItemStack(int slot, @NotNull ItemStack itemStack) {
        if (slot < left.getSize()) {
            left.setItemStack(slot, itemStack);
        } else {
            right.setItemStack(slot-left.getSize(), itemStack);
        }
    }

    @Override
    public ItemStack[] getItemStacks() {
        ItemStack[] stacks = new ItemStack[getSize()];
        System.arraycopy(left.getItemStacks(), 0, stacks, 0, left.getSize());
        System.arraycopy(right.getItemStacks(), 0, stacks, left.getSize(), right.getSize());
        return stacks;
    }
}
