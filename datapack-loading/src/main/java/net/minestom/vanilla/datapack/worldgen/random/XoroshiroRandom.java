package net.minestom.vanilla.datapack.worldgen.random;

import net.minestom.vanilla.datapack.worldgen.util.Util;

public class XoroshiroRandom implements WorldgenRandom {

    private final Xoroshiro128PlusPlus xpp;

    public XoroshiroRandom(long seed) {
        this(Util.extract128Seed(seed).mixed());
    }

    public XoroshiroRandom(Util.Seed seed) {
        this(seed.low(), seed.high());
    }

    public XoroshiroRandom(long seedLow, long seedHigh) {
        this.xpp = new Xoroshiro128PlusPlus(seedLow, seedHigh);
    }

    @Override
    public long nextLong() {
        return this.xpp.nextLong();
    }

    @Override
    public int nextInt() {
        return (int) this.xpp.nextLong();
    }

    @Override
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }

        long nextUInt = Integer.toUnsignedLong(this.nextInt());
        long random = nextUInt * (long) bound;
        long limit = random & 4294967295L;
        if (limit < (long) bound) {
            for (int remainder = Integer.remainderUnsigned(~bound + 1, bound); limit < (long) remainder; limit = random & 0xffffffffL) {
                nextUInt = Integer.toUnsignedLong(this.nextInt());
                random = nextUInt * (long)bound;
            }
        }

        return (int) (random >> 32);
    }

    @Override
    public float nextFloat() {
        return (float) this.nextBits(24) * 0x1.0p-24f;
    }

    @Override
    public double nextDouble() {
        return (double) this.nextBits(53) * 0x1.0p-53;
    }

    private long nextBits(int bitCount) {
        return this.xpp.nextLong() >>> 64 - bitCount;
    }

    @Override
    public WorldgenRandom fork() {
        return new XoroshiroRandom(xpp.nextLong(), xpp.nextLong());
    }

    @Override
    public Positional forkPositional() {
        return new XoroshiroPositionalRandom(xpp.nextLong(), xpp.nextLong());
    }

    private static class Xoroshiro128PlusPlus {
        private long seedLow;
        private long seedHigh;

        public Xoroshiro128PlusPlus(long seedLow, long seedHigh) {
            this.seedLow = seedLow;
            this.seedHigh = seedHigh;
            if ((this.seedLow | this.seedHigh) == 0L) {
                this.seedLow = -7046029254386353131L;
                this.seedHigh = 7640891576956012809L;
            }
        }

        public long nextLong() {
            long low = this.seedLow;
            long high = this.seedHigh;
            long $$2 = Long.rotateLeft(low + high, 17) + low;
            high ^= low;
            this.seedLow = Long.rotateLeft(low, 49) ^ high ^ high << 21;
            this.seedHigh = Long.rotateLeft(high, 28);
            return $$2;
        }
    }
}
