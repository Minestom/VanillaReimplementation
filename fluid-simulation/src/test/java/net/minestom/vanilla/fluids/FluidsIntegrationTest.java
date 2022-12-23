package net.minestom.vanilla.fluids;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class FluidsIntegrationTest {

    @Test
    public void testWaterFlow(Env env) {
        VanillaReimplementation vri = VanillaReimplementation.hook(env.process());

        // Setup instance
        Instance instance = vri.createInstance(NamespaceID.from("kry:world"), VanillaDimensionTypes.OVERWORLD);
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 16, Block.STONE));
        ChunkUtils.forChunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());

        // Place water and wait for flow
        instance.setBlock(0, 16, 0, Block.WATER);

        // Tick for 5 * distance - 1 ticks
        int distance = 7;
        for (int i = 0; i < (5 * distance) - 1; i++) {
            env.tick();
        }

        // Check that the water has not flowed all the way
        for (int x = -distance; x <= distance; x++) {
            for (int z = -distance; z <= distance; z++) {
                if (Math.abs(x) + Math.abs(z) != distance) continue;
                assertFalse(Block.WATER.compare(instance.getBlock(x, 16, z)),
                        "Water flowed too quickly from (0, 16, 0) to (" + x + ", 16, " + z + ")");
            }
        }

        // Do the last tick
        env.tick();

        // Check that the water flowed out 7 blocks (manhattan) in all directions
        for (int x = -distance; x <= distance; x++) {
            for (int z = -distance; z <= distance; z++) {
                if (Math.abs(x) + Math.abs(z) > distance) continue;
                assertTrue(Block.WATER.compare(instance.getBlock(x, 16, z)),
                        "Water did not flow out 7 blocks in all directions from (0, 16, 0) to (" + x + ", 16, " + z + ")");
            }
        }
    }

    @Test
    public void testLavaFlow(Env env) {
        VanillaReimplementation vri = VanillaReimplementation.hook(env.process());

        // Setup instance
        Instance instance = vri.createInstance(NamespaceID.from("kry:world"), VanillaDimensionTypes.OVERWORLD);
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 16, Block.STONE));
        ChunkUtils.forChunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());

        // Place water and wait for flow
        instance.setBlock(0, 16, 0, Block.LAVA);

        // Tick for flowrate * distance - 1 ticks
        int flowrate = 30;
        int distance = 3;
        for (int i = 0; i < (flowrate * distance) - 1; i++) {
            env.tick();
        }

        // Check that the fluid has not flowed all the way yet
        for (int x = -distance; x <= distance; x++) {
            for (int z = -distance; z <= distance; z++) {
                if (Math.abs(x) + Math.abs(z) != distance) continue;
                assertFalse(Block.LAVA.compare(instance.getBlock(x, 16, z)),
                        "Water flowed too quickly from (0, 16, 0) to (" + x + ", 16, " + z + ")");
            }
        }

        // Do the last tick
        env.tick();

        // Check that the fluid flowed out distance blocks (manhattan) in all directions
        for (int x = -distance; x <= distance; x++) {
            for (int z = -distance; z <= distance; z++) {
                if (Math.abs(x) + Math.abs(z) > distance) continue;
                assertTrue(Block.LAVA.compare(instance.getBlock(x, 16, z)),
                        "Water did not flow out " + distance + " blocks in all directions from (0, 16, 0) to (" + x + ", 16, " + z + ")");
            }
        }
    }
}
