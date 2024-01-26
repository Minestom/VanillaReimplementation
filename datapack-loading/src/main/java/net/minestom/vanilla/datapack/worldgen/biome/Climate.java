package net.minestom.vanilla.datapack.worldgen.biome;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.vanilla.datapack.worldgen.DensityFunction;
import net.minestom.vanilla.datapack.worldgen.NoiseSettings;
import net.minestom.vanilla.datapack.worldgen.util.Util;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Climate {
    static final int PARAMETER_SPACE = 7;

    //        export function target(temperature: number, humidity: number, continentalness: number, erosion: number, depth: number, weirdness: number) {
//        return new TargetPoint(temperature, humidity, continentalness, erosion, depth, weirdness)
//        }
    public static TargetPoint target(double temperature, double humidity, double continentalness, double erosion, double depth, double weirdness) {
        return new TargetPoint(temperature, humidity, continentalness, erosion, depth, weirdness);
    }

    //        export function parameters(temperature: number | Param, humidity: number | Param, continentalness: number | Param, erosion: number | Param, depth: number | Param, weirdness: number | Param, offset: number) {
//            return new ParamPoint(param(temperature), param(humidity), param(continentalness), param(erosion), param(depth), param(weirdness), offset)
//        }
    public static ParamPoint parameters(double temperature, double humidity, double continentalness, double erosion, double depth, double weirdness, double offset) {
        return new ParamPoint(param(temperature), param(humidity), param(continentalness), param(erosion), param(depth), param(weirdness), offset);
    }

    //        export function param(value: number | Param, max?: number) {
//        if (typeof value === 'number') {
//        return new Param(value, max ?? value)
//        }
//        return value
//        }
    public static Param param(double min, double max) {
        return new Param(min, max);
    }

    public static Param param(double value) {
        return new Param(value, value);
    }

    public static Param param(Param value) {
        return value;
    }

    public static Param param(Object value) {
        if (value instanceof Param) {
            return (Param) value;
        }
        if (value instanceof Number) {
            return new Param(((Number) value).doubleValue(), ((Number) value).doubleValue());
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to Param");
    }

    //        export class Param {
//    constructor(
//            public readonly min: number,
//            public readonly max: number,
//            ) {}
    public record Param(double min, double max) {
        //    public distance(param: Param | number) {
//			const diffMax = (typeof param === 'number' ? param : param.min) - this.max
//			const diffMin = this.min - (typeof param === 'number' ? param : param.max)
//        if (diffMax > 0) {
//            return diffMax
//        }
//        return Math.max(diffMin, 0)
//    }
        public double distance(Param param) {
            double diffMax = param.min() - this.max();
            double diffMin = this.min() - param.max();
            if (diffMax > 0) {
                return diffMax;
            }
            return Math.max(diffMin, 0);
        }

        //    public union(param: Param) {
//        return new Param(
//                Math.min(this.min, param.min),
//                Math.max(this.max, param.max)
//        )
//    }
        public Param union(Param param) {
            return new Param(
                    Math.min(this.min(), param.min()),
                    Math.max(this.max(), param.max())
            );
        }

        //    public static fromJson(obj: unknown) {
//        if (typeof obj === 'number') return new Param(obj, obj)
//			const [min, max] = Json.readArray(obj, e => Json.readNumber(e)) ?? []
//        return new Param(min ?? 0, max ?? 0)
//    }
        public static Param fromJson(Object obj) {
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
            }
            if (obj instanceof Number) {
                return new Param(((Number) obj).doubleValue(), ((Number) obj).doubleValue());
            }
            throw new IllegalArgumentException("Cannot convert " + obj + " to Param");
        }
    }

    //	export class ParamPoint {
//    constructor(
//            public readonly temperature: Param,
//            public readonly humidity: Param,
//            public readonly continentalness: Param,
//            public readonly erosion: Param,
//            public readonly depth: Param,
//            public readonly weirdness: Param,
//            public readonly offset: number,
//            ) {}
    public record ParamPoint(Param temperature, Param humidity, Param continentalness, Param erosion, Param depth,
                             Param weirdness, double offset) {
        public double fittness(ParamPoint point) {
            return Util.square(this.temperature().distance(point.temperature()))
                    + Util.square(this.humidity().distance(point.humidity()))
                    + Util.square(this.continentalness().distance(point.continentalness()))
                    + Util.square(this.erosion().distance(point.erosion()))
                    + Util.square(this.depth().distance(point.depth()))
                    + Util.square(this.weirdness().distance(point.weirdness()))
                    + Util.square(this.offset() - point.offset());
        }


//    public fittness(point: ParamPoint | TargetPoint) {
//        return square(this.temperature.distance(point.temperature))
//                + square(this.humidity.distance(point.humidity))
//                + square(this.continentalness.distance(point.continentalness))
//                + square(this.erosion.distance(point.erosion))
//                + square(this.depth.distance(point.depth))
//                + square(this.weirdness.distance(point.weirdness))
//                + square(this.offset - point.offset)
//    }


        //    public space() {
//        return [this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, new Param(this.offset, this.offset)]
//    }
        public Param[] space() {
            return new Param[]{this.temperature(), this.humidity(), this.continentalness(), this.erosion(), this.depth(), this.weirdness(), new Param(this.offset(), this.offset())};
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
        public static ParamPoint fromJson(Object obj) {
            if (obj instanceof JsonElement json) {
                if (json.isJsonObject()) {
                    JsonObject root = json.getAsJsonObject();
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
            throw new IllegalArgumentException("Cannot convert " + obj + " to ParamPoint");
        }
    }


    //	export class TargetPoint {
//    constructor(
//            public readonly temperature: number,
//            public readonly humidity: number,
//            public readonly continentalness: number,
//            public readonly erosion: number,
//            public readonly depth: number,
//            public readonly weirdness: number,
//            ) {}
    public record TargetPoint(double temperature, double humidity, double continentalness, double erosion, double depth,
                              double weirdness) {


        //    get offset() {
//        return 0
//    }
        public double offset() {
            return 0;
        }

        //    public toArray() {
//        return [this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, this.offset]
//    }
        public double[] toArray() {
            return new double[]{this.temperature(), this.humidity(), this.continentalness(), this.erosion(), this.depth(), this.weirdness(), this.offset()};
        }
    }

//export class Parameters<T> {
//    private readonly index: RTree<T>
//
//    constructor(public readonly things: [ParamPoint, () => T][]) {
//        this.index = new RTree(things)
//    }
//
//    public find(target: TargetPoint) {
//        return this.index.search(target, (node, values) => node.distance(values))
//    }
//}

    public static class Parameters<T> {
        private final RTree<T> index;

        public Parameters(Map<ParamPoint, Supplier<T>> things) {
            this.index = new RTree<>(things);
        }

        public T find(TargetPoint target) {
            return this.index.search(target, RNode::distance);
        }
    }

    //	export class Sampler {
//    constructor(
//            public readonly temperature: DensityFunction,
//            public readonly humidity: DensityFunction,
//            public readonly continentalness: DensityFunction,
//            public readonly erosion: DensityFunction,
//            public readonly depth: DensityFunction,
//            public readonly weirdness: DensityFunction,
//            ) {}
//
//    public static fromRouter(router: NoiseRouter) {
//        return new Climate.Sampler(router.temperature, router.vegetation, router.continents, router.erosion, router.depth, router.ridges)
//    }
//
//    sample(x: number, y: number, z: number) {
//			const context = DensityFunction.context(x << 2, y << 2, z << 2)
//        return Climate.target(this.temperature.compute(context), this.humidity.compute(context), this.continentalness.compute(context), this.erosion.compute(context), this.depth.compute(context), this.weirdness.compute(context))
//    }
//}
    public record Sampler(DensityFunction temperature, DensityFunction humidity, DensityFunction continentalness,
                          DensityFunction erosion, DensityFunction depth, DensityFunction weirdness) {
        public static Sampler fromRouter(NoiseSettings.NoiseRouter router) {
            return new Sampler(router.temperature(), router.vegetation(), router.continents(), router.erosion(), router.depth(), router.ridges());
        }

        public TargetPoint sample(int x, int y, int z) {
            DensityFunction.Context context = DensityFunction.context(x << 2, y << 2, z << 2);
            return Climate.target(this.temperature().compute(context), this.humidity().compute(context), this.continentalness().compute(context), this.erosion().compute(context), this.depth().compute(context), this.weirdness().compute(context));
        }
    }

    //    type DistanceMetric<T> = (node: RNode<T>, values: number[]) => number
    interface DistanceMetric<T> {
        double distance(RNode<T> node, double[] values);
    }

//export class RTree<T> {

    public static class RTree<T> {

//    public static readonly CHILDREN_PER_NODE = 10
//    private readonly root: RNode<T>

        private static final int CHILDREN_PER_NODE = 10;
        private final RNode<T> root;
//
//    constructor(points: [ParamPoint, () => T][]) {
//        if (points.length === 0) {
//            throw new Error('At least one point is required to build search tree')
//        }
//        this.root = RTree.build(points.map(([point, thing]) => new RLeaf(point, thing)))
//    }

        public RTree(Map<ParamPoint, Supplier<T>> points) {
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

//
//		private static build<T>(nodes: RNode<T>[]): RNode<T> {
//        if (nodes.length === 1) {
//            return nodes[0]
//        }
//        if (nodes.length <= RTree.CHILDREN_PER_NODE) {
//				const sortedNodes = nodes
//                    .map(node => {
//                    let key = 0.0
//            for (let i = 0; i < PARAMETER_SPACE; i += 1) {
//							const param = node.space[i]
//                key += Math.abs((param.min + param.max) / 2.0)
//            }
//            return { key, node }
//					})
//					.sort((a, b) => a.key - b.key)
//					.map(({ node }) => node)
//            return new RSubTree(sortedNodes)
//        }
//        let f = Infinity
//        let n3 = -1
//        let result: RSubTree<T>[] = []
//        for (let n2 = 0; n2 < PARAMETER_SPACE; ++n2) {
//            nodes = RTree.sort(nodes, n2, false)
//            result = RTree.bucketize(nodes)
//            let f2 = 0.0
//            for (const subTree2 of result) {
//                f2 += RTree.area(subTree2.space)
//            }
//            if (!(f > f2)) continue
//            f = f2
//                    n3 = n2
//        }
//        nodes = RTree.sort(nodes, n3, false)
//        result = RTree.bucketize(nodes)
//        result = RTree.sort(result, n3, true)
//        return new RSubTree(result.map(subTree => RTree.build(subTree.children)))
//    }

        private static <T> RNode<T> build(List<RNode<T>> nodes) {
            if (nodes.size() == 1) {
                return nodes.get(0);
            }
            if (nodes.size() <= RTree.CHILDREN_PER_NODE) {
                List<RNode<T>> sortedNodes = nodes.stream()
                        .map(node -> {
                            double key = 0.0;
                            for (int i = 0; i < PARAMETER_SPACE; i += 1) {
                                Param param = node.space[i];
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

//
//		private static sort<N extends RNode<unknown>>(nodes: N[], i: number, abs: boolean) {
//        return nodes
//                .map(node => {
//					const param = node.space[i]
//					const f = (param.min + param.max) / 2
//					const key = abs ? Math.abs(f) : f
//        return { key, node }
//				})
//				.sort((a, b) => a.key - b.key)
//				.map(({ node }) => node)
//    }

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

//
//		private static bucketize<T>(nodes: RNode<T>[]) {
//			const arrayList: RSubTree<T>[] = []
//        let arrayList2: RNode<T>[] = []
//			const n = Math.pow(10.0, Math.floor(Math.log(nodes.length - 0.01) / Math.log(10.0)))
//        for (const node of nodes) {
//            arrayList2.push(node)
//            if (arrayList2.length < n) continue
//            arrayList.push(new RSubTree(arrayList2))
//            arrayList2 = []
//        }
//        if (arrayList2.length !== 0) {
//            arrayList.push(new RSubTree(arrayList2))
//        }
//        return arrayList
//    }

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
            if (!arrayList2.isEmpty()) {
                arrayList.add(new RSubTree<>(arrayList2));
            }
            return arrayList;
        }

//
//    private static area(params: Param[]) {
//        let f = 0.0
//        for (const param of params) {
//            f += Math.abs(param.max - param.min)
//        }
//        return f
//    }

        private static double area(Collection<Param> params) {
            double f = 0.0;
            for (Param param : params) {
                f += Math.abs(param.max() - param.min());
            }
            return f;
        }

//
//    public search(target: TargetPoint, distance: DistanceMetric<T>) {
//			const leaf = this.root.search(target.toArray(), distance)
//        return leaf.thing()
//    }

        public T search(TargetPoint target, DistanceMetric<T> distance) {
            RLeaf<T> leaf = this.root.search(target.toArray(), distance);
            return leaf.thing.get();
        }
//}
    }

    //	export abstract class RNode<T> {
//    constructor(public readonly space: Param[]) {}
//
//    public abstract search(values: number[], distance: DistanceMetric<T>): RLeaf<T>
//
//    public distance(values: number[]) {
//        let result = 0
//        for (let i = 0; i < PARAMETER_SPACE; i += 1) {
//            result += square(this.space[i].distance(values[i]))
//        }
//        return result
//    }
//}
    static abstract class RNode<T> {
        protected final Param[] space;

        public RNode(Param[] space) {
            this.space = space;
        }

        public abstract RLeaf<T> search(double[] values, DistanceMetric<T> distance);

        public double distance(double[] values) {
            double result = 0;
            for (int i = 0; i < PARAMETER_SPACE; i += 1) {
                result += Util.square(this.space[i].distance(Param.fromJson(values[i])));
            }
            return result;
        }

        public List<Param> space() {
            return Arrays.asList(this.space);
        }
    }

    //export class RSubTree<T> extends RNode<T> {
//    constructor(public readonly children: RNode<T>[]) {
//        super(RSubTree.buildSpace(children))
//    }
//
//		private static buildSpace<T>(nodes: RNode<T>[]): Param[] {
//        let space = [...Array(PARAMETER_SPACE)].map(() => new Param(Infinity, -Infinity))
//        for (const node of nodes) {
//            space = [...Array(PARAMETER_SPACE)].map((_, i) => space[i].union(node.space[i]))
//        }
//        return space
//    }
//
//    search(values: number[], distance: DistanceMetric<T>): RLeaf<T> {
//        let dist = Infinity
//        let leaf: RLeaf<T> | null = null
//        for (const node of this.children) {
//				const d1 = distance(node, values)
//            if (dist <= d1) continue
//				const leaf2 = node.search(values, distance)
//				const d2 = node == leaf2 ? d1 : distance(leaf2, values)
//            if (dist <= d2) continue
//            dist = d2
//                    leaf = leaf2
//        }
//        return leaf!
//    }
//}
    static class RSubTree<T> extends RNode<T> {
        private final List<RNode<T>> children;

        public RSubTree(List<RNode<T>> children) {
            super(RSubTree.buildSpace(children));
            this.children = children;
        }

        private static <T> Param[] buildSpace(List<RNode<T>> nodes) {
            Param[] space = new Param[PARAMETER_SPACE];
            for (int i = 0; i < PARAMETER_SPACE; i += 1) {
                space[i] = new Param(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
            }
            for (RNode<T> node : nodes) {
                for (int i = 0; i < PARAMETER_SPACE; i += 1) {
                    space[i] = space[i].union(node.space().get(i));
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

//	export class RLeaf<T> extends RNode<T> {
//    constructor(point: ParamPoint, public readonly thing: () => T) {
//        super(point.space())
//    }
//
//    search() {
//        return this
//    }
//}

    static class RLeaf<T> extends RNode<T> {
        private final Supplier<T> thing;

        public RLeaf(ParamPoint point, Supplier<T> thing) {
            super(point.space());
            this.thing = thing;
        }

        @Override
        public RLeaf<T> search(double[] values, DistanceMetric<T> distance) {
            return this;
        }
    }
}