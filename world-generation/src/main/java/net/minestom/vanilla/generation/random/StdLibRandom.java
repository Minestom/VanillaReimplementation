package net.minestom.vanilla.generation.random;

import net.minestom.vanilla.generation.math.Util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

record StdLibRandom(Random random) implements WorldGenRandom {
    @Override
    public void consume(int count) {
        for (int i = 0; i < count; i++) {
            random.nextLong();
        }
    }

    @Override
    public int nextInt(int max) {
        return random.nextInt(max);
    }

    @Override
    public long nextLong() {
        return random.nextLong();
    }

    @Override
    public float nextFloat() {
        return random.nextFloat();
    }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }

    @Override
    public WorldGenRandom fork() {
        return new StdLibRandom(new Random(random.nextLong()));
    }

    @Override
    public WorldGenRandom.Positional forkPositional() {
        return new XoroshiroPositionalRandom(random.nextLong(), random.nextLong());
    }
}
