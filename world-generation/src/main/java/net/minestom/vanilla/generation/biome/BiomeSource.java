package net.minestom.vanilla.generation.biome;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.Util;

public interface BiomeSource extends BiomeSources {

    NamespaceID getBiome(int x, int y, int z, Climate.Sampler climateSampler);

    static BiomeSource checkerBoard(int shift, NamespaceID... biomes) {
        return new CheckerboardBiomeSource(shift, biomes);
    }

    static BiomeSource fixed(NamespaceID biome) {
        return new FixedBiomeSource(biome);
    }

    static BiomeSource multiNoise(Climate.Parameters<NamespaceID> parameters) {
        return new MultiNoiseBiomeSource(parameters);
    }

    static BiomeSource theEnd() {
        return new TheEndBiomeSource();
    }

    static BiomeSource fromJson(Object obj) {
        JsonObject root = Util.jsonObject(obj);

        String type = Util.jsonRequire(root, "type", JsonElement::getAsString).replace("^minecraft:", "");
        return switch (type) {
            case "fixed" -> FixedBiomeSource.fromJson(obj);
            case "checkerboard" -> CheckerboardBiomeSource.fromJson(obj);
            case "multi_noise" -> MultiNoiseBiomeSource.fromJson(obj);
            case "the_end" -> TheEndBiomeSource.fromJson(obj);
            default -> (x, y, z, climateSampler) -> NamespaceID.from("plains");
        };
    }
}
