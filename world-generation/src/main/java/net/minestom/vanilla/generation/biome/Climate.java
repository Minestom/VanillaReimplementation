package net.minestom.vanilla.generation.biome;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.densityfunctions.DensityFunction;
import net.minestom.vanilla.generation.noise.NoiseRouter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class Climate {
    static final int PARAMETER_SPACE = 7;

    public static TargetPoint target(double temperature, double humidity, double continentalness, double erosion, double depth, double weirdness) {
        return new TargetPoint(temperature, humidity, continentalness, erosion, depth, weirdness);
    }

    public static ParamPoint parameters(double temperature, double humidity, double continentalness, double erosion, double depth, double weirdness, double offset) {
        return new ParamPoint(param(temperature), param(humidity), param(continentalness), param(erosion), param(depth), param(weirdness), offset);
    }

    public static @NotNull Param param(double min, double max) {
        return new Param(min, max);
    }

    public static @NotNull Param param(double value) {
        return new Param(value, value);
    }

    public static @NotNull Param param(@NotNull Param value) {
        return value;
    }

    public static @NotNull Param param(Object value) {
        if (value instanceof Param param) {
            return param;
        }
        if (value instanceof Number number) {
            return param(number.doubleValue());
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to Param");
    }

    public record Param(double min, double max) {
        public double distance(@NotNull Param other) {
            double diffMax = other.min() - this.max();
            double diffMin = this.min() - other.max();
            if (diffMax > 0) {
                return diffMax;
            }
            return Math.max(diffMin, 0);
        }

        public @NotNull Param union(@NotNull Param other) {
            return new Param(
                    Math.min(this.min(), other.min()),
                    Math.max(this.max(), other.max())
            );
        }

        public static @NotNull Param fromJson(Object obj) {
            if (obj instanceof JsonElement json) {
                if (json.isJsonPrimitive()) {
                    return fromJson(json.getAsDouble());
                }
                if (json.isJsonArray()) {
                    double[] array = StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                            .mapToDouble(JsonElement::getAsDouble)
                            .toArray();
                    return new Param(array[0], array[1]);
                }
            } else if (obj instanceof Number number) {
                return param(number.doubleValue());
            }
            throw new IllegalArgumentException("Cannot convert " + obj + " to Param");
        }
    }

    public record ParamPoint(@NotNull Param temperature, @NotNull Param humidity, @NotNull Param continentalness,
                             @NotNull Param erosion, @NotNull Param depth, @NotNull Param weirdness, double offset) {

        public double fittness(@NotNull ParamPoint other) {
            return Util.square(this.temperature().distance(other.temperature()))
                    + Util.square(this.humidity().distance(other.humidity()))
                    + Util.square(this.continentalness().distance(other.continentalness()))
                    + Util.square(this.erosion().distance(other.erosion()))
                    + Util.square(this.depth().distance(other.depth()))
                    + Util.square(this.weirdness().distance(other.weirdness()))
                    + Util.square(this.offset() - other.offset());
        }

        public @NotNull List<@NotNull Param> space() {
            return List.of(
                    temperature(),
                    humidity(),
                    continentalness(),
                    erosion(),
                    depth(),
                    weirdness(),
                    new Param(offset(), offset())
            );
        }

        public static @NotNull ParamPoint fromJson(Object obj) {
            if (!(obj instanceof JsonObject root)) {
                throw new IllegalArgumentException("Cannot convert " + obj + " to ParamPoint");
            }
            return new ParamPoint(
                    Param.fromJson(root.get("temperature")),
                    Param.fromJson(root.get("humidity")),
                    Param.fromJson(root.get("continentalness")),
                    Param.fromJson(root.get("erosion")),
                    Param.fromJson(root.get("depth")),
                    Param.fromJson(root.get("weirdness")),
                    root.get("offset").getAsInt()
            );
        }
    }

    public record TargetPoint(double temperature, double humidity, double continentalness,
                              double erosion, double depth, double weirdness) {

        public double offset() {
            return 0;
        }

        public double[] toArray() {
            return new double[]{this.temperature(), this.humidity(), this.continentalness(), this.erosion(), this.depth(), this.weirdness(), this.offset()};
        }
    }

    public static class Parameters<T> {
        private final RTree<T> index;

        public Parameters(Map<ParamPoint, Supplier<T>> things) {
            this.index = new RTree<>(things);
        }

        public T find(TargetPoint target) {
            return this.index.search(target, RNode::distance);
        }
    }

    public record Sampler(@NotNull DensityFunction temperature, @NotNull DensityFunction humidity,
                          @NotNull DensityFunction continentalness, @NotNull DensityFunction erosion,
                          @NotNull DensityFunction depth, @NotNull DensityFunction weirdness) {

        public static @NotNull Sampler fromRouter(@NotNull NoiseRouter router) {
            return new Sampler(router.temperature(), router.vegetation(), router.continents(), router.erosion(), router.depth(), router.ridges());
        }

        public @NotNull TargetPoint sample(int x, int y, int z) {
            Point point = new Vec(x << 2, y << 2, z << 2);
            return Climate.target(
                    this.temperature().compute(point),
                    this.humidity().compute(point),
                    this.continentalness().compute(point),
                    this.erosion().compute(point),
                    this.depth().compute(point),
                    this.weirdness().compute(point)
            );
        }
    }

    interface DistanceMetric<T> {
        double distance(@NotNull RNode<T> node, double[] values);
    }

    public static class RTree<T> {

        private static final int CHILDREN_PER_NODE = 10;
        private final RNode<T> root;

        public RTree(@NotNull Map<ParamPoint, Supplier<T>> points) {
            if (points.isEmpty()) {
                throw new IllegalArgumentException("At least one point is required to build search tree");
            }
            var pointList = points.entrySet()
                    .stream()
                    .map(entry -> new RLeaf<>(entry.getKey(), entry.getValue()))
                    .map(leaf -> (RNode<T>) leaf)
                    .toList();
            this.root = RTree.build(pointList);
        }

        private static <T> RNode<T> build(@NotNull List<RNode<T>> nodes) {
            if (nodes.size() == 1) {
                return nodes.get(0);
            }
            if (nodes.size() <= RTree.CHILDREN_PER_NODE) {
                List<RNode<T>> sortedNodes = nodes.stream()
                        .map(node -> {
                            double key = 0.0;
                            for (int i = 0; i < PARAMETER_SPACE; i += 1) {
                                Param param = node.space().get(i);
                                key += Math.abs((param.min() + param.max()) / 2.0);
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
            List<RSubTree<T>> result;
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
                        Param param = node.space().get(i);
                        double f = (param.min() + param.max()) / 2;
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

        private static double area(Collection<Param> params) {
            double f = 0.0;
            for (Param param : params) {
                f += Math.abs(param.max() - param.min());
            }
            return f;
        }

        public T search(TargetPoint target, DistanceMetric<T> distance) {
            RLeaf<T> leaf = this.root.search(target.toArray(), distance);
            return leaf.thing.get();
        }

    }

    interface RNode<T> {

        @NotNull List<Param> space();

        RLeaf<T> search(double[] values, DistanceMetric<T> distance);

        default double distance(double[] values) {
            var space = space();
            return IntStream
                    .range(0, PARAMETER_SPACE)
                    .mapToDouble(i -> Util.square(space.get(i).distance(Param.fromJson(values[i]))))
                    .sum();
        }

    }

    record RSubTree<T>(@NotNull List<RNode<T>> children,
                       @NotNull List<Param> space) implements RNode<T> {

        public RSubTree(@NotNull List<RNode<T>> children) {
            this(children, RSubTree.buildSpace(children));
        }

        private static <T> List<Param> buildSpace(List<RNode<T>> nodes) {
            Param[] space = new Param[PARAMETER_SPACE];
            for (int i = 0; i < PARAMETER_SPACE; i += 1) {
                space[i] = new Param(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
            }
            for (RNode<T> node : nodes) {
                for (int i = 0; i < PARAMETER_SPACE; i += 1) {
                    space[i] = space[i].union(node.space().get(i));
                }
            }
            return Arrays.asList(space);
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

    record RLeaf<T>(@NotNull List<Param> space,
                    @NotNull Supplier<T> thing) implements RNode<T> {
        public RLeaf(ParamPoint point, Supplier<T> thing) {
            this(point.space(), thing);
        }

        @Override
        public RLeaf<T> search(double[] values, DistanceMetric<T> distance) {
            return this;
        }
    }
}