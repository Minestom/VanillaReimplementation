package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.UpdateOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Represents a vanilla block implementation.
 * Will handle creation of all VanillaCustomBlock necessary to represent this block
 */
public abstract class VanillaBlock {

    private final Block baseBlock;
    private final List<VanillaCustomBlock> variants;

    public VanillaBlock(Block baseBlock) {
        this.baseBlock = baseBlock;
        this.variants = new ArrayList<>();
        BlockPropertyList properties = createPropertyValues();
        if(properties.isEmpty()) {
            variants.add(new VanillaCustomBlock(this, baseBlock.getBlockId(), new String[0]));
        } else {
            for(String[] propertyValues : properties.getCartesianProduct()) {
                System.out.println(">> "+baseBlock.name()+" => "+ Arrays.toString(propertyValues));
                variants.add(new VanillaCustomBlock(this, baseBlock.withProperties(propertyValues), propertyValues));
            }
        }
    }

    protected abstract BlockPropertyList createPropertyValues();

    public Block getBaseBlock() {
        return baseBlock;
    }

    public List<VanillaCustomBlock> getAllVariants() {
        return variants;
    }

    /**
     * Create data for this block
     * @param blockPosition
     * @param data
     * @param properties allow to change depending on the block state
     * @return
     */
    public Data createData(BlockPosition blockPosition, Data data, String[] properties) {
        return data;
    }

    /**
     * Place this block with given properties
     * @param instance
     * @param blockPosition
     * @param data
     * @param properties
     */
    public void onPlace(Instance instance, BlockPosition blockPosition, Data data, String[] properties) {

    }

    /**
     * Destroy this block with given properties
     * @param instance
     * @param blockPosition
     * @param data
     * @param properties
     */
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data, String[] properties) {

    }

    /**
     * Interact with this block, depending on properties
     */
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data, String[] properties) {
        return false;
    }

    public UpdateOption getUpdateOption(String[] properties) {
        return null;
    }

    public short getBaseBlockId() {
        return baseBlock.getBlockId();
    }

    public int getBreakDelay(Player player, String[] properties) {
        return -1;
    }

    public short getStateForPlacement(Player player, Player.Hand hand, BlockPosition blockPosition) {
        return getBaseBlockId();
    }
}
