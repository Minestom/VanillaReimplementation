package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.utils.BlockPosition;

public class TNTBlock extends VanillaBlock {
    public TNTBlock() {
        super(Block.TNT);
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList().booleanProperty("unstable");
    }

    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        if(player.getInventory().getItemInHand(hand).getMaterial() == Material.FLINT_AND_STEEL) {
            // TODO
            Data explosionData = new Data();
            explosionData.set("minestom:from_tnt", true, Boolean.class);
            player.getInstance().explode(blockPosition.getX()+0.5f, blockPosition.getY()+0.5f, blockPosition.getZ()+0.5f, 4f, explosionData);
            return true;
        }
        return super.onInteract(player, hand, blockPosition, data);
    }
}
