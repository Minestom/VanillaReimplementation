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
        testFluid(env, Block.WATER, 5, 7);
    }

    @Test
    public void testLavaFlow(Env env) {
        testFluid(env, Block.LAVA, 30, 3);
    }

    private void testFluid(Env env, Block fluid, int flowrate, int distance) {
        VanillaReimplementation vri = VanillaReimplementation.hook(env.process());

        // Setup instance
        Instance instance = vri.createInstance(NamespaceID.from("kry:world"), VanillaDimensionTypes.OVERWORLD);
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 16, Block.STONE));
        ChunkUtils.forChunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());

        // Place fluid and wait for flow
        instance.setBlock(0, 16, 0, fluid);

        // Tick for flowrate * distance - 1 ticks
        for (int i = 0; i < (flowrate * distance) - 1; i++) {
            env.tick();
        }

        // Check that the fluid has not flowed all the way yet
        for (int x = -distance; x <= distance; x++) {
            for (int z = -distance; z <= distance; z++) {
                if (Math.abs(x) + Math.abs(z) != distance) continue;
                assertFalse(fluid.compare(instance.getBlock(x, 16, z)),
                        "Fluid flowed too quickly from (0, 16, 0) to (" + x + ", 16, " + z + ")");
            }
        }

        // Do the last tick
        env.tick();

        // Check that the fluid flowed out distance blocks (manhattan) in all directions
        for (int x = -distance; x <= distance; x++) {
            for (int z = -distance; z <= distance; z++) {
                if (Math.abs(x) + Math.abs(z) > distance) continue;
                assertTrue(fluid.compare(instance.getBlock(x, 16, z)),
                        "Fluid did not flow out " + distance + " blocks in all directions from (0, 16, 0) to (" + x + ", 16, " + z + ")");
            }
        }
    }
}
