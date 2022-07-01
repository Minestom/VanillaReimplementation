package net.minestom.vanilla.entities;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.vanilla.go_away.VanillaRegistry;
import net.minestom.vanilla.entitymeta.EntityTags;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class FallingBlockEntity extends Entity {
    private static final Random rng = new Random();
    private final @NotNull Block toPlace;

    public FallingBlockEntity(@NotNull Block toPlace, @NotNull Pos initialPosition) {
        super(EntityType.FALLING_BLOCK);
        this.toPlace = toPlace;

        // setGravity(0.025f, getGravityAcceleration());
        setBoundingBox(0.98f, 0.98f, 0.98f);

        FallingBlockMeta meta = (FallingBlockMeta) this.getEntityMeta();
        meta.setBlock(toPlace);
        meta.setSpawnPosition(initialPosition);
    }

    public FallingBlockEntity(@NotNull VanillaRegistry.EntityContext context) {
        this(Objects.requireNonNullElse(context.getTag(EntityTags.FallingBlock.BLOCK), Block.AIR), context.position());
    }

    @Override
    public void update(long time) {
        // TODO: Cleanup this method structure

        // TODO: This isOnGround method seems to snap the entity to the ground earlier than expected
        if (!isOnGround()) {
            return;
        }

        Block block = instance.getBlock(position);

        if (block.registry().isSolid()) {
            // TODO: Better way to get block's loot
            Material loot = block.registry().material();
            if (loot != null) {
                ItemEntity itemEntity = new ItemEntity(ItemStack.of(loot));
                itemEntity.setInstance(instance, position);
            }
            remove();
            return;
        }

        instance.setBlock(position, toPlace);
        remove();
    }
}
