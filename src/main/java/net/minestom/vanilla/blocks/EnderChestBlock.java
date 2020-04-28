package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.minestom.vanilla.system.EnderChestSystem;

public class EnderChestBlock extends VanillaBlock {
    public EnderChestBlock() {
        super(Block.ENDER_CHEST);
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList().directionProperty("facing").booleanProperty("waterlogged");
    }

    @Override
    public short getStateForPlacement(Player player, Player.Hand hand, BlockPosition position) {
        boolean waterlogged = Block.fromId(player.getInstance().getBlockId(position.getX(), position.getY(), position.getZ())) == Block.WATER;
        float yaw = player.getPosition().getYaw();
        Direction direction = MathUtils.getHorizontalDirection(yaw).opposite();
        return Block.ENDER_CHEST.withProperties("facing="+direction.name().toLowerCase(), "waterlogged="+waterlogged);
    }

    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data, String[] properties) {
        // TODO: Handle crouching players
        Block above = Block.fromId(player.getInstance().getBlockId(blockPosition.getX(), blockPosition.getY()+1, blockPosition.getZ()));
        if(above.isSolid()) {
            return false;
        }
        player.openInventory(EnderChestSystem.getInstance().get(player));
        return true;
    }

}
