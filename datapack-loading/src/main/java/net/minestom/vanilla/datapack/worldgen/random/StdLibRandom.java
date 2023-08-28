package net.minestom.vanilla.datapack.worldgen.random;

import java.util.Random;
import java.util.random.RandomGenerator;

record StdLibRandom(RandomGenerator random) implements WorldgenRandom {
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
    public WorldgenRandom fork() {
        return new StdLibRandom(new Random(random.nextLong()));
    }

    @Override
    public WorldgenRandom.Positional forkPositional() {
        return new XoroshiroPositionalRandom(random.nextLong(), random.nextLong());
    }
}
