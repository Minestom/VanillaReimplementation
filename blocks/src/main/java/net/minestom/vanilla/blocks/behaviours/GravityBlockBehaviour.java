package net.minestom.vanilla.blocks.behaviours;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.blocks.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.blockupdatesystem.BlockUpdatable;
import net.minestom.vanilla.blockupdatesystem.BlockUpdateInfo;
import net.minestom.vanilla.blockupdatesystem.BlockUpdateManager;
import net.minestom.vanilla.entitymeta.EntityTags;
import org.jetbrains.annotations.NotNull;

public class GravityBlockBehaviour extends VanillaBlockBehaviour implements BlockUpdatable {
    public GravityBlockBehaviour(@NotNull VanillaBlocks.BlockContext context) {
        super(context);
    }

    @Override
    public void onPlace(@NotNull VanillaPlacement placement) {
        Instance instance = placement.instance();
        Point position = placement.position();
        Block block = placement.blockToPlace();

        if (checkFall(instance, position, block)) {
            placement.blockToPlace(Block.AIR);
        }
    }

    /**
     * Checks if a block should fall
     *
     * @param instance the instance the block is in
     * @param position the position of the block
     * @return true if the block should fall
     */
    public boolean checkFall(Instance instance, Point position, Block block) {
        Block below = instance.getBlock(position.blockX(), position.blockY() - 1, position.blockZ());

        // Exit out now if block below is solid
        if (below.isSolid()) {
            return false;
        }

        // Schedule block update
        BlockUpdateManager.from(instance).scheduleNeighborsUpdate(position, BlockUpdateInfo.MOVE_BLOCK());

        // Create the context
        Pos initialPosition = new Pos(position.x() + 0.5f, Math.round(position.y()), position.z() + 0.5f);
        VanillaRegistry.EntityContext entityContext = context.vri().entityContext(EntityType.FALLING_BLOCK,
                initialPosition, nbt -> nbt.setTag(EntityTags.FallingBlock.BLOCK, block));
        Entity entity = context.vri().createEntityOrDummy(entityContext);

        // Spawn the entity
        entity.setInstance(instance, initialPosition);
        return true;
    }

    @Override
    public void blockUpdate(@NotNull Instance instance, @NotNull Point pos, @NotNull BlockUpdateInfo info) {
        checkFall(instance, pos, instance.getBlock(pos));
    }
}
