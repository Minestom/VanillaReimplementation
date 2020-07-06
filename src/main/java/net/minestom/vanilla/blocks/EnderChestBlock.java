package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.utils.BlockPosition;
import net.minestom.vanilla.system.EnderChestSystem;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class EnderChestBlock extends ChestLikeBlock {
    public EnderChestBlock() {
        super(Block.ENDER_CHEST);
    }

    @Override
    public boolean dropContentsOnDestroy() {
        return false;
    }

    @Override
    protected Inventory getInventory(Player player, BlockPosition blockPosition, Data data) {
        return EnderChestSystem.getInstance().get(player);
    }

    @Override
    public Data readTileEntity(NBTCompound nbt, Instance instance, BlockPosition position, Data originalData) {
        return null;
    }
}
