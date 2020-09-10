package net.minestom.vanilla.entity;

import net.minestom.server.data.Data;
import net.minestom.server.data.DataImpl;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.vanilla.instance.VanillaExplosion;

import java.util.function.Consumer;

public class PrimedTNT extends ObjectEntity {

    private int fuseTime = 0;

    public PrimedTNT(Position initialPosition) {
        super(EntityType.TNT, initialPosition);
        setGravity(0.025f);
        setBoundingBox(0.98f, 0.98f, 0.98f);
    }

    @Override
    public Consumer<BinaryWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            packet.writeByte((byte)7); // data index
            packet.writeByte(METADATA_VARINT);
            packet.writeVarInt(fuseTime);
        };
    }

    private void explode() {
        if(shouldRemove())
            return;
        remove();
        Data explosionData = new DataImpl();
        explosionData.set(VanillaExplosion.DROP_EVERYTHING_KEY, true, Boolean.class);
        Block block = Block.fromStateId(instance.getBlockStateId((int)Math.floor(getPosition().getX()), (int)Math.floor(getPosition().getY()+0.5f), (int)Math.floor(getPosition().getZ())));
        if(block.isLiquid()) {
            explosionData.set(VanillaExplosion.DONT_DESTROY_BLOCKS_KEY, true, Boolean.class);
        }
        getInstance().explode(getPosition().getX(), getPosition().getY()+0.5f, getPosition().getZ(), 4f, explosionData);
    }

    @Override
    public void update(long time) {
        if(fuseTime-- <= 0) {
            explode();
        }
    }

    @Override
    public int getObjectData() {
        return 0;
    }

    @Override
    public void spawn() {

    }

    public int getFuseTime() {
        return fuseTime;
    }

    public void setFuseTime(int fuseTime) {
        this.fuseTime = fuseTime;
    }
}
