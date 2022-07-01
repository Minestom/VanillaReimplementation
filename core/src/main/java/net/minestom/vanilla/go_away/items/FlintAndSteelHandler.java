package net.minestom.vanilla.go_away.items;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.go_away.inventory.InventoryManipulation;

public class FlintAndSteelHandler implements VanillaItemHandler {
    public FlintAndSteelHandler() {
    }

    @Override
    public boolean onUseOnBlock(PlayerUseItemOnBlockEvent event) {
        // TODO: check if flammable
        Point pos = event.getPosition();
        Player player = event.getPlayer();
        Instance instance = player.getInstance();
        ItemStack itemStack = event.getItemStack();
        Player.Hand hand = event.getHand();
        Direction blockDir = event.getBlockFace().toDirection();

        // Find block in direction
        Point firePosition = pos.add(
                blockDir.normalX(),
                blockDir.normalY(),
                blockDir.normalZ()
        );

        Block atFirePosition = instance.getBlock(firePosition);

        if (atFirePosition.isAir()) {
            InventoryManipulation.damageItemIfNotCreative(player, itemStack, hand);
            instance.setBlock(firePosition, Block.FIRE);
            return true;
        }

        return false;
    }
}
