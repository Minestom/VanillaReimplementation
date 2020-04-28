package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.UpdateOption;

public final class VanillaCustomBlock extends CustomBlock {

    private final VanillaBlock vanillaBlock;
    private final String[] properties;

    public VanillaCustomBlock(VanillaBlock vanillaBlock, short blockId, String[] properties) {
        super(blockId, "vanilla_"+buildIdentifier(vanillaBlock, properties));
        this.vanillaBlock = vanillaBlock;
        this.properties = properties;
    }

    private static String buildIdentifier(VanillaBlock vanillaBlock, String[] properties) {
        StringBuilder nameBuilder = new StringBuilder(vanillaBlock.getBaseBlock().name().toLowerCase());
        for(String property : properties) {
            nameBuilder.append("_").append(property);
        }
        return nameBuilder.toString();
    }

    @Override
    public Data createData(BlockPosition blockPosition, Data data) {
        return vanillaBlock.createData(blockPosition, data, properties);
    }

    @Override
    public void onPlace(Instance instance, BlockPosition blockPosition, Data data) {
        vanillaBlock.onPlace(instance, blockPosition, data, properties);
    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {
        vanillaBlock.onDestroy(instance, blockPosition, data, properties);
    }

    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        return vanillaBlock.onInteract(player, hand, blockPosition, data, properties);
    }

    @Override
    public UpdateOption getUpdateOption() {
        return vanillaBlock.getUpdateOption(properties);
    }

    @Override
    public short getCustomBlockId() {
        return getBlockId();
    }

    @Override
    public int getBreakDelay(Player player) {
        return vanillaBlock.getBreakDelay(player, properties);
    }
}
