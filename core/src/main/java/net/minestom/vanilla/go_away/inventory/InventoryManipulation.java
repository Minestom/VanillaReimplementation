package net.minestom.vanilla.go_away.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public class InventoryManipulation {
    public static void consumeItemIfNotCreative(Player player, ItemStack itemStack, Player.Hand hand) {
        if (player.isCreative()) {
            return;
        }

        player.getInventory().setItemInHand(hand, itemStack);
    }

    public static void damageItemIfNotCreative(Player player, ItemStack itemStack, Player.Hand hand) {
        if (player.isCreative()) {
            return;
        }

        int damage = itemStack.meta().getDamage();
        ItemStack newItem = itemStack.withMeta(meta -> meta.damage(damage + 1));

        player.getInventory().setItemInHand(hand, newItem);
    }
}
