package net.minestom.vanilla.datapack.worldgen;

import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minestom.vanilla.datapack.worldgen.random.LegacyRandom;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RandomUnitTests {

    @Test
    public void testLegacyRandom() {
        testLegacyRandom(0);
        testLegacyRandom(534);
        testLegacyRandom(-49273);
        testLegacyRandom(123456789);
    }

    private void testLegacyRandom(long seed) {
        var vri = WorldgenRandom.legacy(seed);
        var vanilla = new LegacyRandomSource(seed);

        for (int i = 0; i < 100; i++) {
            assertEquals(vanilla.nextInt(), vri.nextInt(), "Iteration " + i);
        }

        vri.consumeInt(19283);
        vanilla.consumeCount(19283);

        for (int i = 0; i < 100; i++) {
            assertEquals(vanilla.nextLong(), vri.nextLong(), "Iteration " + i);
        }
    }

    @Test
    public void testXoroshiroRandom() {
        var vri = WorldgenRandom.xoroshiro(0);
        var vanilla = new LegacyRandomSource(0);
    }
}
