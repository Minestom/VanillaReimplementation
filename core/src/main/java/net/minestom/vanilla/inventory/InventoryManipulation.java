package net.minestom.vanilla.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public class InventoryManipulation {
    public static void consumeItemIfNotCreative(Player player, ItemStack itemStack, Player.Hand hand) {
        if (player.isCreative()) {
            return;
        }

        player.getInventory().setItemInHand(hand, itemStack);
    }

    /**
     * If this function returns false, the operation did not complete.
     * @return true if there was enough items to consume, false otherwise.
     */
    public static boolean consumeItemIfNotCreative(Player player, Player.Hand hand, int amount) {
        if (player.isCreative()) {
            return true;
        }

        ItemStack item = player.getItemInHand(hand);
        item = item.withAmount(amt -> amt - amount);
        if (item.amount() == 0) item = ItemStack.AIR;
        if (item.amount() < 0) return false;
        player.getInventory().setItemInHand(hand, item);
        return true;
    }

    public static void damageItemIfNotCreative(Player player, Player.Hand hand, int amount) {
        if (player.isCreative()) {
            return;
        }

        ItemStack itemStack = player.getItemInHand(hand);
        int damage = itemStack.meta().getDamage();
        int maxDamage = itemStack.material().registry().maxDamage();
        ItemStack newItem = itemStack.withMeta(meta -> meta.damage(damage + amount));
        if (damage + amount >= maxDamage) {
            newItem = ItemStack.AIR;
            // TODO: Item Break Event
        }
        player.getInventory().setItemInHand(hand, newItem);
    }
}
