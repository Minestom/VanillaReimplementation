package net.minestom.vanilla.utils;

import net.minestom.server.coordinate.Point;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class MathUtils {
    public static void forEachWithinManhattanDistance(Point origin, int distance, Consumer<Point> consumer) {
        for (int i = 0; i <= distance; i++) {
            for (int j = 0; i + j <= distance; j++) {
                for (int k = 0; i + j + k <= distance; k++) {
                    if (i == 0 && j == 0 && k == 0) {
                        continue;
                    }
                    consumer.accept(origin.add(i, j, k));
                    if (i != 0) {
                        consumer.accept(origin.add(-i, j, k));
                        if (j != 0) consumer.accept(origin.add(-i, -j, k));
                        if (k != 0) {
                            consumer.accept(origin.add(-i, j, -k));
                            consumer.accept(origin.add(i, j, -k));
                            consumer.accept(origin.add(-i, -j, -k));
                        }
                    }
                    if (j != 0) {
                        consumer.accept(origin.add(i, -j, k));
                        if (k != 0) consumer.accept(origin.add(i, -j, -k));
                    }
                    if (k != 0) consumer.accept(origin.add(i, j, -k));
                }
            }
        }
    }

    public static Set<Point> getWithinManhattanDistance(Point origin, int distance) {
        Set<Point> points = new HashSet<>();
        forEachWithinManhattanDistance(origin, distance, points::add);
        return points;
    }
}
