package net.minestom.vanilla.generation.math;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.vanilla.generation.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

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

    CubicSpline<C> mapAll(CoordinateVisitor<C> visitor);

    void calculateMinMax();

    interface CoordinateVisitor<C> {
        NumberFunction<C> visit(NumberFunction<C> function);
    }

    record SplinePoint(double location, CubicSpline<?> value, double derivative) {

        public static <C> List<SplinePoint> fromJsonArray(JsonElement element, Function<Object, NumberFunction<C>> extractor) {
            JsonArray array = Util.jsonArray(element);
            return StreamSupport.stream(array.spliterator(), false)
                    .map(Util::jsonObject)
                    .map(json -> {
                        double location = json.get("location").getAsDouble();
                        double derivative = json.get("derivative").getAsDouble();
                        JsonElement value = json.get("value");

                        if (value.isJsonObject()) {
                            return new SplinePoint(location, CubicSpline.fromJson(value, extractor), derivative);
                        }

                        return new SplinePoint(location, new CubicSpline.Constant<>(value.getAsDouble()), derivative);
                    })
                    .toList();
        }
    }

    static <C> CubicSpline<C> fromJson(Object obj, Function<Object, NumberFunction<C>> extractor) {
        if (obj instanceof Number number) {
            return new Constant<>(number.doubleValue());
        }
        JsonObject root = Util.jsonObject(obj);

        MultiPoint<C> spline = new MultiPoint<>(extractor.apply(Util.jsonRequire(root, "coordinate", Function.identity())));
        @NotNull List<SplinePoint> points = Util.jsonRequire(root, "points",
                element -> SplinePoint.fromJsonArray(element, extractor));
        if (points.size() == 0) {
            return new Constant<>(0);
        }
        for (var point : points) {
            spline.addPoint(point);
        }
        return spline;
    }

//    export class Constant implements CubicSpline<unknown> {
//        constructor(private readonly value: number) {}
//        public compute() {
//            return this.value
//        }
//        public min() {
//            return this.value
//        }
//        public max() {
//            return this.value
//        }
//        public mapAll() {
//            return this
//        }
//
//        public calculateMinMax() {}
//    }

    class Constant<C> implements CubicSpline<C> {
        private final double value;

        public Constant(double value) {
            this.value = value;
        }

        @Override
        public double compute(C coordinate) {
            return value;
        }

        @Override
        public double min() {
            return value;
        }

        @Override
        public double max() {
            return value;
        }

        @Override
        public CubicSpline<C> mapAll(CoordinateVisitor<C> visitor) {
            return this;
        }

        @Override
        public void calculateMinMax() {
        }
    }

    //    class MultiPoint<C> implements CubicSpline<C> {
//        private calculatedMin = Number.NEGATIVE_INFINITY
//        private calculatedMax = Number.POSITIVE_INFINITY
    class MultiPoint<C> implements CubicSpline<C> {
        private double calculatedMin = Double.NEGATIVE_INFINITY;
        private double calculatedMax = Double.POSITIVE_INFINITY;
        //
        //        constructor(
        //                        public coordinate: NumberFunction<C>,
        //                        public locations: number[] = [],
        //                        public values: CubicSpline<C>[] = [],
        //                        public derivatives: number[] = [],
        //                        ) {}

        public final NumberFunction<C> coordinate;
        public double[] locations;
        public CubicSpline<C>[] values;
        public double[] derivatives;

        public MultiPoint(NumberFunction<C> coordinate) {
            this.coordinate = coordinate;
            this.locations = new double[0];
            //noinspection unchecked
            this.values = new CubicSpline[0];
            this.derivatives = new double[0];
        }

        public MultiPoint(NumberFunction<C> coordinate, double[] locations, CubicSpline<C>[] values, double[] derivatives) {
            this.coordinate = coordinate;
            this.locations = locations;
            this.values = values;
            this.derivatives = derivatives;
        }

        //        public compute(c: C) {
        //			const coordinate = this.coordinate.compute(c)
        //			const i = binarySearch(0, this.locations.length, n => coordinate < this.locations[n]) - 1
        //			const n = this.locations.length - 1
        //            if (i < 0) {
        //                return this.values[0].compute(c) + this.derivatives[0] * (coordinate - this.locations[0])  //TODO: us linear extend for this
        //            }
        //            if (i === n) {
        //                return this.values[n].compute(c) + this.derivatives[n] * (coordinate - this.locations[n])  //TODO: us linear extend for this
        //            }
        //			const loc0 = this.locations[i]
        //			const loc1 = this.locations[i + 1]
        //			const der0 = this.derivatives[i]
        //			const der1 = this.derivatives[i + 1]
        //			const f = (coordinate - loc0) / (loc1 - loc0)
        //
        //			const val0 = this.values[i].compute(c)
        //			const val1 = this.values[i + 1].compute(c)
        //
        //			const f8 = der0 * (loc1 - loc0) - (val1 - val0)
        //			const f9 = -der1 * (loc1 - loc0) + (val1 - val0)
        //			const f10 = lerp(f, val0, val1) + f * (1.0 - f) * lerp(f, f8, f9)
        //            return f10
        //        }

        @Override
        public double compute(C coordinate) {
            double c = this.coordinate.compute(coordinate);
            int i = Util.binarySearch(0, this.locations.length, n -> c < this.locations[n]) - 1;
            int n = this.locations.length - 1;
            if (i < 0) {
                return this.values[0].compute(coordinate) + this.derivatives[0] * (c - this.locations[0]);  //TODO: us linear extend for this
            }
            if (i == n) {
                return this.values[n].compute(coordinate) + this.derivatives[n] * (c - this.locations[n]);  //TODO: us linear extend for this
            }
            double loc0 = this.locations[i];
            double loc1 = this.locations[i + 1];
            double der0 = this.derivatives[i];
            double der1 = this.derivatives[i + 1];
            double f = (c - loc0) / (loc1 - loc0);

            double val0 = this.values[i].compute(coordinate);
            double val1 = this.values[i + 1].compute(coordinate);

            double f8 = der0 * (loc1 - loc0) - (val1 - val0);
            double f9 = -der1 * (loc1 - loc0) + (val1 - val0);
            return Util.lerp(f, val0, val1) + f * (1.0 - f) * Util.lerp(f, f8, f9);
        }
        //
        //        public min() {
        //            return this.calculatedMin
        //        }
        //
        //        public max() {
        //            return this.calculatedMax
        //        }

        @Override
        public double min() {
            return this.calculatedMin;
        }

        @Override
        public double max() {
            return this.calculatedMax;
        }

        //        public mapAll(visitor: CubicSpline.CoordinateVisitor<C>): CubicSpline<C> {
        //            return new MultiPoint(visitor(this.coordinate), this.locations, this.values.map(v => v.mapAll(visitor)), this.derivatives)
        //        }

        @Override
        public CubicSpline<C> mapAll(CoordinateVisitor<C> visitor) {
            //noinspection unchecked
            return new MultiPoint<>(visitor.visit(this.coordinate), this.locations,
                    Arrays.stream(this.values).map(v -> v.mapAll(visitor)).toArray(CubicSpline[]::new), this.derivatives);
        }
        //        public addPoint(location: number, value: number | CubicSpline<C>, derivative = 0) {
        //            this.locations.push(location)
        //            this.values.push(typeof value === 'number'
        //                    ? new CubicSpline.Constant(value)
        //                    : value)
        //            this.derivatives.push(derivative)
        //            return this
        //        }

        public void addPoint(double location, CubicSpline<C> value, double derivative) {
            this.locations = Arrays.copyOf(this.locations, this.locations.length + 1);
            this.locations[this.locations.length - 1] = location;
            this.values = Arrays.copyOf(this.values, this.values.length + 1);
            this.values[this.values.length - 1] = value;
            this.derivatives = Arrays.copyOf(this.derivatives, this.derivatives.length + 1);
            this.derivatives[this.derivatives.length - 1] = derivative;
        }

        public void addPoint(SplinePoint point) {
            //noinspection unchecked
            this.addPoint(point.location, (CubicSpline<C>) point.value, point.derivative);
        }
        //
        //        public calculateMinMax() {
        //            if (!MinMaxNumberFunction.is(this.coordinate)) {
        //                return
        //            }
        //
        //			const lastIdx = this.locations.length - 1
        //            var splineMin = Number.POSITIVE_INFINITY
        //            var splineMax = Number.NEGATIVE_INFINITY
        //			const coordinateMin = this.coordinate.minValue()
        //			const coordinateMax = this.coordinate.maxValue()
        //
        //            for(const innerSpline of this.values) {
        //                innerSpline.calculateMinMax()
        //            }

        public void calculateMinMax() {
            if (!MinMaxNumberFunction.is(this.coordinate)) {
                return;
            }

            MinMaxNumberFunction<C> coordinate = (MinMaxNumberFunction<C>) this.coordinate;

            int lastIdx = this.locations.length - 1;
            double splineMin = Double.POSITIVE_INFINITY;
            double splineMax = Double.NEGATIVE_INFINITY;
            double coordinateMin = coordinate.min();
            double coordinateMax = coordinate.max();

            for (CubicSpline<C> innerSpline : this.values) {
                innerSpline.calculateMinMax();
            }

            //
            //            if (coordinateMin < this.locations[0]) {
            //				const minExtend = MultiPoint.linearExtend(coordinateMin, this.locations, (this.values[0]).min(), this.derivatives, 0)
            //				const maxExtend = MultiPoint.linearExtend(coordinateMin, this.locations, (this.values[0]).max(), this.derivatives, 0)
            //                splineMin = Math.min(splineMin, Math.min(minExtend, maxExtend))
            //                splineMax = Math.max(splineMax, Math.max(minExtend, maxExtend))
            //            }
            //
            //            if (coordinateMax > this.locations[lastIdx]) {
            //				const minExtend = MultiPoint.linearExtend(coordinateMax, this.locations, (this.values[lastIdx]).min(), this.derivatives, lastIdx)
            //				const maxExtend = MultiPoint.linearExtend(coordinateMax, this.locations, (this.values[lastIdx]).max(), this.derivatives, lastIdx)
            //                splineMin = Math.min(splineMin, Math.min(minExtend, maxExtend))
            //                splineMax = Math.max(splineMax, Math.max(minExtend, maxExtend))
            //            }
            //
            //            for (const innerSpline of this.values) {
            //                splineMin = Math.min(splineMin, innerSpline.min())
            //                splineMax = Math.max(splineMax, innerSpline.max())
            //            }

            if (coordinateMin < this.locations[0]) {
                double minExtend = MultiPoint.linearExtend(coordinateMin, this.locations, this.values[0].min(), this.derivatives, 0);
                double maxExtend = MultiPoint.linearExtend(coordinateMin, this.locations, this.values[0].max(), this.derivatives, 0);
                splineMin = Math.min(splineMin, Math.min(minExtend, maxExtend));
                splineMax = Math.max(splineMax, Math.max(minExtend, maxExtend));
            }

            if (coordinateMax > this.locations[lastIdx]) {
                double minExtend = MultiPoint.linearExtend(coordinateMax, this.locations, this.values[lastIdx].min(), this.derivatives, lastIdx);
                double maxExtend = MultiPoint.linearExtend(coordinateMax, this.locations, this.values[lastIdx].max(), this.derivatives, lastIdx);
                splineMin = Math.min(splineMin, Math.min(minExtend, maxExtend));
                splineMax = Math.max(splineMax, Math.max(minExtend, maxExtend));
            }

            for (CubicSpline<C> innerSpline : this.values) {
                splineMin = Math.min(splineMin, innerSpline.min());
                splineMax = Math.max(splineMax, innerSpline.max());
            }
            //
            //            for (var i = 0; i < lastIdx; ++i) {
            //				const locationLeft = this.locations[i]
            //				const locationRight = this.locations[i + 1]
            //				const locationDelta = locationRight - locationLeft
            //				const splineLeft = this.values[i]
            //				const splineRight = this.values[i + 1]
            //				const minLeft = splineLeft.min()
            //				const maxLeft = splineLeft.max()
            //				const minRight = splineRight.min()
            //				const maxRight = splineRight.max()
            //				const derivativeLeft = this.derivatives[i]
            //				const derivativeRight = this.derivatives[i + 1]
            //                if (derivativeLeft !== 0.0 || derivativeRight !== 0.0) {
            //					const maxValueDeltaLeft = derivativeLeft * locationDelta
            //					const maxValueDeltaRight = derivativeRight * locationDelta
            //					const minValue = Math.min(minLeft, minRight)
            //					const maxValue = Math.max(maxLeft, maxRight)
            //					const minDeltaLeft = maxValueDeltaLeft - maxRight + minLeft
            //					const maxDeltaLeft = maxValueDeltaLeft - minRight + maxLeft
            //					const minDeltaRight = -maxValueDeltaRight + minRight - maxLeft
            //					const maxDeltaRight = -maxValueDeltaRight + maxRight - minLeft
            //					const minDelta = Math.min(minDeltaLeft, minDeltaRight)
            //					const maxDelta = Math.max(maxDeltaLeft, maxDeltaRight)
            //                    splineMin = Math.min(splineMin, minValue + 0.25 * minDelta)
            //                    splineMax = Math.max(splineMax, maxValue + 0.25 * maxDelta)
            //                }
            //            }
            //
            //            this.calculatedMin = splineMin
            //            this.calculatedMax = splineMax
            //        }

            for (int i = 0; i < lastIdx; ++i) {
                double locationLeft = this.locations[i];
                double locationRight = this.locations[i + 1];
                double locationDelta = locationRight - locationLeft;
                CubicSpline<C> splineLeft = this.values[i];
                CubicSpline<C> splineRight = this.values[i + 1];
                double minLeft = splineLeft.min();
                double maxLeft = splineLeft.max();
                double minRight = splineRight.min();
                double maxRight = splineRight.max();
                double derivativeLeft = this.derivatives[i];
                double derivativeRight = this.derivatives[i + 1];
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

            this.calculatedMin = splineMin;
            this.calculatedMax = splineMax;
        }


        //
        //
        //        private static linearExtend(location: number, locations: number[], value: number, derivatives: number[], useIndex: number) {
        //			const derivative = derivatives[useIndex]
        //            return derivative == 0.0 ? value : value + derivative * (location - locations[useIndex])
        //        }
        //    }

        private static double linearExtend(double location, double[] locations, double value, double[] derivatives, int useIndex) {
            double derivative = derivatives[useIndex];
            return derivative == 0.0 ? value : value + derivative * (location - locations[useIndex]);
        }
    }
}
