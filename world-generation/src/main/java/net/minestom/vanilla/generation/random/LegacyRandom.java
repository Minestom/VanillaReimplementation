package net.minestom.vanilla.generation.random;

import net.minestom.vanilla.generation.math.Util;

public class LegacyRandom implements WorldGenRandom {
    private static int MODULUS_BITS = 48;
    private static long MODULUS_MASK = 281474976710655L;
    private static long MULTIPLIER = 25214903917L;
    private static long INCREMENT = 11L;
    private static double FLOAT_MULTIPLIER = 1 / Math.pow(2, 24);
    private static double DOUBLE_MULTIPLIER = 1 / Math.pow(2, 30);

    private long seed;

    public LegacyRandom(long seed) {
        this.setSeed(seed);
    }

    @Override
    public WorldGenRandom fork() {
        return new LegacyRandom(this.nextLong());
    }

    public WorldGenRandom.Positional forkPositional() {
        return new LegacyPositionalRandom(this.nextLong());
    }

    public void setSeed(long seed) {
        this.seed = seed ^ LegacyRandom.MULTIPLIER & LegacyRandom.MODULUS_MASK;
    }

    private void advance() {
        this.seed = this.seed * LegacyRandom.MULTIPLIER + LegacyRandom.INCREMENT & LegacyRandom.MODULUS_MASK;
    }

    public void consume(int count) {
        for (int i = 0; i < count; ++i) {
            this.advance();
        }
    }

//    protected next(bits: number): number {
//        this.advance()
//        const out = Number(this.seed >> BigInt(LegacyRandom.MODULUS_BITS - bits))
//        return out > 2147483647 ? out - 4294967296 : out
//    }
    protected int next(int bits) {
        this.advance();
        int out = (int) (this.seed >> LegacyRandom.MODULUS_BITS - bits);
        return out > 2147483647L ? (int) (out - 4294967296L) : out;
    }

//    public nextInt(max?: number): number {
//        if (max === undefined) {
//            return this.next(32)
//        }
//        if ((max & max - 1) == 0) { // If max is a power of two
//            return Number(BigInt(max) * BigInt(this.next(31)) >> BigInt(31))
//        }
//        let a, b
//        while ((a = this.next(31)) - (b = a % max) + (max - 1) < 0) {}
//        return b
//    }
    public int nextInt(int max) {
        if (max == 0) {
            return this.next(32);
        }
        if ((max & max - 1) == 0) { // If max is a power of two
            return (int) (max * (long) this.next(31) >> 31);
        }
        int a, b;
        while (true) {
            if ((a = this.next(31)) - (b = a % max) + (max - 1) >= 0) break;
        }
        return b;
    }

//    public nextLong() {
//        return (BigInt(this.next(32)) << BigInt(32)) + BigInt(this.next(32))
//    }
    public long nextLong() {
        return ((long) this.next(32) << 32) + (long) this.next(32);
    }

//    public nextFloat(): number {
//        return this.next(24) * LegacyRandom.FLOAT_MULTIPLIER
//    }
    public float nextFloat() {
        return this.next(24) * (float) LegacyRandom.FLOAT_MULTIPLIER;
    }

//    public nextDouble(): number {
//    const a = this.next(30)
//        this.advance()
//        return a * LegacyRandom.DOUBLE_MULTIPLIER
//    }
    public double nextDouble() {
        int a = this.next(30);
        this.advance();
        return a * LegacyRandom.DOUBLE_MULTIPLIER;
    }

//    export class LegacyPositionalRandom implements PositionalRandom {
//        constructor(
//                private readonly seed: bigint,
//                ) {}
//
//        public at(x: number, y: number, z: number) {
//    const seed = getSeed(x, y, z)
//            return new LegacyRandom(seed ^ this.seed)
//        }
//
//        public fromHashOf(name: string) {
//    const hash = md5(name, { asBytes: true })
//    const seed = longfromBytes(hash[0], hash[1], hash[2], hash[3], hash[4], hash[5], hash[6], hash[7])
//            return new LegacyRandom(seed ^ this.seed)
//        }
//
//        seedKey(): [bigint, bigint] {
//            return [this.seed, BigInt(0)]
//        }
//    }
    public static class LegacyPositionalRandom implements WorldGenRandom.Positional {
        private final long seed;

        public LegacyPositionalRandom(long seed) {
            this.seed = seed;
        }

        public WorldGenRandom at(int x, int y, int z) {
            long seed = Util.getSeed(x, y, z);
            return new LegacyRandom(seed ^ this.seed);
        }

        public WorldGenRandom fromHashOf(String name) {
            byte[] hash = Util.md5(name);
            long seed = Util.longfromBytes(hash[0], hash[1], hash[2], hash[3], hash[4], hash[5], hash[6], hash[7]);
            return new LegacyRandom(seed ^ this.seed);
        }

        public long[] seedKey() {
            return new long[] { this.seed, 0L };
        }
    }
}
