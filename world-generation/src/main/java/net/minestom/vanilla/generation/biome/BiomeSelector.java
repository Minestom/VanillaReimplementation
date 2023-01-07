package net.minestom.vanilla.generation.biome;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.Util;

public interface BiomeSelector extends BiomeSelectors {

    NamespaceID getBiome(int x, int y, int z, Climate.Sampler climateSampler);

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
        JsonObject root = Util.jsonObject(obj);

        String type = Util.jsonRequire(root, "type", JsonElement::getAsString).replace("^minecraft:", "");
        return switch (type) {
            case "fixed" -> FixedBiomeSelector.fromJson(obj);
            case "checkerboard" -> CheckerboardBiomeSelector.fromJson(obj);
            case "multi_noise" -> MultiNoiseBiomeSelector.fromJson(obj);
            case "the_end" -> TheEndBiomeSelector.fromJson(obj);
            default -> (x, y, z, climateSampler) -> NamespaceID.from("plains");
        };
    }
}
