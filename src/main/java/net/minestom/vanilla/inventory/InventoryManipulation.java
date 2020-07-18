package net.minestom.vanilla.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;

public class InventoryManipulation {
    public static void consumeItemIfNotCreative(Player player, ItemStack itemStack, Player.Hand hand) {
        if(!player.isCreative()) {
            StackingRule stackingRule = itemStack.getStackingRule();
            ItemStack newUsedItem = stackingRule.apply(itemStack, stackingRule.getAmount(itemStack) - 1);

            if (hand == Player.Hand.OFF) {
                player.getInventory().setItemInOffHand(newUsedItem);
            } else { // Main
                player.getInventory().setItemInMainHand(newUsedItem);
            }
        }
    }
}
