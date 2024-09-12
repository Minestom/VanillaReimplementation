package net.minestom.vanilla.randomticksystem;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

public class RandomTickManager {

    private static final @NotNull String RANDOM_TICK_SYSTEM_PROPERTY = "vri.gamerule.randomtickspeed";

    private static final Map<VanillaReimplementation, RandomTickManager> vri2managers =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final Short2ObjectMap<RandomTickable> randomTickables = new Short2ObjectOpenHashMap<>();

    private final VanillaReimplementation vri;
    private RandomTickManager(VanillaReimplementation vri) {
        this.vri = vri;
    }

    public static @NotNull RandomTickManager create(@NotNull VanillaReimplementation vri) {
        return vri2managers.computeIfAbsent(vri, RandomTickManager::new);
    }

    public static void init(VanillaReimplementation.Feature.@NotNull HookContext context) {
        RandomTickManager manager = create(context.vri());
        context.vri().process().eventHandler().addListener(InstanceTickEvent.class, event -> {
            int randomTickCount = Integer.parseInt(System.getProperty(RANDOM_TICK_SYSTEM_PROPERTY, "3"));
            manager.handleInstanceTick(event, randomTickCount);
        });
    }

    public static void registerRandomTickable(short stateId, RandomTickable randomTickable) {
        synchronized (randomTickables) {
            randomTickables.put(stateId, randomTickable);
        }
    }

    private void handleInstanceTick(InstanceTickEvent event, int randomTickCount) {
        Instance instance = event.getInstance();
        Random instanceRandom = vri.random(instance);
        synchronized (randomTickables) {
            for (Chunk chunk : instance.getChunks()) {
                int minSection = chunk.getMinSection();
                int maxSection = chunk.getMaxSection();
                for (int section = minSection; section < maxSection; section++) {
                    for (int i = 0; i < randomTickCount; i++) {
                        randomTickSection(instanceRandom, instance, chunk, section);
                    }
                }
            }
        }
    }

    private void randomTickSection(Random random, Instance instance, Chunk chunk, int minSection) {
        int minX = chunk.getChunkX() * Chunk.CHUNK_SIZE_X;
        int minZ = chunk.getChunkZ() * Chunk.CHUNK_SIZE_Z;
        int minY = minSection * Chunk.CHUNK_SECTION_SIZE;

        int x = minX + random.nextInt(Chunk.CHUNK_SIZE_X);
        int z = minZ + random.nextInt(Chunk.CHUNK_SIZE_Z);
        int y = minY + random.nextInt(Chunk.CHUNK_SECTION_SIZE);
        Point pos = new Vec(x, y, z);

        Block block = instance.getBlock(x, y, z);
        RandomTickable randomTickable = randomTickables.get((short) block.stateId());
        if (randomTickable == null) return;
        randomTickable.randomTick(new RandomTick(instance, pos, block));
    }

    private record RandomTick(Instance instance, Point position, Block block) implements RandomTickable.RandomTick {}
}
