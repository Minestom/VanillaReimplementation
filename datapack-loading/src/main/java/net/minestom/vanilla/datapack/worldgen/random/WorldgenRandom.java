package net.minestom.vanilla.datapack.worldgen.random;

import net.minestom.vanilla.datapack.worldgen.Util;

import java.util.Random;

public interface WorldgenRandom {
    static WorldgenRandom standard(long seed) {
        return new StdLibRandom(new Random(seed));
    }

    void consume(int count);

    int nextInt(int max);

    long nextLong();

    float nextFloat();

    double nextDouble();

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
