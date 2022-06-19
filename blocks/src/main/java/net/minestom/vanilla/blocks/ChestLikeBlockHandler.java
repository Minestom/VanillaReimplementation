package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.blocks.chestlike.ChestInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Base class for Ender Chest, Chest and Trapped Chest
 * <p>
 * This class needs onPlace to be able to change the block being placed
 */
public abstract class ChestLikeBlockHandler extends VanillaBlockHandler {

    public static final Tag<List<ItemStack>> TAG_ITEMS = Tag.ItemStack("vri:chest_items").list();
    protected static final Random rng = new Random();
    protected final int size;

    public ChestLikeBlockHandler(@NotNull VanillaBlocks.BlockContext context, int size) {
        super(context);
        this.size = size;
    }

    @Override
    public void onPlace(@NotNull VanillaPlacement placement) {
        Block block = placement.blockToPlace();
        Instance instance = placement.instance();
        Point pos = placement.position();

        @UnknownNullability List<ItemStack> items = block.getTag(TAG_ITEMS);

        if (items != null) {
            return;
        }


        ItemStack[] itemsArray = new ItemStack[size];
        Arrays.fill(itemsArray, ItemStack.AIR);

        // Override the block to set
        Block blockToSet = block.withTag(TAG_ITEMS, List.of(itemsArray));
        placement.blockToPlace(blockToSet);
    }

    @Override
    public void onDestroy(@NotNull BlockHandler.Destroy destroy) {
        Instance instance = destroy.getInstance();
        Point pos = destroy.getBlockPosition();
        Block block = destroy.getBlock();

        // TODO: Introduce a way to get the block this is getting replaced with, enabling us to remove the tick delay.
        destroy.getInstance().scheduleNextTick(ignored -> {
            if (instance.getBlock(pos).compare(block)) {
                // Same block, don't remove chest inventory
                return;
            }

            // Different block, remove chest inventory
            ItemStack[] items = ChestInventory.remove(instance, pos);

            if (!dropContentsOnDestroy()) {
                return;
            }

            for (ItemStack item : items) {

                if (item == null) {
                    continue;
                }

                ItemEntity entity = new ItemEntity(item);

                entity.setInstance(destroy.getInstance());
                entity.teleport(new Pos(pos.x() + rng.nextDouble(), pos.y() + .5f, pos.z() + rng.nextDouble()));
            }
        });
    }

//    @Override
//    public short getVisualBlockForPlacement(Player player, Player.Hand hand, BlockPosition position) {
//        // TODO: handle double chests
//        boolean waterlogged = Block.fromStateId(player.getInstance().getBlockStateId(position.getX(), position.getY(), position.getZ())) == Block.WATER;
//        float yaw = player.getPosition().getYaw();
//        Direction direction = MathUtils.getHorizontalDirection(yaw).opposite();
//        return getBaseBlockState().with("facing", direction.name().toLowerCase()).with("waterlogged", String.valueOf(waterlogged)).getBlockId();
//    }

    @Override
    public boolean onInteract(@NotNull BlockHandler.Interaction interaction) {
        // TODO: handle double chests
        // TODO: Handle crouching players

        Block block = interaction.getBlock();
        Instance instance = interaction.getInstance();
        Point pos = interaction.getBlockPosition();
        Player player = interaction.getPlayer();

        Block above = instance.getBlock(pos.blockX(), pos.blockY() + 1, pos.blockZ());

        if (above.isSolid()) { // FIXME: chests below transparent blocks cannot be opened
            return false;
        }

        Inventory chestInventory = ChestInventory.from(instance, pos);
        player.openInventory(chestInventory);
        return true;
    }

    public abstract boolean dropContentsOnDestroy();

    /**
     * Gets the items in this block only
     *
     * @param block the block
     * @return the items
     */
    protected @NotNull List<ItemStack> getItems(Block block) {
        List<ItemStack> items = block.getTag(TAG_ITEMS);
        if (items == null) {
            throw new IllegalStateException("Chest block has no items");
        }
        if (items.size() != this.size) {
            throw new IllegalStateException("Invalid items size");
        }
        return items;
    }

    /**
     * Gets all items represented by this position in this instance
     *
     * @param instance the instance
     * @param pos      the position
     * @return all items in the position in the instance
     */
    protected List<ItemStack> getAllItems(Instance instance, Point pos, Player player) {
        Block block = instance.getBlock(pos);
        List<ItemStack> items = new ArrayList<>(getItems(block));

        Point positionOfOtherChest = pos;
        Direction facing = Direction.valueOf(block.getProperty("facing").toUpperCase());
        String type = block.getProperty("type");

        switch (type) {
            case "single" -> {
                return List.copyOf(items);
            }
            case "left" -> positionOfOtherChest = positionOfOtherChest.add(-facing.normalZ(), 0, facing.normalX());
            case "right" -> positionOfOtherChest = positionOfOtherChest.add(facing.normalZ(), 0, -facing.normalX());
            default -> throw new IllegalArgumentException("Invalid chest type: " + type);
        }

        Block otherBlock = instance.getBlock(positionOfOtherChest);
        BlockHandler handler = otherBlock.handler();

        if (handler instanceof ChestLikeBlockHandler chestLike) {
            items.addAll(chestLike.getItems(otherBlock));
        }

        return List.copyOf(items);
    }

//    @Override
//    public Data readBlockEntity(NBTCompound nbt, Instance instance, BlockPosition position, Data originalData) {
//        ChestBlockEntity data;
//        if (originalData instanceof ChestBlockEntity) {
//            data = (ChestBlockEntity) originalData;
//        } else {
//            data = new ChestBlockEntity(position.copy());
//        }
//
//        // TODO: CustomName
//        // TODO: Lock
//        // TODO: LootTable
//        // TODO: LootTableSeed
//
//        if (nbt.containsKey("Items")) {
//            NBTUtils.loadAllItems(nbt.getList("Items"), data.getInventory());
//        }
//
//        return data;
//    }

//    @Override
//    public void writeBlockEntity(BlockPosition position, Data blockData, NBTCompound nbt) {
//        // TODO: CustomName
//        // TODO: Lock
//        // TODO: LootTable
//        // TODO: LootTableSeed
//        if (blockData instanceof ChestBlockEntity) {
//            ChestBlockEntity data = (ChestBlockEntity) blockData;
//            NBTList<NBTCompound> list = new NBTList<>(NBTTypes.TAG_Compound);
//            NBTUtils.saveAllItems(list, data.getInventory());
//            nbt.set("Items", list);
//        }
//    }
}
