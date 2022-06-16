package net.minestom.vanilla.entities;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.instance.VanillaExplosion;

public class PrimedTNT extends Entity {

    private int fuseTime;

    public PrimedTNT(int fuseTime) {
        super(EntityType.TNT);
        setGravity(0.025f, getGravityAcceleration());
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
        if (fuseTime-- <= 0) {
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
