package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.UpdateOption;
import net.minestom.vanilla.system.EnderChestSystem;

public class EnderChestBlock extends CustomBlock {
    public EnderChestBlock() {
        super(Block.ENDER_CHEST, "vanilla_ender_chest");
    }

    @Override
    public void onPlace(Instance instance, BlockPosition blockPosition, Data data) {

    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {

    }

    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        // TODO: Handle crouching players
        Block above = Block.fromId(player.getInstance().getBlockId(blockPosition.getX(), blockPosition.getY()+1, blockPosition.getZ()));
        if(above.isSolid()) {
            return false;
        }
        player.openInventory(EnderChestSystem.getInstance().get(player));
        return true;
    }

    @Override
    public UpdateOption getUpdateOption() {
        return null;
    }

    @Override
    public short getCustomBlockId() {
        return Block.ENDER_CHEST.getBlockId();
    }

    @Override
    public int getBreakDelay(Player player) {
        return -1;
    }
}
