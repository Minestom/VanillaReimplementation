package net.minestom.vanilla.blockentity;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.utils.BlockPosition;

/**
 * Block entity for a chest-like block. Holds a single inventory
 */
public class ChestBlockEntity extends BlockEntity {

    private final Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, "Chest");

    public ChestBlockEntity(BlockPosition position) {
        super(position);
        set("inventory", inventory,  Inventory.class);
    }

    public Inventory getInventory() {
        return inventory;
    }
}
