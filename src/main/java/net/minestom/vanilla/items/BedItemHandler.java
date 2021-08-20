package net.minestom.vanilla.items;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.minestom.vanilla.blocks.VanillaBlockHandler;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.inventory.InventoryManipulation;

public class BedItemHandler implements VanillaItemHandler {
    public BedItemHandler() {}

    private void placeBed(Instance instance, Block bedBlock, Point footPosition, Point headPosition, Direction facing) {
        Block correctFacing = bedBlock.withProperty("facing", facing.name().toLowerCase());

        Block footBlock = correctFacing.withProperty("part", "foot");
        Block headBlock = correctFacing.withProperty("part", "head");
        instance.setBlock(footPosition, footBlock);
        instance.setBlock(headPosition, headBlock);
    }

    private boolean isReplaceable(Block blockAtPosition) {
        return blockAtPosition.isAir() || blockAtPosition.isLiquid();
    }

    @Override
    public void onUseInAir(PlayerUseItemEvent event) {}

    @Override
    public boolean onUseOnBlock(PlayerUseItemOnBlockEvent event) {
        Point pos = event.getPosition();
        Player player = event.getPlayer();
        Instance instance = player.getInstance();
        ItemStack itemStack = event.getItemStack();
        Player.Hand hand = event.getHand();


        Point abovePos = pos.add(0, 1, 0);
        Block above = instance.getBlock(abovePos);

        Block bedBlock = itemStack.getMaterial().block();


        if (isReplaceable(above)) {
            Direction playerDirection = MathUtils.getHorizontalDirection(player.getPosition().yaw());

            Point bedHeadPosition = abovePos.add(playerDirection.normalX(), playerDirection.normalY(), playerDirection.normalZ());
            Block blockAtPotentialBedHead = instance.getBlock(bedHeadPosition);

            if (isReplaceable(blockAtPotentialBedHead)) {
                placeBed(instance, bedBlock, abovePos, bedHeadPosition, playerDirection);

                InventoryManipulation.consumeItemIfNotCreative(player, itemStack, hand);
            }
        }
        return true;
    }
}
