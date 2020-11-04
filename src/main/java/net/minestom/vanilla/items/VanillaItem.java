package net.minestom.vanilla.items;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;

public abstract class VanillaItem {

    private final Material vanillaItem;

    public VanillaItem(Material vanillaItem) {
        this.vanillaItem = vanillaItem;
    }


    public Material getMaterial() {
        return vanillaItem;
    }

    /**
     * Called when the player right clicks with this item in the air
     *
     * @param player
     * @param itemStack
     * @param hand
     */
    public abstract void onUseInAir(Player player, ItemStack itemStack, Player.Hand hand);

    /**
     * Called when the player right clicks with this item on a block
     *
     * @param player
     * @param itemStack
     * @param hand
     * @param position
     * @param blockFace
     * @return true if it prevents normal item use (placing blocks for instance)
     */
    public abstract boolean onUseOnBlock(Player player, ItemStack itemStack, Player.Hand hand, BlockPosition position, Direction blockFace);

    public static void damageItem(Player player, Player.Hand hand, ItemStack itemStack) {
        ItemStack newUsedItem = itemStack.copy();
        newUsedItem.setDamage((byte) (itemStack.getDamage() + 1));

        if (hand == Player.Hand.OFF) {
            player.getInventory().setItemInOffHand(newUsedItem);
        } else { // Main
            player.getInventory().setItemInMainHand(newUsedItem);
        }
    }
}
