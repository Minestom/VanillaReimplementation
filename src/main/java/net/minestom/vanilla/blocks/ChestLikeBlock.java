package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.minestom.vanilla.system.EnderChestSystem;

/**
 * Base class for Ender Chest, Chest and Trapped Chest
 */
public abstract class ChestLikeBlock extends VanillaBlock {

    public ChestLikeBlock(Block baseBlock) {
        super(baseBlock);
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList().directionProperty("facing").booleanProperty("waterlogged");
    }

    @Override
    public short getVisualBlockForPlacement(Player player, Player.Hand hand, BlockPosition position) {
        // TODO: handle double chests
        boolean waterlogged = Block.fromId(player.getInstance().getBlockId(position.getX(), position.getY(), position.getZ())) == Block.WATER;
        float yaw = player.getPosition().getYaw();
        Direction direction = MathUtils.getHorizontalDirection(yaw).opposite();
        return getBaseBlockState().with("facing", direction.name().toLowerCase()).with("waterlogged", String.valueOf(waterlogged)).getBlockId();
    }

    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        // TODO: Handle crouching players
        Block above = Block.fromId(player.getInstance().getBlockId(blockPosition.getX(), blockPosition.getY()+1, blockPosition.getZ()));
        if(above.isSolid()) {
            return false;
        }
        player.openInventory(getInventory(player, blockPosition, data));
        return true;
    }

    /**
     * Return the inventory to open when opening this chest
     */
    protected abstract Inventory getInventory(Player player, BlockPosition blockPosition, Data data);

}
