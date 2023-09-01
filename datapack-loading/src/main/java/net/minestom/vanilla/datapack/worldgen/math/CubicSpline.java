package net.minestom.vanilla.datapack.worldgen.math;

import com.squareup.moshi.JsonReader;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.worldgen.DensityFunction;
import net.minestom.vanilla.datapack.worldgen.util.Util;

import java.io.IOException;
import java.util.*;

public interface CubicSpline extends NumberFunction<DensityFunction.Context> {

    static CubicSpline fromJson(JsonReader reader) throws IOException {
        return JsonUtils.typeMap(reader, token -> switch (token) {
            case NUMBER -> json -> new Constant(json.nextDouble());
            case BEGIN_OBJECT -> json -> DatapackLoader.moshi(MultiPoint.class).apply(json);
            default -> null;
        });
    }


    double min();

    double max();
    record Constant(double value) implements CubicSpline {
        @Override
        public double min() {
            return value;
        }

        @Override
        public double max() {
            return value;
        }

        @Override
        public double compute(DensityFunction.Context context) {
            return value;
        }
    }

    record MultiPoint(DensityFunction coordinate, List<Point> points) implements CubicSpline {

        // TODO: Remove this static map, and instead use lazily loaded class fields
        private static final Map<MultiPoint, CachedMinMax> cache = Collections.synchronizedMap(new WeakHashMap<>());

        private record CachedMinMax(double min, double max) {
        }

        @Override
        public double compute(DensityFunction.Context coordinate) {
            double c = this.coordinate.compute(coordinate);
            int pointsLength = this.points.size();
            int i = Util.binarySearch(0, pointsLength, n -> c < this.points.get(n).location() - 1);
            int n = pointsLength - 1;

            if (i < 0) {
                Point point = this.points.get(0);
                return point.value().compute(coordinate) + point.derivative() * (c - point.location());
            }
            if (i == n) {
                Point point = this.points.get(n);
                return point.value().compute(coordinate) + point.derivative() * (c - point.location());
            }
            if (i > n) {
                throw new IllegalStateException("i > n");
            }

            Point point0 = this.points.get(i);
            Point point1 = this.points.get(i + 1);
            double loc0 = point0.location();
            double loc1 = point1.location();
            double der0 = point0.derivative();
            double der1 = point1.derivative();
            double f = (c - loc0) / (loc1 - loc0);

            double val0 = point0.value().compute(coordinate);
            double val1 = point1.value().compute(coordinate);

            double f8 = der0 * (loc1 - loc0) - (val1 - val0);
            double f9 = -der1 * (loc1 - loc0) + (val1 - val0);
            return Util.lerp(f, val0, val1) + f * (1.0 - f) * Util.lerp(f, f8, f9);
        }

        private CachedMinMax minMax() {
            if (cache.containsKey(this)) {
                return cache.get(this);
            }
            int pointsLength = this.points.size();
            int lastIdx = pointsLength - 1;
            double splineMin = Double.POSITIVE_INFINITY;
            double splineMax = Double.NEGATIVE_INFINITY;
            double coordinateMin = coordinate.minValue();
            double coordinateMax = coordinate.maxValue();
            Point first = this.points.get(0);

            if (coordinateMin < first.location()) {
                double minExtend = MultiPoint.linearExtend(coordinateMin, first, first.value().min());
                double maxExtend = MultiPoint.linearExtend(coordinateMin, first, first.value().max());
                splineMin = Math.min(splineMin, Math.min(minExtend, maxExtend));
                splineMax = Math.max(splineMax, Math.max(minExtend, maxExtend));
            }
            Point last = this.points.get(lastIdx);

            if (coordinateMax > last.location()) {
                double minExtend = MultiPoint.linearExtend(coordinateMax, last, last.value().min());
                double maxExtend = MultiPoint.linearExtend(coordinateMax, last, last.value().max());
                splineMin = Math.min(splineMin, Math.min(minExtend, maxExtend));
                splineMax = Math.max(splineMax, Math.max(minExtend, maxExtend));
            }

            for (Point point : points()) {
                CubicSpline innerSpline = point.value();
                splineMin = Math.min(splineMin, innerSpline.min());
                splineMax = Math.max(splineMax, innerSpline.max());
            }

            for (int i = 0; i < lastIdx; ++i) {
                Point pointLeft = this.points.get(i);
                Point pointRight = this.points.get(i + 1);
                double locationLeft = pointLeft.location();
                double locationRight = pointRight.location();
                double locationDelta = locationRight - locationLeft;
                CubicSpline splineLeft = pointLeft.value();
                CubicSpline splineRight = pointRight.value();
                double minLeft = splineLeft.min();
                double maxLeft = splineLeft.max();
                double minRight = splineRight.min();
                double maxRight = splineRight.max();
                double derivativeLeft = pointLeft.derivative();
                double derivativeRight = pointRight.derivative();
                if (derivativeLeft != 0.0 || derivativeRight != 0.0) {
                    double maxValueDeltaLeft = derivativeLeft * locationDelta;
                    double maxValueDeltaRight = derivativeRight * locationDelta;
                    double minValue = Math.min(minLeft, minRight);
                    double maxValue = Math.max(maxLeft, maxRight);
                    double minDeltaLeft = maxValueDeltaLeft - maxRight + minLeft;
                    double maxDeltaLeft = maxValueDeltaLeft - minRight + maxLeft;
                    double minDeltaRight = -maxValueDeltaRight + minRight - maxLeft;
                    double maxDeltaRight = -maxValueDeltaRight + maxRight - minLeft;
                    double minDelta = Math.min(minDeltaLeft, minDeltaRight);
                    double maxDelta = Math.max(maxDeltaLeft, maxDeltaRight);
                    splineMin = Math.min(splineMin, minValue + 0.25 * minDelta);
                    splineMax = Math.max(splineMax, maxValue + 0.25 * maxDelta);
                }
            }

            CachedMinMax cachedMinMax = new CachedMinMax(splineMin, splineMax);
            cache.put(this, cachedMinMax);
            return cachedMinMax;
        }

        @Override
        public double min() {
            return minMax().min();
        }

        @Override
        public double max() {
            return minMax().max();
        }

        public record Point(double location, CubicSpline value, double derivative) {
        }

        private static double linearExtend(double location, Point point, double value) {
            double derivative = point.derivative();
            return derivative == 0.0 ? value : value + derivative * (location - point.location());
        }
    }
}
