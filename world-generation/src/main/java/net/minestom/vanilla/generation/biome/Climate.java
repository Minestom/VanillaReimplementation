package net.minestom.vanilla.generation.biome;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.vanilla.generation.densityfunctions.DensityFunction;
import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.noise.NoiseRouter;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Climate {
    static final int PARAMETER_SPACE = 7;

    public static Result target(double temperature, double humidity, double continentalness, double erosion, double depth, double weirdness) {
        return new Result(temperature, humidity, continentalness, erosion, depth, weirdness);
    }

    public static RangePoint parameters(double temperature, double humidity, double continentalness, double erosion, double depth, double weirdness, double offset) {
        return new RangePoint(param(temperature), param(humidity), param(continentalness), param(erosion), param(depth), param(weirdness), offset);
    }

    public static Range param(double min, double max) {
        return new Range(min, max);
    }

    public static Range param(double value) {
        return new Range(value, value);
    }

    public static Range param(Range value) {
        return value;
    }

    public static Range param(Object value) {
        if (value instanceof Range) {
            return (Range) value;
        }
        if (value instanceof Number) {
            return new Range(((Number) value).doubleValue(), ((Number) value).doubleValue());
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to Param");
    }

    public record Range(double min, double max) {
        public double distance(Range range) {
            double diffMax = range.min() - this.max();
            double diffMin = this.min() - range.max();
            if (diffMax > 0) {
                return diffMax;
            }
            return Math.max(diffMin, 0);
        }

        public Range union(Range range) {
            return new Range(
                    Math.min(this.min(), range.min()),
                    Math.max(this.max(), range.max())
            );
        }

        public static Range fromJson(Object obj) {
            if (obj instanceof JsonElement json) {
                if (json.isJsonPrimitive()) {
                    return fromJson(json.getAsDouble());
                }
                if (json.isJsonArray()) {
                    double[] array = StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                            .mapToDouble(JsonElement::getAsDouble)
                            .toArray();
                    return new Range(array[0], array[1]);
                }
            }
            if (obj instanceof Number) {
                return new Range(((Number) obj).doubleValue(), ((Number) obj).doubleValue());
            }
            throw new IllegalArgumentException("Cannot convert " + obj + " to Param");
        }
    }

    public record RangePoint(Range temperature, Range humidity, Range continentalness, Range erosion, Range depth,
                             Range weirdness, double offset) {
        public double fitness(RangePoint point) {
            return Util.square(this.temperature().distance(point.temperature()))
                    + Util.square(this.humidity().distance(point.humidity()))
                    + Util.square(this.continentalness().distance(point.continentalness()))
                    + Util.square(this.erosion().distance(point.erosion()))
                    + Util.square(this.depth().distance(point.depth()))
                    + Util.square(this.weirdness().distance(point.weirdness()))
                    + Util.square(this.offset() - point.offset());
        }


        public List<Range> space() {
            return List.of(this.temperature(), this.humidity(), this.continentalness(), this.erosion(), this.depth(),
                    this.weirdness(), new Range(this.offset(), this.offset()));
        }

        //    public static fromJson(obj: unknown) {
//			const root = Json.readObject(obj) ?? {}
//        return new ParamPoint(
//                Param.fromJson(root.temperature),
//                Param.fromJson(root.humidity),
//                Param.fromJson(root.continentalness),
//                Param.fromJson(root.erosion),
//                Param.fromJson(root.depth),
//                Param.fromJson(root.weirdness),
//                Json.readInt(root.offset) ?? 0,
//			)
//    }
        public static RangePoint fromJson(Object obj) {
            if (obj instanceof JsonElement json) {
                if (json.isJsonObject()) {
                    JsonObject root = json.getAsJsonObject();
                    return new RangePoint(
                            Range.fromJson(root.get("temperature")),
                            Range.fromJson(root.get("humidity")),
                            Range.fromJson(root.get("continentalness")),
                            Range.fromJson(root.get("erosion")),
                            Range.fromJson(root.get("depth")),
                            Range.fromJson(root.get("weirdness")),
                            root.get("offset").getAsInt()
                    );
                }
            }
            throw new IllegalArgumentException("Cannot convert " + obj + " to ParamPoint");
        }
    }


    public record Result(double temperature, double humidity, double continentalness, double erosion, double depth,
                         double weirdness) {

        public double offset() {
            return 0;
        }

        public double[] toArray() {
            return new double[]{this.temperature(), this.humidity(), this.continentalness(), this.erosion(), this.depth(), this.weirdness(), this.offset()};
        }
    }


    public static class Parameters<T> {
        private final RTree<T> index;

        public Parameters(Map<RangePoint, Supplier<T>> things) {
            this.index = new RTree<>(things);
        }

        public T find(Result target) {
            return this.index.search(target, RNode::distance);
        }
    }

    public record Sampler(DensityFunction temperature, DensityFunction humidity, DensityFunction continentalness,
                          DensityFunction erosion, DensityFunction depth, DensityFunction weirdness) {
        public static Sampler fromRouter(NoiseRouter router) {
            return new Sampler(router.temperature(), router.vegetation(), router.continents(), router.erosion(), router.depth(), router.ridges());
        }

        public Result sample(int x, int y, int z) {
            DensityFunction.Context context = DensityFunction.context(x << 2, y << 2, z << 2);
            return Climate.target(this.temperature().compute(context), this.humidity().compute(context), this.continentalness().compute(context), this.erosion().compute(context), this.depth().compute(context), this.weirdness().compute(context));
        }
    }

    //    type DistanceMetric<T> = (node: RNode<T>, values: number[]) => number
    interface DistanceMetric<T> {
        double distance(RNode<T> node, double[] values);
    }


    public static class RTree<T> {

        private static final int CHILDREN_PER_NODE = 10;
        private final RNode<T> root;

        public RTree(Map<RangePoint, Supplier<T>> points) {
            if (points.isEmpty()) {
                throw new IllegalArgumentException("At least one point is required to build search tree");
            }
            var pointList = points.entrySet()
                    .stream()
                    .map(entry -> new RLeaf<>(entry.getKey(),
                            entry.getValue()))
                    .map(leaf -> (RNode<T>) leaf)
                    .toList();
            this.root = RTree.build(pointList);
        }

        private static <T> RNode<T> build(List<RNode<T>> nodes) {
            if (nodes.size() == 1) {
                return nodes.get(0);
            }
            if (nodes.size() <= RTree.CHILDREN_PER_NODE) {
                List<RNode<T>> sortedNodes = nodes.stream()
                        .map(node -> {
                            double key = 0.0;
                            for (int i = 0; i < PARAMETER_SPACE; i += 1) {
                                Range range = node.space.get(i);
                                key += Math.abs((range.min() + range.max()) / 2.0);
                            }
                            return Map.entry(key, node);
                        })
                        .sorted(Comparator.comparingDouble(Map.Entry::getKey))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());
                return new RSubTree<>(sortedNodes);
            }
            double f = Double.POSITIVE_INFINITY;
            int n3 = -1;
            List<RSubTree<T>> result = new ArrayList<>();
            for (int n2 = 0; n2 < PARAMETER_SPACE; ++n2) {
                nodes = RTree.sort(nodes, n2, false);
                result = RTree.bucketize(nodes);
                double f2 = 0.0;
                for (RSubTree<T> subTree2 : result) {
                    f2 += RTree.area(subTree2.space());
                }
                if (!(f > f2)) continue;
                f = f2;
                n3 = n2;
            }
            nodes = RTree.sort(nodes, n3, false);
            result = RTree.bucketize(nodes);
            result = RTree.sort(result, n3, true);
            return new RSubTree<>(result.stream().map(subTree -> RTree.build(subTree.children)).collect(Collectors.toList()));
        }

        private static <N extends RNode<?>> List<N> sort(List<N> nodes, int i, boolean abs) {
            return nodes.stream()
                    .map(node -> {
                        Range range = node.space().get(i);
                        double f = (range.min() + range.max()) / 2;
                        double key = abs ? Math.abs(f) : f;
                        return Map.entry(key, node);
                    })
                    .sorted(Comparator.comparingDouble(Map.Entry::getKey))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        }

        private static <T> List<RSubTree<T>> bucketize(List<RNode<T>> nodes) {
            List<RSubTree<T>> arrayList = new ArrayList<>();
            List<RNode<T>> arrayList2 = new ArrayList<>();
            double n = Math.pow(10.0, Math.floor(Math.log(nodes.size() - 0.01) / Math.log(10.0)));
            for (RNode<T> node : nodes) {
                arrayList2.add(node);
                if (arrayList2.size() < n) continue;
                arrayList.add(new RSubTree<>(arrayList2));
                arrayList2 = new ArrayList<>();
            }
            if (arrayList2.size() != 0) {
                arrayList.add(new RSubTree<>(arrayList2));
            }
            return arrayList;
        }

        private static double area(Collection<Range> ranges) {
            double f = 0.0;
            for (Range range : ranges) {
                f += Math.abs(range.max() - range.min());
            }
            return f;
        }

        public T search(Result target, DistanceMetric<T> distance) {
            RLeaf<T> leaf = this.root.search(target.toArray(), distance);
            return leaf.thing.get();
        }
    }

    static abstract class RNode<T> {
        protected final List<Range> space;

        public RNode(List<Range> space) {
            this.space = space;
        }

        public abstract RLeaf<T> search(double[] values, DistanceMetric<T> distance);

        public double distance(double[] values) {
            double result = 0;
            for (int i = 0; i < PARAMETER_SPACE; i += 1) {
                result += Util.square(this.space.get(i).distance(Range.fromJson(values[i])));
            }
            return result;
        }

        public List<Range> space() {
            return this.space;
        }
    }

    static class RSubTree<T> extends RNode<T> {
        private final List<RNode<T>> children;

        public RSubTree(List<RNode<T>> children) {
            super(RSubTree.buildSpace(children));
            this.children = children;
        }

        private static <T> List<Range> buildSpace(List<RNode<T>> nodes) {
            List<Range> space = new ArrayList<>(PARAMETER_SPACE);
            for (int i = 0; i < PARAMETER_SPACE; i += 1) {
                space.set(i, new Range(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
            }
            for (RNode<T> node : nodes) {
                for (int i = 0; i < PARAMETER_SPACE; i += 1) {
                    space.set(i, space.get(i).union(node.space().get(i)));
                }
            }
            return space;
        }

        @Override
        public RLeaf<T> search(double[] values, DistanceMetric<T> distance) {
            double dist = Double.POSITIVE_INFINITY;
            RLeaf<T> leaf = null;
            for (RNode<T> node : this.children) {
                double d1 = distance.distance(node, values);
                if (dist <= d1) continue;
                RLeaf<T> leaf2 = node.search(values, distance);
                double d2 = node == leaf2 ? d1 : distance.distance(leaf2, values);
                if (dist <= d2) continue;
                dist = d2;
                leaf = leaf2;
            }
            return leaf;
        }
    }

    static class RLeaf<T> extends RNode<T> {
        private final Supplier<T> thing;

        public RLeaf(RangePoint point, Supplier<T> thing) {
            super(point.space());
            this.thing = thing;
        }

        @Override
        public RLeaf<T> search(double[] values, DistanceMetric<T> distance) {
            return this;
        }
    }
}