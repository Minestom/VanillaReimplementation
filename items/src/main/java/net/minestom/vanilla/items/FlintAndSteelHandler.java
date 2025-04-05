package net.minestom.vanilla.items;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.inventory.InventoryManipulation;

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
        PlayerHand hand = event.getHand();
        Direction blockDir = event.getBlockFace().toDirection();

        // Find block in direction
        Point firePosition = pos.add(
                blockDir.normalX(),
                blockDir.normalY(),
                blockDir.normalZ()
        );

        Block atFirePosition = instance.getBlock(firePosition);

        if (atFirePosition.isAir()) {
            InventoryManipulation.damageItemIfNotCreative(player, hand, 1);
            // Block block, Instance instance, Point blockPosition, Player player, Player.Hand hand,
            // BlockFace blockFace, float cursorX, float cursorY, float cursorZ
            instance.placeBlock(new BlockHandler.PlayerPlacement(
                    Block.FIRE,
                    instance,
                    firePosition,
                    player,
                    hand,
                    event.getBlockFace(),
                    0, 0, 0 // TODO: cursor position via raycast
            ));
            return true;
        }

        return false;
    }
}
