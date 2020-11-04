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

public class ChestBlock extends ChestLikeBlock {
    public ChestBlock() {
        super(Block.CHEST);
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return super.createPropertyValues().property("type", "single", "left", "right");
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
        BlockState state = getBlockStates().fromStateID(player.getInstance().getBlockStateId(blockPosition));
        String type = state.get("type");

        Inventory selfInventory = ((ChestBlockEntity) data).getInventory();

        BlockPosition positionOfOtherChest = blockPosition.copy();
        Direction facing = Direction.valueOf(state.get("facing").toUpperCase());
        switch (type) {
            case "single":
                return selfInventory;

            case "left":
                positionOfOtherChest.add(-facing.normalZ(), 0, facing.normalX());
                break;

            case "right":
                positionOfOtherChest.add(facing.normalZ(), 0, facing.normalX()*-1);
                break;

            default:
                throw new IllegalArgumentException("Invalid chest type: "+type);
        }

        Data otherData = player.getInstance().getBlockData(positionOfOtherChest);
        if(otherData instanceof ChestBlockEntity) {
            Inventory otherInventory = ((ChestBlockEntity) otherData).getInventory();
            switch (type) {
                case "left":
                    return new DoubleChestInventory(otherInventory, selfInventory, "Chest"); // TODO: custom name

                case "right":
                    return new DoubleChestInventory(selfInventory, otherInventory, "Chest"); // TODO: custom name
            }
        }

        return selfInventory;
    }
}
