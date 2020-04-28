package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.UpdateOption;

/**
 * Represents a vanilla block implementation.
 * Will handle creation of all VanillaCustomBlock necessary to represent this block
 */
public abstract class VanillaBlock extends CustomBlock {

    private final Block baseBlock;

    public VanillaBlock(Block baseBlock) {
        super(baseBlock, "vanilla_"+baseBlock.name().toLowerCase());
        this.baseBlock = baseBlock;
    }

    protected abstract BlockPropertyList createPropertyValues();

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
}
