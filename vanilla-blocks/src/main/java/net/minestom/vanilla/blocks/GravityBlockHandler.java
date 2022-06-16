package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.blockupdatesystem.BlockUpdatable;
import net.minestom.vanilla.blockupdatesystem.BlockUpdateInfo;
import net.minestom.vanilla.blockupdatesystem.BlockUpdateManager;
import org.jetbrains.annotations.NotNull;

public class GravityBlockHandler extends VanillaBlockHandler implements BlockUpdatable {
    public GravityBlockHandler(@NotNull VanillaBlocks.BlockContext context) {
        super(context);
    }

    @Override
    public void onPlace(@NotNull BlockHandler.Placement placement) {
        Instance instance = placement.getInstance();
        Point position = placement.getBlockPosition();

        instance.scheduleNextTick((instance1 -> checkFall(instance1, position)));
    }

    @Override
    public void tick(@NotNull BlockHandler.Tick tick) {
        Instance instance = tick.getInstance();
        Point position = tick.getBlockPosition();
        instance.scheduleNextTick((instance1 -> checkFall(instance1, position)));
    }

    /**
     * Checks if a block should fall
     *
     * @param instance the instance the block is in
     * @param position the position of the block
     */
    public void checkFall(Instance instance, Point position) {
        Block block = instance.getBlock(position);
        Block below = instance.getBlock(position.blockX(), position.blockY() - 1, position.blockZ());

        // Exit out now if block below is solid
        if (below.isSolid()) {
            return;
        }

        // Schedule block update
        BlockUpdateManager.from(instance).scheduleNeighborsUpdate(position, BlockUpdateInfo.MOVE_BLOCK(position));

        instance.setBlock(position, Block.AIR);

        // Create the context
        Pos initialPosition = new Pos(position.x() + 0.5f, Math.round(position.y()), position.z() + 0.5f);
        VanillaRegistry.EntityContext entityContext = context.vri()
                .entityContext(EntityType.FALLING_BLOCK, initialPosition);
        Entity entity = context.vri().createEntityOrDummy(entityContext);

        // Spawn the entity
        entity.setInstance(instance, initialPosition);
    }

    @Override
    public void blockUpdate(@NotNull Instance instance, @NotNull Point pos, @NotNull BlockUpdateInfo info) {
        instance.scheduleNextTick(ignored -> checkFall(instance, pos));
    }
}
