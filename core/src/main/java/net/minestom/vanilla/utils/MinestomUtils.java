package net.minestom.vanilla.utils;

import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.RegistryKey;

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

    public static int getEnchantLevel(ItemStack itemStack, Enchantment enchantment, int defaultValue) {
        RegistryKey<Enchantment> enchant = getEnchantKey(enchantment);
        if (enchant == null) return defaultValue;
        EnchantmentList enchants = itemStack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null) return defaultValue;
        if (!enchants.has(enchant)) return defaultValue;
        return enchants.level(enchant);
    }

    public static RegistryKey<Enchantment> getEnchantKey(Enchantment enchantment) {
        return MinecraftServer.getEnchantmentRegistry().getKey(enchantment);
    }
}
