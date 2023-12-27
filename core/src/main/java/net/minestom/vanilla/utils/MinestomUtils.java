package net.minestom.vanilla.utils;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;

public class MinestomUtils {

    /**
     * Initializes the resources of Minestom.
     * This is used to not interfere with the timing of initialising the vanilla modules.
     */
    public static void initialize() {
        Block.values();
        Material.values();
        EntityType.values();
    }
}
