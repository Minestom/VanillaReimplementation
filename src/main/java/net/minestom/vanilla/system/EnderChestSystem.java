package net.minestom.vanilla.system;

import net.minestom.server.data.SerializableData;
import net.minestom.server.data.SerializableDataImpl;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderChestSystem {

    public static final EnderChestSystem INSTANCE = new EnderChestSystem();

    private Map<UUID, NBTList<NBTCompound>> itemsMap = new HashMap<>();

    private EnderChestSystem() {}

    public NBTList<NBTCompound> getItems(@NotNull Player player) {
        return getItems(player.getUuid());
    }

    public @NotNull NBTList<NBTCompound> getItems(@NotNull UUID uuid) {
        var items = itemsMap.get(uuid);

        if (items == null) {
            items = new NBTList<>(NBTTypes.TAG_Compound);
            itemsMap.put(uuid, items);
        }

        return items;
    }

    public static EnderChestSystem getInstance() {
        return INSTANCE;
    }
}
