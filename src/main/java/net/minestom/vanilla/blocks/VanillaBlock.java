package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.UpdateOption;
import net.querz.nbt.tag.CompoundTag;

import java.util.List;

/**
 * Represents a vanilla block implementation.
 * Will handle creation of all VanillaCustomBlock necessary to represent this block
 */
public abstract class VanillaBlock extends CustomBlock {

    private final Block baseBlock;
    private final BlockPropertyList properties;
    protected final BlockStates blockStates;
    private final BlockState baseBlockState;

    public VanillaBlock(Block baseBlock) {
        super(baseBlock, "vanilla_"+baseBlock.name().toLowerCase());
        this.baseBlock = baseBlock;
        this.properties = createPropertyValues();

        // create block states
        this.blockStates = new BlockStates(properties);
        List<String[]> allVariants = properties.getCartesianProduct();
        if(allVariants.isEmpty()) {
            short id = baseBlock.getBlockId();
            BlockState state = new BlockState(id, blockStates);
            blockStates.add(state);
        } else {
            for(String[] variant : allVariants) {
                short id = baseBlock.withProperties(variant);
                BlockState blockState = new BlockState(id, blockStates, variant);
                blockStates.add(blockState);
            }
        }
        baseBlockState = blockStates.getDefault();
    }

    protected abstract BlockPropertyList createPropertyValues();

    public BlockState getBaseBlockState() {
        return baseBlockState;
    }

    public Block getBaseBlock() {
        return baseBlock;
    }

    /**
     * Create data for this block
     * @param blockPosition
     * @param data
     * @return
     */
    @Override
    public Data createData(Instance instance, BlockPosition blockPosition, Data data) {
        return data;
    }

    @Override
    public void onPlace(Instance instance, BlockPosition blockPosition, Data data) {

    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {

    }

    @Override
    public void update(Instance instance, BlockPosition blockPosition, Data data) {

    }

    /**
     * Interact with this block, depending on properties
     */
    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        return false;
    }

    @Override
    public UpdateOption getUpdateOption() {
        return null;
    }

    public short getBaseBlockId() {
        return baseBlock.getBlockId();
    }

    @Override
    public short getCustomBlockId() {
        return baseBlock.getBlockId();
    }

    @Override
    public int getBreakDelay(Player player, BlockPosition position) {
        return -1;
    }

    public short getVisualBlockForPlacement(Player player, Player.Hand hand, BlockPosition blockPosition) {
        return getBaseBlockId();
    }

    /**
     * Loads the TileEntity information from the given NBT, during world loading from the Anvil format.
     * Should be stored in the Data object returned by this function.
     * It is allowed (and encouraged) to modify 'originalData' and returning it.
     *
     * Your method {@link #createData(Instance, BlockPosition, Data)} should return a non-null data object if you want to use this method easily
     * @param nbt
     * @param instance instance in which the tile entity is being loaded
     * @param position position at which this block is. DON'T CACHE IT
     * @param originalData data present at the current position
     * @return a Data object with the loaded information. Can be originalData, a new object, or even null if you don't use the TE info
     */
    public Data readTileEntity(CompoundTag nbt, Instance instance, BlockPosition position, Data originalData) {
        return originalData;
    }
}
