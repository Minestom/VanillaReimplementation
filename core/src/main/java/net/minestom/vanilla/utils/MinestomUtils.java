package net.minestom.vanilla.utils;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;

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

    public static int getEnchantLevel(ItemStack itemStack, Key enchantment, int defaultValue) {
        DynamicRegistry.Key<Enchantment> enchant = getEnchantKey(enchantment);
        if (enchant == null) return defaultValue;
        EnchantmentList enchants = itemStack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null) return defaultValue;
        if (!enchants.has(enchant)) return defaultValue;
        return enchants.level(enchant);
    }

    public static DynamicRegistry.Key<Enchantment> getEnchantKey(Enchantment enchantment) {
        return MinecraftServer.getEnchantmentRegistry().getKey(enchantment);
    }

    public static DynamicRegistry.Key<Enchantment> getEnchantKey(Key enchantment) {
        int id = MinecraftServer.getEnchantmentRegistry().getId(enchantment);
        return MinecraftServer.getEnchantmentRegistry().getKey(id);
    }
}
