package net.minestom.vanilla.generation.noise;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minestom.vanilla.generation.Holder;
import net.minestom.vanilla.generation.WorldgenRegistries;
import net.minestom.vanilla.generation.densityfunctions.DensityFunction;
import net.minestom.vanilla.generation.densityfunctions.DensityFunctions;
import net.minestom.vanilla.generation.math.Util;
import net.minestom.vanilla.generation.random.WorldGenRandom;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public record NoiseRouter(DensityFunction barrier, DensityFunction fluidLevelFloodedness,
                          DensityFunction fluidLevelSpread, DensityFunction lava, DensityFunction temperature,
                          DensityFunction vegetation, DensityFunction continents, DensityFunction erosion,
                          DensityFunction depth, DensityFunction ridges,
                          DensityFunction initialDensityWithoutJaggedness, DensityFunction finalDensity,
                          DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap) {


    //    export namespace NoiseRouter {
    public static Function<Object, DensityFunction> fieldParser = obj ->
            new DensityFunctions.HolderHolder(Holder.parser(WorldgenRegistries.DENSITY_FUNCTION, DensityFunctions::fromJson).apply(obj));

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
    public static NoiseRouter mapAll(NoiseRouter router, DensityFunction.Visitor visitor) {
        return new NoiseRouter(
                router.barrier().mapAll(visitor),
                router.fluidLevelFloodedness().mapAll(visitor),
                router.fluidLevelSpread().mapAll(visitor),
                router.lava().mapAll(visitor),
                router.temperature().mapAll(visitor),
                router.vegetation().mapAll(visitor),
                router.continents().mapAll(visitor),
                router.erosion().mapAll(visitor),
                router.depth().mapAll(visitor),
                router.ridges().mapAll(visitor),
                router.initialDensityWithoutJaggedness().mapAll(visitor),
                router.finalDensity().mapAll(visitor),
                router.veinToggle().mapAll(visitor),
                router.veinRidged().mapAll(visitor),
                router.veinGap().mapAll(visitor)
        );
    }

    // new Map<string, [bigint | number, bigint | number, NormalNoise]>()
    static Map<String, Object[]> noiseCache = new HashMap<>();

    static NormalNoise instantiate(WorldGenRandom.Positional random, Holder<NormalNoise.NoiseParameters> noise) {
        if (noise.key() == null)
            throw new Error("Cannot instantiate noise from direct holder");
        var key = noise.key().toString();
        var randomKey = random.seedKey();
        var cached = noiseCache.get(key);
        if (cached != null && Objects.equals(cached[0], randomKey[0]) && Objects.equals(cached[1], randomKey[1])) {
            return (NormalNoise) cached[2];
        }
        var result = new NormalNoise(random.fromHashOf(key), noise.value());
        noiseCache.put(key, new Object[] {randomKey[0], randomKey[1], result});
        return result;
    }
}
