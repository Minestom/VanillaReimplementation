package net.minestom.vanilla.gamedata;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registries;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;

// for lack of a better name
public class NBTUtils {

    /**
     * Loads all the items from the 'items' list into the given inventory
     * @param items
     * @param destination
     */
    public static void loadAllItems(NBTList<NBTCompound> items, Inventory destination) {
        // TODO: clear inventory
        for(NBTCompound tag : items) {
            Material item = Registries.getMaterial(tag.getString("id"));
            if(item == Material.AIR) {
                item = Material.STONE;
            }
            ItemStack stack = new ItemStack(item, tag.getByte("Count"));
            destination.setItemStack(tag.getByte("Slot"), stack);
        }
    }

    public static void saveAllItems(NBTList<NBTCompound> list, Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItemStack(i);
            NBTCompound nbt = new NBTCompound();
            nbt.setByte("Slot", (byte) i);
            nbt.setByte("Count", stack.getAmount());
            nbt.setString("id", stack.getMaterial().getName());

            list.add(nbt);
        }
    }
}
