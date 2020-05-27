package net.minestom.vanilla.entity;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;

import java.util.Random;
import java.util.function.Consumer;

public class FallingBlockEntity extends ObjectEntity {
    private final Block baseBlock;
    private final CustomBlock toPlace;

    public FallingBlockEntity(Block baseBlock, CustomBlock toPlace, Position initialPosition) {
        super(EntityType.FALLING_BLOCK, initialPosition);
        this.baseBlock = baseBlock;
        this.toPlace = toPlace;
        setGravity(0.025f);
        setBoundingBox(0.98f, 0.98f, 0.98f);
    }

    @Override
    public int getObjectData() {
        return baseBlock.getBlockId();
    }

    @Override
    public void spawn() {

    }

    @Override
    public void update() {
        if(isOnGround()) {
            BlockPosition position = getPosition().toBlockPosition().subtract(0, 1, 0);
            if(instance.getBlockId(position) != Block.AIR.getBlockId()) {
                // landed on non-full block, break into item
                Material correspondingItem = Material.valueOf(baseBlock.name()); // TODO: ugly way of finding corresponding item, change
                ItemStack stack = new ItemStack(correspondingItem, (byte) 1);
                ItemEntity itemForm = new ItemEntity(stack, new Position(position.getX()+0.5f, position.getY(), position.getZ()+0.5f));

                Random rng = new Random();
                itemForm.getVelocity().setX((float) rng.nextGaussian()*2f);
                itemForm.getVelocity().setY(rng.nextFloat()*2.5f+2.5f);
                itemForm.getVelocity().setZ((float) rng.nextGaussian()*2f);

                itemForm.setInstance(instance);
            } else {
                if(toPlace != null) {
                    instance.setSeparateBlocks(position.getX(), position.getY(), position.getZ(), baseBlock.getBlockId(), toPlace.getCustomBlockId());
                } else {
                    instance.setBlock(getPosition(), baseBlock);
                }
            }
            remove();
        }
    }

    @Override
    public Consumer<PacketWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            packet.writeByte((byte)7); // data index
            packet.writeByte((byte) 9);
            // https://wiki.vg/Entity_metadata#FallingBlock
            BlockPosition spawnPosition = getPosition().toBlockPosition();
            packet.writeBlockPosition(spawnPosition);
        };
    }
}
