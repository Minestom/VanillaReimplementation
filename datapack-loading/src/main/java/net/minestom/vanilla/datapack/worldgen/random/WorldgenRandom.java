package net.minestom.vanilla.datapack.worldgen.random;

import net.minestom.vanilla.datapack.worldgen.util.Util;

import java.util.random.RandomGenerator;

public interface WorldgenRandom extends RandomGenerator {
    static WorldgenRandom xoroshiro(long seed) {
        return new XoroshiroRandom(seed);
    }

    static WorldgenRandom legacy(long i) {
        return new LegacyRandom(i);
    }

    long nextLong();

    default void consumeInt(int i) {
        for (int j = 0; j < i; j++) {
            nextInt();
        }
    }

    WorldgenRandom fork();

    Positional forkPositional();

    interface Positional {

        default WorldgenRandom at(int x, int y, int z) {
            return fromSeed(Util.getSeed(x, y, z));
        }

        WorldgenRandom fromHashOf(String name);
        WorldgenRandom fromSeed(long seed);

        long[] seedKey();
    }
}
