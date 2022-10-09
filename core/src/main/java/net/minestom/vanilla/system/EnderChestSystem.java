package net.minestom.vanilla.system;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnderChestSystem {

    public static final EnderChestSystem INSTANCE = new EnderChestSystem();

    private final Map<UUID, List<ItemStack>> itemsMap = new HashMap<>();

    private EnderChestSystem() {
    }

    public List<ItemStack> getItems(@NotNull Player player) {
        return getItems(player.getUuid());
    }

    public @NotNull List<ItemStack> getItems(@NotNull UUID uuid) {
        return itemsMap.computeIfAbsent(uuid, k -> List.of());
    }

    public static EnderChestSystem getInstance() {
        return INSTANCE;
    }
}
