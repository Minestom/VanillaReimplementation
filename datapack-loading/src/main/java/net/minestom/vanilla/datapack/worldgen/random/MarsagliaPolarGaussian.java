package net.minestom.vanilla.datapack.worldgen.random;

import net.minestom.vanilla.datapack.worldgen.util.Util;

class MarsagliaPolarGaussian {
    public final WorldgenRandom random;
    private double nextGaussian;
    private boolean hasNextGaussian;

    public MarsagliaPolarGaussian(WorldgenRandom random) {
        this.random = random;
    }

    public void reset() {
        this.hasNextGaussian = false;
    }

    public double nextGaussian() {
        if (this.hasNextGaussian) {
            this.hasNextGaussian = false;
            return this.nextGaussian;
        } else {
            double a;
            double b;
            double c;
            do {
                do {
                    a = 2.0 * random.nextDouble() - 1.0;
                    b = 2.0 * random.nextDouble() - 1.0;
                    c = Util.square(a) + Util.square(b);
                } while(c >= 1.0);
            } while(c == 0.0);

            double $$3 = Math.sqrt(-2.0 * Math.log(c) / c);
            this.nextGaussian = b * $$3;
            this.hasNextGaussian = true;
            return a * $$3;
        }
    }
}
