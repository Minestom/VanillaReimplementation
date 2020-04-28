package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.UpdateOption;
import net.minestom.vanilla.blockentity.JukeboxBlockEntity;

/**
 * Reimplementation of the jukebox block
 */
public class JukeboxBlock extends CustomBlock {
    public JukeboxBlock() {
        super(Block.JUKEBOX, "vanilla_jukebox");
    }

    @Override
    public void onPlace(Instance instance, BlockPosition blockPosition, Data data) {

    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {
        JukeboxBlockEntity entity = (JukeboxBlockEntity)data;
        if(entity != null) {
            entity.onDestroyed(instance);
        }
    }

    @Override
    public void onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        JukeboxBlockEntity entity = (JukeboxBlockEntity)data;
        if(entity != null) {
            entity.onInteract(player, hand, heldItem);
        }
    }

    @Override
    public Data createData(BlockPosition position, Data data) {
        return new JukeboxBlockEntity(position);
    }

    @Override
    public UpdateOption getUpdateOption() {
        return null;
    }

    @Override
    public short getCustomBlockId() {
        return Block.JUKEBOX.getBlockId();
    }

    @Override
    public int getBreakDelay(Player player) {
        return 750; // TODO
    }
}
