package net.minestom.vanilla.generation.densityfunctions;

import net.minestom.server.coordinate.Point;
import net.minestom.vanilla.generation.math.NumberFunction;

public interface DensityFunction extends DensityFunctions, NumberFunction<Point> {
    double compute(Point point);

    default double minValue() {
        return -maxValue();
    }

    double maxValue();

    default DensityFunction mapAll(DensityFunction.Visitor visitor) {
        return visitor.map().apply(this);
    }

}
