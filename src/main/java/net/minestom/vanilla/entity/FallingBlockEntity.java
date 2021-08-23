package net.minestom.vanilla.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Random;

public class FallingBlockEntity extends Entity {
    private static final Random rng = new Random();
    private final Block toPlace;

    public FallingBlockEntity(Block toPlace, Pos initialPosition) {
        super(EntityType.FALLING_BLOCK);
        this.toPlace = toPlace;

        // setGravity(0.025f, getGravityAcceleration());
        setBoundingBox(0.98f, 0.98f, 0.98f);

        FallingBlockMeta meta = (FallingBlockMeta) this.getEntityMeta();
        meta.setBlock(toPlace);
        meta.setSpawnPosition(initialPosition);
    }

    @Override
    public void update(long time) {
        // TODO: Cleanup this method structure

        if (!isOnGround()) {
            return;
        }

        if (getVelocity().y() < 0.0) {
            return;
        }

        Block block = instance.getBlock(position);

        if (!block.isAir()) {
            // TODO: Better way to get block's loot
            Material loot = Material.fromNamespaceId(toPlace.namespace());

            ItemEntity entity = new ItemEntity(ItemStack.of(loot));
            entity.setInstance(instance);
            entity.teleport(position);
            remove();
        }

        instance.setBlock(position, toPlace);
        remove();
    }
}
