package net.minestom.vanilla.entity;

import net.minestom.server.data.Data;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.network.packet.PacketWriter;

import java.util.function.Consumer;

public class PrimedTNT extends ObjectEntity {

    private int fuseTime = 0;

    public PrimedTNT() {
        super(EntityType.TNT.getId());
        setGravity(0.025f);
        setBoundingBox(0.98f, 0.98f, 0.98f);
    }

    @Override
    public Consumer<PacketWriter> getMetadataConsumer() {
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
        Data explosionData = new Data();
        explosionData.set("minestom:from_tnt", true, Boolean.class);
        getInstance().explode(getPosition().getX(), getPosition().getY()+0.5f, getPosition().getZ(), 4f, explosionData);
    }

    @Override
    public void update() {
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
