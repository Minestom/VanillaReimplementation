package net.minestom.vanilla.generation;


import net.kyori.adventure.key.Key;
import net.minestom.vanilla.datapack.worldgen.NoiseSettings;
import net.minestom.vanilla.datapack.worldgen.biome.Climate;
import net.minestom.vanilla.datapack.worldgen.random.LegacyRandom;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;
import net.minestom.vanilla.datapack.worldgen.random.XoroshiroRandom;

public class RandomState {

    public final WorldgenRandom.Positional random;
    public final WorldgenRandom.Positional aquiferRandom;
    public final WorldgenRandom.Positional oreRandom;
    public final SurfaceSystem surfaceSystem;
    public final NoiseSettings.NoiseRouter router;
    public final Climate.Sampler sampler;

    public final long seed;

    public RandomState(NoiseSettings settings, long seed) {
        this.seed = seed;
        this.random = (settings.legacy_random_source() ? new LegacyRandom(seed) : new XoroshiroRandom(seed)).forkPositional();
        this.aquiferRandom = this.random.fromHashOf(Key.key("aquifer").toString()).forkPositional();
        this.oreRandom = this.random.fromHashOf(Key.key("ore").toString()).forkPositional();
        this.surfaceSystem = new SurfaceSystem(settings.surface_rule(), settings.default_block().toMinestom(), seed);
        this.router = settings.noise_router();
        this.sampler = Climate.Sampler.fromRouter(this.router);
    }
}
