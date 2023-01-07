package net.minestom.vanilla.generation.noise;

import com.google.gson.JsonObject;
import net.minestom.vanilla.generation.Holder;
import net.minestom.vanilla.generation.WorldgenRegistries;
import net.minestom.vanilla.generation.densityfunctions.DensityFunction;
import net.minestom.vanilla.generation.densityfunctions.DensityFunctions;
import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.random.WorldgenRandom;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public record NoiseRouter(DensityFunction barrier,
                          DensityFunction fluidLevelFloodedness,
                          DensityFunction fluidLevelSpread,
                          DensityFunction lava,
                          DensityFunction temperature,
                          DensityFunction vegetation,
                          DensityFunction continents,
                          DensityFunction erosion,
                          DensityFunction depth,
                          DensityFunction ridges,
                          DensityFunction initialDensityWithoutJaggedness,
                          DensityFunction finalDensity,
                          DensityFunction veinToggle,
                          DensityFunction veinRidged,
                          DensityFunction veinGap) {


    //    export namespace NoiseRouter {
    public static final Function<Object, DensityFunction> fieldParser = obj ->
            new DensityFunctions.HolderHolder(Holder.parser(WorldgenRegistries.DENSITY_FUNCTION, DensityFunction::fromJson).apply(obj));

    //        export function fromJson(obj: unknown): NoiseRouter {
//		const root = Json.readObject(obj) ?? {}
//            return {
//                    barrier: fieldParser(root.barrier),
//                    fluidLevelFloodedness: fieldParser(root.fluid_level_floodedness),
//                    fluidLevelSpread: fieldParser(root.fluid_level_spread),
//                    lava: fieldParser(root.lava),
//                    temperature: fieldParser(root.temperature),
//                    vegetation: fieldParser(root.vegetation),
//                    continents: fieldParser(root.continents),
//                    erosion: fieldParser(root.erosion),
//                    depth: fieldParser(root.depth),
//                    ridges: fieldParser(root.ridges),
//                    initialDensityWithoutJaggedness: fieldParser(root.initial_density_without_jaggedness),
//                    finalDensity: fieldParser(root.final_density),
//                    veinToggle: fieldParser(root.vein_toggle),
//                    veinRidged: fieldParser(root.vein_ridged),
//                    veinGap: fieldParser(root.vein_gap),
//		}
//        }
    public static NoiseRouter fromJson(Object obj) {
        JsonObject root = Util.jsonObject(obj);
        return new NoiseRouter(
                Util.jsonRequire(root, "barrier", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "fluid_level_floodedness", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "fluid_level_spread", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "lava", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "temperature", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "vegetation", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "continents", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "erosion", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "depth", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "ridges", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "initial_density_without_jaggedness", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "final_density", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "vein_toggle", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "vein_ridged", element -> fieldParser.apply(element)),
                Util.jsonRequire(root, "vein_gap", element -> fieldParser.apply(element))
        );
    }

    //        export function mapAll(router: NoiseRouter, visitor: DensityFunction.Visitor) {
//            return {
//                    barrier: router.barrier.mapAll(visitor),
//                    fluidLevelFloodedness: router.fluidLevelFloodedness.mapAll(visitor),
//                    fluidLevelSpread: router.fluidLevelSpread.mapAll(visitor),
//                    lava: router.lava.mapAll(visitor),
//                    temperature: router.temperature.mapAll(visitor),
//                    vegetation: router.vegetation.mapAll(visitor),
//                    continents: router.continents.mapAll(visitor),
//                    erosion: router.erosion.mapAll(visitor),
//                    depth: router.depth.mapAll(visitor),
//                    ridges: router.ridges.mapAll(visitor),
//                    initialDensityWithoutJaggedness: router.initialDensityWithoutJaggedness.mapAll(visitor),
//                    finalDensity: router.finalDensity.mapAll(visitor),
//                    veinToggle: router.veinToggle.mapAll(visitor),
//                    veinRidged: router.veinRidged.mapAll(visitor),
//                    veinGap: router.veinGap.mapAll(visitor),
//		    }
//        }
    public static NoiseRouter mapAll(NoiseRouter router, DensityFunction.Mapper mapper) {
        return new NoiseRouter(
                router.barrier().mapAll(mapper),
                router.fluidLevelFloodedness().mapAll(mapper),
                router.fluidLevelSpread().mapAll(mapper),
                router.lava().mapAll(mapper),
                router.temperature().mapAll(mapper),
                router.vegetation().mapAll(mapper),
                router.continents().mapAll(mapper),
                router.erosion().mapAll(mapper),
                router.depth().mapAll(mapper),
                router.ridges().mapAll(mapper),
                router.initialDensityWithoutJaggedness().mapAll(mapper),
                router.finalDensity().mapAll(mapper),
                router.veinToggle().mapAll(mapper),
                router.veinRidged().mapAll(mapper),
                router.veinGap().mapAll(mapper)
        );
    }

    // new Map<string, [bigint | number, bigint | number, NormalNoise]>()
    static final Map<String, Object[]> noiseCache = new HashMap<>();

    static NormalNoise instantiate(WorldgenRandom.Positional random, Holder<NormalNoise.NoiseParameters> noise) {
        if (noise.key() == null)
            throw new Error("Cannot instantiate noise from direct holder");
        var key = noise.key().toString();
        var randomKey = random.seedKey();
        var cached = noiseCache.get(key);
        if (cached != null && Objects.equals(cached[0], randomKey[0]) && Objects.equals(cached[1], randomKey[1])) {
            return (NormalNoise) cached[2];
        }
        var result = new NormalNoise(random.fromHashOf(key), noise.value());
        noiseCache.put(key, new Object[]{randomKey[0], randomKey[1], result});
        return result;
    }
}
