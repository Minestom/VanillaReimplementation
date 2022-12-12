package net.minestom.vanilla.randomticksystem;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class RandomTickManager {

    private static @NotNull String RANDOM_TICK_SYSTEM_PROPERTY = "vri.gamerule.randomtickspeed";

    private final VanillaReimplementation vri;
    private RandomTickManager(VanillaReimplementation vri) {
        this.vri = vri;
    }

    public static void init(VanillaReimplementation vri) {
        RandomTickManager manager = new RandomTickManager(vri);
        vri.process().eventHandler().addListener(InstanceTickEvent.class, event -> {
            int randomTickCount = Integer.parseInt(System.getProperty(RANDOM_TICK_SYSTEM_PROPERTY, "3"));
            manager.handleInstanceTick(event, randomTickCount);
        });
    }

    private void handleInstanceTick(InstanceTickEvent event, int randomTickCount) {
        Instance instance = event.getInstance();
        Random instanceRandom = vri.random(instance);
        for (Chunk chunk : instance.getChunks()) {
            int minSection = chunk.getMinSection();
            int maxSection = chunk.getMaxSection();
            for (int section = minSection; section < maxSection; section++) {
                for (int i = 0; i < randomTickCount; i++) {
                    tickSection(instanceRandom, instance, chunk, section);
                }
            }
        }
    }

    private void tickSection(Random random, Instance instance, Chunk chunk, int minSection) {
        int minX = chunk.getChunkX() * Chunk.CHUNK_SIZE_X;
        int minZ = chunk.getChunkZ() * Chunk.CHUNK_SIZE_Z;
        int minY = minSection * Chunk.CHUNK_SECTION_SIZE;

        int x = minX + random.nextInt(Chunk.CHUNK_SIZE_X);
        int z = minZ + random.nextInt(Chunk.CHUNK_SIZE_Z);
        int y = minY + random.nextInt(Chunk.CHUNK_SECTION_SIZE);
        Point pos = new Vec(x, y, z);

        Block block = instance.getBlock(x, y, z);
        if (block.handler() instanceof RandomTickable randomTickable) {
            randomTickable.randomTick(new RandomTickable.RandomTick() {
                @Override
                public @NotNull Instance instance() {
                    return instance;
                }

                @Override
                public @NotNull Point position() {
                    return pos;
                }

                @Override
                public @NotNull Block block() {
                    return block;
                }
            });
        }
    }
}
