package net.minestom.vanilla.generation.random;

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

        WorldgenRandom at(int x, int y, int z);

        WorldgenRandom fromHashOf(String name);

        long[] seedKey();
    }
}
