package net.minestom.vanilla.generation.biome;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.biome.NativeBiomeSelectors.CheckerboardBiomeSelector;
import net.minestom.vanilla.generation.biome.NativeBiomeSelectors.FixedBiomeSelector;
import net.minestom.vanilla.generation.biome.NativeBiomeSelectors.MultiNoiseBiomeSelector;
import net.minestom.vanilla.generation.biome.NativeBiomeSelectors.TheEndBiomeSelector;

public interface BiomeSelector {

    NamespaceID select(int x, int y, int z, Climate.Sampler climateSampler);

    static BiomeSelector checkerBoard(int shift, NamespaceID... biomes) {
        return new CheckerboardBiomeSelector(shift, biomes);
    }

    static BiomeSelector fixed(NamespaceID biome) {
        return new FixedBiomeSelector(biome);
    }

    static BiomeSelector multiNoise(Climate.Parameters<NamespaceID> parameters) {
        return new MultiNoiseBiomeSelector(parameters);
    }

    static BiomeSelector theEnd() {
        return new TheEndBiomeSelector();
    }

    static BiomeSelector fromJson(Object obj) {
        return BiomeSelectorsUtil.fromJson(obj);
    }
}
