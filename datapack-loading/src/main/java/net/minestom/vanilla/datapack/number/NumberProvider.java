package net.minestom.vanilla.datapack.number;

import java.util.random.RandomGenerator;

public interface NumberProvider {


    interface Context {
        // TODO: Scoreboard query
        RandomGenerator random();
    }

    interface Int {
        int apply(Context context);
        default Double asDouble() {
            return context -> (double) apply(context);
        }

        static NumberProvider.Int constant(int value) {
            return new IntNumberProviders.Constant(value);
        }

        static NumberProvider.Int uniform(NumberProvider.Int min, NumberProvider.Int max) {
            return new IntNumberProviders.Uniform(min, max);
        }

        static NumberProvider.Int binomial(NumberProvider.Int n, NumberProvider.Double p) {
            return new IntNumberProviders.Binomial(n, p);
        }
    }

    interface Double {
        double apply(Context context);
        default Int asInt() {
            return context -> (int) apply(context);
        }

        static NumberProvider.Double constant(double value) {
            return new DoubleNumberProviders.Constant(value);
        }

        static NumberProvider.Double uniform(NumberProvider.Double min, NumberProvider.Double max) {
            return new DoubleNumberProviders.Uniform(min, max);
        }

        static NumberProvider.Double binomial(NumberProvider.Int n, NumberProvider.Double p) {
            return new DoubleNumberProviders.Binomial(n, p);
        }
    }
}
