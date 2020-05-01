package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.vanilla.entity.FallingBlockEntity;

public class GravityBlock extends VanillaBlock {
    public GravityBlock(Block baseBlock) {
        super(baseBlock);
    }

    @Override
    public void onPlace(Instance instance, BlockPosition blockPosition, Data data) {
        instance.scheduleUpdate(2, TimeUnit.TICK, blockPosition);
    }

    @Override
    public void update(Instance instance, BlockPosition blockPosition, Data data) {
        Block below = Block.fromId(instance.getBlockId(blockPosition.getX(), blockPosition.getY()-1, blockPosition.getZ()));
        if(below.isAir()) {
            instance.scheduleUpdate(2, TimeUnit.TICK, blockPosition);
        }
    }

    @Override
    public void scheduledUpdate(Instance instance, BlockPosition blockPosition, Data data) {
        Block below = Block.fromId(instance.getBlockId(blockPosition.getX(), blockPosition.getY()-1, blockPosition.getZ()));
        if(below.isAir()) {
            instance.setBlock(blockPosition, Block.AIR);

            FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(getBaseBlock(), this);
            fallingBlockEntity.getPosition().setX(blockPosition.getX() + 0.5f);
            fallingBlockEntity.getPosition().setY(Math.round(blockPosition.getY()));
            fallingBlockEntity.getPosition().setZ(blockPosition.getZ() + 0.5f);

            fallingBlockEntity.setInstance(instance);
        }
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList();
    }
}
