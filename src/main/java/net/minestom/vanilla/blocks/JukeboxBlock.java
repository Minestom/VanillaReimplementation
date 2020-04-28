package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.BlockPosition;
import net.minestom.vanilla.blockentity.JukeboxBlockEntity;

/**
 * Reimplementation of the jukebox block
 */
public class JukeboxBlock extends VanillaBlock {
    public JukeboxBlock() {
        super(Block.JUKEBOX);
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList()
                .booleanProperty("has_record");
    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data, String[] properties) {
        JukeboxBlockEntity entity = (JukeboxBlockEntity)data;
        if(entity != null) {
            entity.onDestroyed(instance);
        }
    }

    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data, String[] properties) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        JukeboxBlockEntity entity = (JukeboxBlockEntity)data;
        if(entity != null) {
            return entity.onInteract(player, hand, heldItem);
        }
        return false;
    }

    @Override
    public Data createData(BlockPosition position, Data data, String[] properties) {
        return new JukeboxBlockEntity(position);
    }
}
