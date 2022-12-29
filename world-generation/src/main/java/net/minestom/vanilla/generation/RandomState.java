package net.minestom.vanilla.generation;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.biome.Climate;
import net.minestom.vanilla.generation.densityfunctions.DensityFunction;
import net.minestom.vanilla.generation.noise.*;
import net.minestom.vanilla.generation.random.LegacyRandom;
import net.minestom.vanilla.generation.random.WorldgenRandom;
import net.minestom.vanilla.generation.random.XoroshiroRandom;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RandomState {
    private final Map<String, NormalNoise> noiseCache;
    private final Map<String, WorldgenRandom.Positional> randomCache;

    public final WorldgenRandom.Positional random;
    public final WorldgenRandom.Positional aquiferRandom;
    public final WorldgenRandom.Positional oreRandom;
    public final SurfaceSystem surfaceSystem;
    public final NoiseRouter router;
    public final Climate.Sampler sampler;

    public final long seed;

    public RandomState(NoiseGeneratorSettings settings, long seed) {
        this.seed = seed;
        this.noiseCache = new HashMap<>();
        this.randomCache = new HashMap<>();

        this.random = (settings.legacyRandomSource() ? new LegacyRandom(seed) : XoroshiroRandom.create(seed)).forkPositional();
        this.aquiferRandom = this.random.fromHashOf(NamespaceID.from("aquifer").toString()).forkPositional();
        this.oreRandom = this.random.fromHashOf(NamespaceID.from("ore").toString()).forkPositional();
        this.surfaceSystem = new SurfaceSystem(settings.surfaceRule(), settings.defaultBlock(), seed);
        this.router = NoiseRouter.mapAll(settings.noiseRouter(), this.createMapper(settings.noise(), settings.legacyRandomSource()));
        this.sampler = Climate.Sampler.fromRouter(this.router);
    }

    public DensityFunction.Mapper createMapper(NoiseSettings noiseSettings, boolean legacyRandom) {
        Map<String, DensityFunction> mapped = new HashMap<>();

        Function<Holder<NormalNoise.NoiseParameters>, NormalNoise> getNoise = (noise) -> {
            NamespaceID key = noise.key();
            if (key == null) {
                throw new Error("Cannot create noise without key");
            }
            if (legacyRandom) {
                if (key.equals(NamespaceID.from("temperature"))) {
                    return new NormalNoise(new LegacyRandom(this.seed), NormalNoise.NoiseParameters.create(-7, new double[]{1, 1}));
                }
                if (key.equals(NamespaceID.from("vegetation"))) {
                    return new NormalNoise(new LegacyRandom(this.seed + 1), NormalNoise.NoiseParameters.create(-7, new double[]{1, 1}));
                }
                if (key.equals(NamespaceID.from("offset"))) {
                    return new NormalNoise(this.random.fromHashOf("offset"), NormalNoise.NoiseParameters.create(0, new double[]{0}));
                }
            }
            return this.getOrCreateNoise(key);
        };
        return DensityFunction.createMapper(seed, noiseSettings, mapped, getNoise, random);
    }

    //    public getOrCreateNoise(id: Identifier) {
//        const noises = Registry.REGISTRY.getOrThrow(Identifier.create("worldgen/noise")) as Registry<NoiseParameters>
//            return computeIfAbsent(this.noiseCache, id.toString(), key =>
//                    new NormalNoise(this.random.fromHashOf(key), noises.getOrThrow(id))
//        )
//    }
    public NormalNoise getOrCreateNoise(NamespaceID id) {
        //noinspection unchecked
        Registry<NormalNoise.NoiseParameters> noises = (Registry<NormalNoise.NoiseParameters>)
                Registry.REGISTRY.getOrThrow(NamespaceID.from("worldgen/noise"));
        return noiseCache.computeIfAbsent(id.toString(),
                key -> new NormalNoise(random.fromHashOf(key), noises.getOrThrow(id)));
    }

    //    public getOrCreateRandom(id:Identifier) {
//        return computeIfAbsent(this.randomCache, id.toString(), key =>
//            this.random.fromHashOf(key).forkPositional()
//        )
//    }
    public WorldgenRandom.Positional getOrCreateRandom(NamespaceID id) {
        return randomCache.computeIfAbsent(id.toString(),
                key -> random.fromHashOf(key).forkPositional());
    }
}
