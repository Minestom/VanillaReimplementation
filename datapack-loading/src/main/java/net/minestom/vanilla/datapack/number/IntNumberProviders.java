package net.minestom.vanilla.datapack.number;

import java.util.random.RandomGenerator;

interface IntNumberProviders {

    record Constant(int value) implements NumberProvider.Int {
        @Override
        public int apply(NumberProvider.Context context) {
            return value;
        }
    }

    record Uniform(NumberProvider.Int min, NumberProvider.Int max) implements NumberProvider.Int {
        @Override
        public int apply(NumberProvider.Context context) {
            int min = this.min.apply(context);
            int max = this.max.apply(context);
            return context.random().nextInt(min, max);
        }
    }

    record Binomial(NumberProvider.Int n, NumberProvider.Double p) implements NumberProvider.Int {
        @Override
        public int apply(NumberProvider.Context context) {
            int n = this.n.apply(context);
            double p = this.p.apply(context);
            RandomGenerator random = context.random();
            double sum = 0;
            for (int i = 0; i < n; i++) {
                if (random.nextDouble() < p) {
                    sum++;
                }
            }
            return (int) sum;
        }
    }
}
