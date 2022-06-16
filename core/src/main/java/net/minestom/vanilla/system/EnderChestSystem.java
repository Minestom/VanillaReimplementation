package net.minestom.vanilla.system;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnderChestSystem {

    public static final EnderChestSystem INSTANCE = new EnderChestSystem();

    private final Map<UUID, List<NBTCompound>> itemsMap = new HashMap<>();

    private EnderChestSystem() {
    }

    public List<NBTCompound> getItems(@NotNull Player player) {
        return getItems(player.getUuid());
    }

    public @NotNull List<NBTCompound> getItems(@NotNull UUID uuid) {
        return itemsMap.computeIfAbsent(uuid, k -> List.of());
    }

    public static EnderChestSystem getInstance() {
        return INSTANCE;
    }
}
