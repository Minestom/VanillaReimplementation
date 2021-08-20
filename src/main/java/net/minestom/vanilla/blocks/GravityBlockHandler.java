package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.data.Data;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.vanilla.entity.FallingBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class GravityBlockHandler extends VanillaBlockHandler {
    public GravityBlockHandler(Block baseBlock) {
        super(baseBlock);
    }

    @Override
    public void onPlace(@NotNull Placement placement) {
        // instance.scheduleUpdate(2, TimeUnit.TICK, blockPosition);
    }

    @Override
    public void tick(@NotNull Tick tick) {
        Instance instance = tick.getInstance();
        Point position = tick.getBlockPosition();

        Block below = instance.getBlock(position.blockX(), position.blockY() - 1, position.blockZ());

        if (below.isAir()) {
            // instance.scheduleUpdate(2, TimeUnit.TICK, blockPosition);
        }
    }

    /**
     * Checks if a block should fall
     * @param instance the instance the block is in
     * @param position the position of the block
     * @param block the block that may fall
     */
    public void checkFall(Instance instance, Point position, Block block) {
        Block below = instance.getBlock(position.blockX(), position.blockY() - 1, position.blockZ());

        // Exit out now if block below is not air
        if(!below.isAir()) {
            return;
        }

        instance.setBlock(position, Block.AIR);

        Pos initialPosition = new Pos(position.x() + 0.5f, Math.round(position.y()), position.z() + 0.5f);
        FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(block, this, initialPosition);
        fallingBlockEntity.setInstance(instance);
    }
}
