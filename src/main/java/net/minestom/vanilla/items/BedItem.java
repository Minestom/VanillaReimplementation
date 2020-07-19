package net.minestom.vanilla.items;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.minestom.vanilla.blocks.VanillaBlock;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.inventory.InventoryManipulation;

public class BedItem extends VanillaItem {
    private final VanillaBlocks correspondingBlock;

    public BedItem(Material baseItem, VanillaBlocks correspondingBlock) {
        super(baseItem);
        this.correspondingBlock = correspondingBlock;
    }

    @Override
    public boolean onUseOnBlock(Player player, ItemStack itemStack, Player.Hand hand, BlockPosition position, Direction blockFace) {
        BlockPosition abovePos = new BlockPosition(position.getX(), position.getY(), position.getZ()).add(0, 1, 0);
        Instance instance = player.getInstance();
        Block above = Block.fromId(instance.getBlockId(abovePos));
        if(isReplaceable(above)) {
            Direction playerDirection = MathUtils.getHorizontalDirection(player.getPosition().getYaw());
            BlockPosition bedHeadPosition = new BlockPosition(abovePos.getX(), abovePos.getY(), abovePos.getZ());
            bedHeadPosition.add(playerDirection.normalX(), playerDirection.normalY(), playerDirection.normalZ());
            Block blockAtPotentialBedHead = Block.fromId(instance.getBlockId(bedHeadPosition));
            if(isReplaceable(blockAtPotentialBedHead)) {
                placeBed(instance, abovePos, bedHeadPosition, playerDirection);

                InventoryManipulation.consumeItemIfNotCreative(player, itemStack, hand);
            }
        }
        return true;
    }

    private void placeBed(Instance instance, BlockPosition footPosition, BlockPosition headPosition, Direction facing) {
        VanillaBlock bedBlock = correspondingBlock.getInstance();
        short footId = bedBlock.getBaseBlockState().with("facing", facing.name().toLowerCase()).with("part", "foot").getBlockId();
        short headId = bedBlock.getBaseBlockState().with("facing", facing.name().toLowerCase()).with("part", "head").getBlockId();
        instance.setSeparateBlocks(footPosition.getX(), footPosition.getY(), footPosition.getZ(), footId, bedBlock.getCustomBlockId(), null);
        instance.setSeparateBlocks(headPosition.getX(), headPosition.getY(), headPosition.getZ(), headId, bedBlock.getCustomBlockId(), null);
    }

    private boolean isReplaceable(Block blockAtPosition) {
        return blockAtPosition.isAir() || blockAtPosition.isLiquid();
    }

    @Override
    public void onUseInAir(Player player, ItemStack itemStack, Player.Hand hand) {}
}
