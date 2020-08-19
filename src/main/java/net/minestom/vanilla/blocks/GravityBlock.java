package net.minestom.vanilla.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
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
        Block below = Block.fromStateId(instance.getBlockStateId(blockPosition.getX(), blockPosition.getY()-1, blockPosition.getZ()));
        if(below.isAir()) {
            instance.scheduleUpdate(2, TimeUnit.TICK, blockPosition);
        }
    }

    @Override
    public void scheduledUpdate(Instance instance, BlockPosition blockPosition, Data data) {
        Block below = Block.fromStateId(instance.getBlockStateId(blockPosition.getX(), blockPosition.getY()-1, blockPosition.getZ()));
        if(below.isAir()) {
            instance.setBlock(blockPosition, Block.AIR);

            Position initialPosition = new Position(blockPosition.getX() + 0.5f, Math.round(blockPosition.getY()), blockPosition.getZ() + 0.5f);
            FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(getBaseBlock(), this, initialPosition);

            fallingBlockEntity.setInstance(instance);
        }
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList();
    }
}
