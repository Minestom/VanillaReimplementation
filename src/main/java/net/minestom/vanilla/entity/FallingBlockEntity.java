package net.minestom.vanilla.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Random;

public class FallingBlockEntity extends Entity {
    private static final Random rng = new Random();
    private final Block baseBlock;
    private final BlockHandler toPlace;

    public FallingBlockEntity(Block baseBlock, BlockHandler toPlace, Pos initialPosition) {
        super(EntityType.FALLING_BLOCK);
        this.baseBlock = baseBlock;
        this.toPlace = toPlace;

        setGravity(0.025f, getGravityAcceleration());
        setBoundingBox(0.98f, 0.98f, 0.98f);

        FallingBlockMeta meta = (FallingBlockMeta) this.getEntityMeta();
        meta.setBlock(baseBlock);
        meta.setSpawnPosition(initialPosition);
    }

    @Override
    public void update(long time) {
        // TODO: Cleanup this method structure

        if(!isOnGround()) {
            return;
        }

        Pos position = getPosition().sub(0, 1, 0);

        if (instance.getBlock(position).compare(Block.AIR)) {
            instance.setBlock(getPosition(), baseBlock.withHandler(toPlace));
        } else {
            // landed on non-full block, break into item
            Material correspondingItem = Material.fromNamespaceId(baseBlock.namespace()); // TODO: ugly way of finding corresponding item, change

            if (correspondingItem != null) {
                ItemStack stack = ItemStack.of(correspondingItem, 1);
                ItemEntity itemForm = new ItemEntity(stack);
                itemForm.teleport(new Pos(position.x() + 0.5, position.y(), position.z() + 0.5));

                itemForm.teleport(
                        new Pos(
                                rng.nextGaussian() * 2f,
                                rng.nextFloat() * 2.5f + 2.5f,
                                rng.nextGaussian() * 2f
                        )
                );

                itemForm.setInstance(instance);
            }
        }

        remove();
    }
}
