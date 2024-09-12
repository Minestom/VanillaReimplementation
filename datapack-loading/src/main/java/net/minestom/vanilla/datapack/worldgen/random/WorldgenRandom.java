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

    default void consumeLong(int i) {
        for (int j = 0; j < i; j++) {
            nextLong();
        }
    }

    WorldgenRandom fork();

    Positional forkPositional();

    WorldgenRandom withSeed(long seed);

    default WorldgenRandom withDecorationSeed(long baseSeed, int x, int z) {
        WorldgenRandom out = this.withSeed(baseSeed);
        long random1 = this.nextLong() | 1L;
        long random2 = this.nextLong() | 1L;
        long combinedSeed = (long)x * random1 + (long)z * random2 ^ baseSeed;
        return out.withSeed(combinedSeed);
    }

    default WorldgenRandom withFeatureSeed(long baseSeed, int x, int z) {
        long combinedSeed = baseSeed + (long)x + (10000L * z);
        return this.withSeed(combinedSeed);
    }

    default WorldgenRandom withLargeFeatureSeed(long baseSeed, int x, int z) {
        WorldgenRandom out = this.withSeed(baseSeed);
        long random1 = this.nextLong();
        long random2 = this.nextLong();
        long combinedSeed = (long)x * random1 ^ (long)z * random2 ^ baseSeed;
        return out.withSeed(combinedSeed);
    }

    default WorldgenRandom withLargeFeatureWithSalt(long baseSeed, int x, int z, int salt) {
        long combinedSeed = (long)x * 341873128712L + (long)z * 132897987541L + baseSeed + (long)salt;
        return this.withSeed(combinedSeed);
    }

    interface Positional {

        default WorldgenRandom at(int x, int y, int z) {
            return fromSeed(Util.getSeed(x, y, z));
        }

        WorldgenRandom fromHashOf(String name);
        WorldgenRandom fromSeed(long seed);

        long[] seedKey();
    }
}
