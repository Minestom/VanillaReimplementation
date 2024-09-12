package net.minestom.vanilla.generation;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.worldgen.NoiseSettings;
import net.minestom.vanilla.datapack.worldgen.biome.Climate;
import net.minestom.vanilla.datapack.worldgen.random.LegacyRandom;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;
import net.minestom.vanilla.datapack.worldgen.random.XoroshiroRandom;

public record RandomState(
        long seed,
        WorldgenRandom.Positional random,
        WorldgenRandom.Positional aquiferRandom,
        WorldgenRandom.Positional oreRandom,
        SurfaceSystem surfaceSystem,
        NoiseSettings.NoiseRouter router,
        Climate.Sampler sampler) {

    public RandomState(NoiseSettings settings, long seed) {
        this(
            settings,
            seed,
            (settings.legacy_random_source() ? new LegacyRandom(seed) : new XoroshiroRandom(seed)).forkPositional()
        );
    }

    public RandomState(NoiseSettings settings, long seed, WorldgenRandom.Positional random) {
        this(
                seed,
                random,
                random.fromHashOf(NamespaceID.from("aquifer").toString()).forkPositional(),
                random.fromHashOf(NamespaceID.from("ore").toString()).forkPositional(),
                new SurfaceSystem(settings.surface_rule(), settings.default_block().toMinestom(), seed),
                settings.noise_router(),
                Climate.Sampler.fromRouter(settings.noise_router())
        );
    }
}
