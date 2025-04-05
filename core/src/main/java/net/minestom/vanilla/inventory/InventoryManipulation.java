package net.minestom.vanilla.inventory;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.item.ItemStack;

import java.util.Objects;

public class InventoryManipulation {
    public static void consumeItemIfNotCreative(Player player, ItemStack itemStack, PlayerHand hand) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        player.setItemInHand(hand, itemStack);
    }

    /**
     * If this function returns false, the operation did not complete.
     *
     * @return true if there was enough items to consume, false otherwise.
     */
    public static boolean consumeItemIfNotCreative(Player player, PlayerHand hand, int amount) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }

        ItemStack item = player.getItemInHand(hand);
        item = item.withAmount(amt -> amt - amount);
        if (item.amount() == 0) item = ItemStack.AIR;
        if (item.amount() < 0) return false;
        player.setItemInHand(hand, item);
        return true;
    }

    public static void damageItemIfNotCreative(Player player, PlayerHand hand, int amount) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        ItemStack itemStack = player.getItemInHand(hand);
        int damage = Objects.requireNonNullElse(itemStack.get(DataComponents.DAMAGE), 0);
        int maxDamage = Objects.requireNonNull(itemStack.material().registry().prototype().get(DataComponents.MAX_DAMAGE));
        ItemStack newItem = itemStack.with(DataComponents.DAMAGE, damage + amount);
        if (damage + amount >= maxDamage) {
            newItem = ItemStack.AIR;
            // TODO: Item Break Event
        }
        player.setItemInHand(hand, newItem);
    }
}
