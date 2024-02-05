package net.minestom.vanilla.datapack.worldgen.random;

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
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }

        if ((bound & bound - 1) == 0) {
            return (int) ((long) bound * (long) this.next(31) >> 31);
        }

        int prev;
        int out;
        prev = this.next(31);
        out = prev % bound;
        while (prev - out + (bound - 1) < 0) {
            prev = this.next(31);
            out = prev % bound;
        }
        return out;
    }

    @Override
    public double nextDouble() {
        int high = this.next(26);
        int low = this.next(27);
        long compose = ((long) high << 27) + (long) low;
        return (double) compose * 1.1102230246251565E-16;
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
