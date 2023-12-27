package net.minestom.vanilla.datapack.number;

import java.util.random.RandomGenerator;

interface DoubleNumberProviders {

    record Constant(double value) implements NumberProvider.Double {
        @Override
        public double apply(NumberProvider.Context context) {
            return value;
        }
    }

    record Uniform(NumberProvider.Double min, NumberProvider.Double max) implements NumberProvider.Double {
        @Override
        public double apply(NumberProvider.Context context) {
            double min = this.min.apply(context);
            double max = this.max.apply(context);
            return context.random().nextDouble(min, max);
        }
    }

    record Binomial(NumberProvider.Int n, NumberProvider.Double p) implements NumberProvider.Double {
        @Override
        public double apply(NumberProvider.Context context) {
            int n = this.n.apply(context);
            double p = this.p.apply(context);
            RandomGenerator random = context.random();
            double sum = 0;
            for (int i = 0; i < n; i++) {
                if (random.nextDouble() < p) {
                    sum++;
                }
            }
            return sum;
        }
    }
}
