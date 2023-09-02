package net.minestom.vanilla.datapack.worldgen.random;

import net.minestom.vanilla.datapack.worldgen.util.Util;

public class LegacyRandom implements WorldgenRandom {

    private long seed;

    public LegacyRandom(long seed) {
        this.seed = (seed ^ 25214903917L) & 281474976710655L;
    }

    @Override
    public long nextLong() {
        int high = this.next(32);
        int low = this.next(32);
        return ((long) high << 32) + (long) low;
    }

    @Override
    public int nextInt() {
        return next(32);
    }

    @Override
    public WorldgenRandom fork() {
        return new LegacyRandom(this.nextLong());
    }

    public WorldgenRandom.Positional forkPositional() {
        return new LegacyPositionalRandom(this.nextLong());
    }

    public int next(int max) {
        long nextSeed = this.seed * 25214903917L + 11L & 281474976710655L;
        this.seed = nextSeed;
        return (int) (nextSeed >> 48 - max);
    }

    private record LegacyPositionalRandom(long seed) implements WorldgenRandom.Positional {

        @Override
        public WorldgenRandom fromHashOf(String name) {
            return fromSeed(name.hashCode());
        }

        @Override
        public WorldgenRandom fromSeed(long seed) {
            return new LegacyRandom(seed ^ this.seed);
        }

        @Override
        public long[] seedKey() {
            return new long[0];
        }
    }
}
