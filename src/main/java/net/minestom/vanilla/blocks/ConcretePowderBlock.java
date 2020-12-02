package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;

public class ConcretePowderBlock extends GravityBlock {
    private final Block solidifiedBlock;

    public ConcretePowderBlock(Block powderBlock, Block solidifiedBlock) {
        super(powderBlock);
        this.solidifiedBlock = solidifiedBlock;
    }

    @Override
    public void onPlace(Instance instance, BlockPosition blockPosition, Data data) {
        super.onPlace(instance, blockPosition, data);
        tryConvert(instance, blockPosition);
    }

    @Override
    public void update(Instance instance, BlockPosition blockPosition, Data data) {
        super.update(instance, blockPosition, data);
        tryConvert(instance, blockPosition);
    }

    private void tryConvert(Instance instance, BlockPosition blockPosition) {
        Block above = Block.fromStateId(instance.getBlockStateId(blockPosition.getX(), blockPosition.getY()+1, blockPosition.getZ()));
        Block west = Block.fromStateId(instance.getBlockStateId(blockPosition.getX()-1, blockPosition.getY(), blockPosition.getZ()));
        Block east = Block.fromStateId(instance.getBlockStateId(blockPosition.getX()+1, blockPosition.getY(), blockPosition.getZ()));
        Block north = Block.fromStateId(instance.getBlockStateId(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ()-1));
        Block south = Block.fromStateId(instance.getBlockStateId(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ()+1));

        // TODO: support block tags
        if(above == Block.WATER
                || west == Block.WATER
                || east == Block.WATER
                || north == Block.WATER
                || south == Block.WATER
        ) {
            instance.setBlock(blockPosition, solidifiedBlock);
        }
    }
}
