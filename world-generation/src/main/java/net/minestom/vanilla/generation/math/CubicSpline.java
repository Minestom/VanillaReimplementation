package net.minestom.vanilla.generation.math;

import java.util.function.Function;

public interface CubicSpline<C> extends NumberFunction<C> {

    interface MinMaxNumberFunction<C> extends NumberFunction<C> {
        double min();

        double max();

        static boolean is(Object obj) {
            return obj instanceof MinMaxNumberFunction;
        }
    }


    double min();

    double max();

    CubicSpline<C> mapAll(Mapper<C> visitor);

    void calculateMinMax();

    static <C> CubicSpline<C> fromJson(Object obj, Function<Object, NumberFunction<C>> extractor) {
        return NativeCubicSplines.fromJson(obj, extractor);
    }
}
