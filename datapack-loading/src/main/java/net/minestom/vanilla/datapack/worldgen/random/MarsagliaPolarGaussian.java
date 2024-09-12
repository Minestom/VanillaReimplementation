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
            double x;
            double y;
            double z;
            do {
                do {
                    x = 2.0 * random.nextDouble() - 1.0;
                    y = 2.0 * random.nextDouble() - 1.0;
                    z = Util.square(x) + Util.square(y);
                } while(z >= 1.0);
            } while(z == 0.0);

            double gaussianFactor = Math.sqrt(-2.0 * Math.log(z) / z);
            this.nextGaussian = y * gaussianFactor;
            this.hasNextGaussian = true;
            return x * gaussianFactor;
        }
    }
}
