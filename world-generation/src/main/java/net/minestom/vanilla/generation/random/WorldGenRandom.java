package net.minestom.vanilla.generation.random;

import java.util.Random;

public interface WorldGenRandom {
    static WorldGenRandom standard(long seed) {
        return new StdLibRandom(new Random(seed));
    }
//    consume(count: number): void
//    nextInt(max?: number): number
//    nextLong(): bigint
//    nextFloat(): number
//    nextDouble(): number
//    fork(): Random
//    forkPositional(): PositionalRandom

    void consume(int count);

    int nextInt(int max);

    long nextLong();

    float nextFloat();

    double nextDouble();

    WorldGenRandom fork();

    Positional forkPositional();

    interface Positional {

        WorldGenRandom at(int x, int y, int z);

        WorldGenRandom fromHashOf(String name);

        long[] seedKey();
    }
}
