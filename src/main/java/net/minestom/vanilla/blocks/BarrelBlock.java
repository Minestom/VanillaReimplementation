package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.blockentity.ChestBlockEntity;
import net.minestom.vanilla.inventory.DoubleChestInventory;
import net.minestom.vanilla.system.EnderChestSystem;

public class BarrelBlock extends ChestLikeBlock {
    public BarrelBlock() {
        super(Block.BARREL);
    }

    @Override
    public boolean dropContentsOnDestroy() {
        return true;
    }

    @Override
    public Data createData(Instance instance, BlockPosition blockPosition, Data data) {
        return new ChestBlockEntity(blockPosition);
    }

    @Override
    protected Inventory getInventory(Player player, BlockPosition blockPosition, Data data) {
        return ((ChestBlockEntity) data).getInventory();
    }
}
