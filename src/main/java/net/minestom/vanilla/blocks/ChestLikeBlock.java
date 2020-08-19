package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.*;
import net.minestom.vanilla.blockentity.ChestBlockEntity;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.Random;

/**
 * Base class for Ender Chest, Chest and Trapped Chest
 */
public abstract class ChestLikeBlock extends VanillaBlock {

    public ChestLikeBlock(Block baseBlock) {
        super(baseBlock);
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList().facingProperty("facing").booleanProperty("waterlogged");
    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {
        super.onDestroy(instance, blockPosition, data);
        if(dropContentsOnDestroy()) {
            ChestBlockEntity blockEntity = (ChestBlockEntity)data;
            for(ItemStack stack : blockEntity.getInventory().getItemStacks()) {
                Random rng = new Random();
                ItemEntity entity = new ItemEntity(stack, new Position((float) (blockPosition.getX()+rng.nextDouble()), blockPosition.getY()+.5f, (float) (blockPosition.getZ()+rng.nextDouble())));
                instance.addEntity(entity);
            }
        }
    }

    @Override
    public short getVisualBlockForPlacement(Player player, Player.Hand hand, BlockPosition position) {
        // TODO: handle double chests
        boolean waterlogged = Block.fromStateId(player.getInstance().getBlockStateId(position.getX(), position.getY(), position.getZ())) == Block.WATER;
        float yaw = player.getPosition().getYaw();
        Direction direction = MathUtils.getHorizontalDirection(yaw).opposite();
        return getBaseBlockState().with("facing", direction.name().toLowerCase()).with("waterlogged", String.valueOf(waterlogged)).getBlockId();
    }

    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        // TODO: handle double chests
        // TODO: Handle crouching players
        Block above = Block.fromStateId(player.getInstance().getBlockStateId(blockPosition.getX(), blockPosition.getY()+1, blockPosition.getZ()));
        if(above.isSolid()) { // FIXME: chests below transparent blocks cannot be opened
            return false;
        }
        player.openInventory(getInventory(player, blockPosition, data));
        return true;
    }

    public abstract boolean dropContentsOnDestroy();

    /**
     * Return the inventory to open when opening this chest
     */
    protected abstract Inventory getInventory(Player player, BlockPosition blockPosition, Data data);

    @Override
    public Data readBlockEntity(NBTCompound nbt, Instance instance, BlockPosition position, Data originalData) {
        ChestBlockEntity data;
        if(originalData instanceof ChestBlockEntity) {
            data = (ChestBlockEntity) originalData;
        } else {
            data = new ChestBlockEntity(position.clone());
        }

        // TODO: CustomName
        // TODO: Lock
        // TODO: LootTable
        // TODO: LootTableSeed

        if(nbt.containsKey("Items")) {
            NBTUtils.loadAllItems(nbt.getList("Items"), data.getInventory());
        }
        return data;
    }

    @Override
    public void writeBlockEntity(BlockPosition position, Data blockData, NBTCompound nbt) {
        // TODO: CustomName
        // TODO: Lock
        // TODO: LootTable
        // TODO: LootTableSeed
        if(blockData instanceof ChestBlockEntity) {
            ChestBlockEntity data = (ChestBlockEntity) blockData;
            NBTList<NBTCompound> list = new NBTList<>(NBTTypes.TAG_Compound);
            NBTUtils.saveAllItems(list, data.getInventory());
            nbt.set("Items", list);
        }
    }
}
