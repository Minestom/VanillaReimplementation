package net.minestom.vanilla.inventory;

import com.google.common.collect.Streams;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;

import java.util.Arrays;

public class ChestInventory extends Inventory {

    protected final NBTList<NBTCompound> items;

    public ChestInventory(@NotNull NBTList<NBTCompound> items) {
        super(InventoryType.CHEST_3_ROW, Component.text("chest"));
        this.items = items;
    }

    @Override
    public @NotNull ItemStack getItemStack(int slot) {
        NBTCompound item = items.get(slot);

        if (item == null) {
            return ItemStack.AIR;
        }

        ItemStack itemStack = ItemStackUtils.fromNBTCompound(item);

        if (itemStack == null) {
            return ItemStack.AIR;
        }

        return itemStack;
    }

    @Override
    public void setItemStack(int slot, @NotNull ItemStack itemStack) {
        items.set(slot, ItemStackUtils.toNBTCompound(itemStack));
    }

    @Override
    public @NotNull ItemStack[] getItemStacks() {
        return Streams.stream(items)
                .map(ItemStackUtils::fromNBTCompound)
                .toArray(ItemStack[]::new);
    }
}
