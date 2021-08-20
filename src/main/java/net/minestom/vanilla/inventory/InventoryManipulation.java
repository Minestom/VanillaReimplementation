package net.minestom.vanilla.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;

public class InventoryManipulation {
    public static void consumeItemIfNotCreative(Player player, ItemStack itemStack, Player.Hand hand) {
        if(player.isCreative()) {
            return;
        }

        StackingRule stackingRule = itemStack.getStackingRule();
        ItemStack newUsedItem = stackingRule.apply(itemStack, stackingRule.getAmount(itemStack) - 1);


        player.getInventory().setItemInHand(hand, newUsedItem);
    }

    public static void damageItemIfNotCreative(Player player, ItemStack itemStack, Player.Hand hand) {
        if (player.isCreative()) {
            return;
        }

        int damage = itemStack.getMeta().getDamage();
        ItemStack newItem = itemStack.withMeta(meta -> meta.damage(damage + 1));

        player.getInventory().setItemInHand(hand, newItem);
    }
}
