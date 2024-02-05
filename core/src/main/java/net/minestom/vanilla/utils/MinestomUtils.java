package net.minestom.vanilla.utils;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;

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
