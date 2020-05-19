package net.minestom.vanilla.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.Vector;

import java.io.FileNotFoundException;
import java.util.*;

public class VanillaExplosion extends Explosion {

    public static final String DROP_EVERYTHING_KEY = "minestom:drop_everything";
    public static final String IS_FLAMING_KEY = "minestom:is_flaming";
    private static final Random explosionRNG = new Random();

    private final boolean startsFires;
    private final boolean dropsEverything;

    public VanillaExplosion(float centerX, float centerY, float centerZ, float strength, boolean startsFires, boolean dropsEverything) {
        super(centerX, centerY, centerZ, strength);
        this.startsFires = startsFires;
        this.dropsEverything = dropsEverything;
    }

    @Override
    protected List<BlockPosition> prepare(Instance instance) {
        Set<BlockPosition> positions = new HashSet<>();

        float stepLength = 0.3f;
        float maximumBlastRadius = (float) Math.floor(1.3f*getStrength()/(stepLength*0.75))*stepLength;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if(!(x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15)) { // must be on outer edge of 16x16x16 cube
                        continue;
                    }
                    Vector ray = new Vector(x-8.5f, y-8.5f, z-8.5f);
                    ray.normalize().multiply(stepLength);
                    float intensity = (0.7f + explosionRNG.nextFloat() * 0.6f) * getStrength();

                    Vector position = new Vector(getCenterX(), getCenterY(), getCenterZ());
                    BlockPosition blockPos = new BlockPosition(position);
                    for (float step = 0f; step < maximumBlastRadius; step += stepLength) {
                        intensity -= 0.225f; // air attenuation

                        blockPos.setX((int) Math.floor(position.getX()));
                        blockPos.setY((int) Math.floor(position.getY()));
                        blockPos.setZ((int) Math.floor(position.getZ()));

                        if(blockPos.getY() < 0 || blockPos.getY() >= 255) { // out of bounds
                            break;
                        }

                        Block block = Block.fromId(instance.getBlockId(blockPos));
                        CustomBlock customBlock = instance.getCustomBlock(blockPos);

                        float blastResistance = 0.05f; // TODO: custom blast resistances
                        intensity -= (blastResistance+stepLength)*stepLength;
                        if(intensity < 0f) {
                            break;
                        }


                        if(!positions.contains(blockPos)) {
                            positions.add(new BlockPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                        }

                        position.add(ray.getX(), ray.getY(), ray.getZ());
                    }
                }
            }
        }

        for(BlockPosition position : positions) {
            Block block = Block.fromId(instance.getBlockId(position));
            CustomBlock customBlock = instance.getCustomBlock(position);

            if(block.isAir())
                continue;
            double p = explosionRNG.nextDouble();
            boolean shouldExplode = p <= 1/getStrength();
            if(shouldExplode) {
                Data lootTableArguments = new Data();
                if(!dropsEverything) {
                    lootTableArguments.set("explosionPower", (double)getStrength(), Double.class);
                }
                if(customBlock != null) {
                    if(!customBlock.onExplode(instance, position, lootTableArguments)) {
                        continue;
                    }
                }
                LootTableManager lootTableManager = MinecraftServer.getLootTableManager();
                try {
                    LootTable table = null;
                    if(customBlock != null) {
                        table = customBlock.getLootTable(lootTableManager);
                    }
                    if(table == null) {
                        table = lootTableManager.load(NamespaceID.from("blocks/"+block.name().toLowerCase()));
                    }
                    List<ItemStack> output = table.generate(lootTableArguments);
                    for (ItemStack out : output) {
                        ItemEntity itemEntity = new ItemEntity(out);
                        itemEntity.getPosition().setX(position.getX()+explosionRNG.nextFloat());
                        itemEntity.getPosition().setY(position.getY()+explosionRNG.nextFloat());
                        itemEntity.getPosition().setZ(position.getZ()+explosionRNG.nextFloat());
                        itemEntity.setPickupDelay(500L);
                        itemEntity.setInstance(instance);
                    }
                } catch (FileNotFoundException e) {
                    // loot table does not exist, ignore
                }
            }
        }

        // TODO: entities

        return new LinkedList<>(positions);
    }
}
