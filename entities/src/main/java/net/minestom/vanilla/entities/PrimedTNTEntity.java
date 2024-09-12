package net.minestom.vanilla.entities;

import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.entitymeta.EntityTags;
import net.minestom.vanilla.instance.VanillaExplosion;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PrimedTNTEntity extends Entity {

    private int fuseTime;

    public PrimedTNTEntity(@NotNull VanillaRegistry.EntityContext context) {
        this(Objects.requireNonNullElse(context.getTag(EntityTags.PrimedTnt.FUSE_TIME), 80));
    }

    public PrimedTNTEntity(int fuseTime) {
        super(EntityType.TNT);
        setAerodynamics(getAerodynamics().withVerticalAirResistance(0.98f));
        setBoundingBox(0.98f, 0.98f, 0.98f);
        this.fuseTime = fuseTime;

        PrimedTntMeta meta = (PrimedTntMeta) this.getEntityMeta();
        meta.setFuseTime(fuseTime);
    }

    private void explode() {
        remove();

        Block block = instance.getBlock(this.getPosition());

        VanillaExplosion explosion = VanillaExplosion.builder(getPosition(), 4.0f)
                .destroyBlocks(!block.isLiquid())
                .build();

        explosion.trigger(instance);
    }

    @Override
    public void update(long time) {
        super.update(time);
        if (fuseTime-- <= 20) {
            explode();
        }
    }

    public int getFuseTime() {
        return fuseTime;
    }

    public void setFuseTime(int fuseTime) {
        this.fuseTime = fuseTime;
    }
}
