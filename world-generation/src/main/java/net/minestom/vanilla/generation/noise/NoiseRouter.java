package net.minestom.vanilla.generation.noise;

import com.google.gson.JsonObject;
import net.minestom.vanilla.generation.Holder;
import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.WorldgenRegistries;
import net.minestom.vanilla.generation.densityfunctions.DensityFunction;
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
            DensityFunction.holder(Holder.parser(WorldgenRegistries.DENSITY_FUNCTION, DensityFunction::fromJson).apply(obj));

    public static NoiseRouter fromJson(Object obj) {
        JsonObject root = Util.jsonObject(obj);
        return new NoiseRouter(
                Util.jsonRequire(root, "barrier", fieldParser::apply),
                Util.jsonRequire(root, "fluid_level_floodedness", fieldParser::apply),
                Util.jsonRequire(root, "fluid_level_spread", fieldParser::apply),
                Util.jsonRequire(root, "lava", fieldParser::apply),
                Util.jsonRequire(root, "temperature", fieldParser::apply),
                Util.jsonRequire(root, "vegetation", fieldParser::apply),
                Util.jsonRequire(root, "continents", fieldParser::apply),
                Util.jsonRequire(root, "erosion", fieldParser::apply),
                Util.jsonRequire(root, "depth", fieldParser::apply),
                Util.jsonRequire(root, "ridges", fieldParser::apply),
                Util.jsonRequire(root, "initial_density_without_jaggedness", fieldParser::apply),
                Util.jsonRequire(root, "final_density", fieldParser::apply),
                Util.jsonRequire(root, "vein_toggle", fieldParser::apply),
                Util.jsonRequire(root, "vein_ridged", fieldParser::apply),
                Util.jsonRequire(root, "vein_gap", fieldParser::apply)
        );
    }

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
    static final Map<String, Cache> noiseCache = new HashMap<>();

    private record Cache(long randomA, long randomB, NormalNoise noise) {
    }

    static NormalNoise instantiate(WorldgenRandom.Positional random, Holder<NormalNoise.NoiseParameters> noise) {
        if (noise.key() == null)
            throw new Error("Cannot instantiate noise from direct holder");
        var key = noise.key().toString();
        var randomKey = random.seedKey();
        var cached = noiseCache.get(key);
        if (cached != null && Objects.equals(cached.randomA, randomKey[0]) && Objects.equals(cached.randomB, randomKey[1])) {
            return cached.noise;
        }
        var result = new NormalNoise(random.fromHashOf(key), noise.value());
        noiseCache.put(key, new Cache(randomKey[0], randomKey[1], result));
        return result;
    }
}
