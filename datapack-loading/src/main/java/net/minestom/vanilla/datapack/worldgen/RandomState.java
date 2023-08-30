package net.minestom.vanilla.datapack.worldgen;

import net.minestom.server.utils.NamespaceID;
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
        this.random = (settings.legacy_random_source() ? new LegacyRandom(seed) : XoroshiroRandom.create(seed)).forkPositional();
        this.aquiferRandom = this.random.fromHashOf(NamespaceID.from("aquifer").toString()).forkPositional();
        this.oreRandom = this.random.fromHashOf(NamespaceID.from("ore").toString()).forkPositional();
        this.surfaceSystem = new SurfaceSystem(settings.surfaceRule(), settings.default_block().toMinestom(), seed);
        this.router = settings.noiseRouter();
        this.sampler = Climate.Sampler.fromRouter(this.router);
    }
}
