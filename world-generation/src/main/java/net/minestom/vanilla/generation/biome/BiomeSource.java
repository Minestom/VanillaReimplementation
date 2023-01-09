package net.minestom.vanilla.generation.biome;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.Util;
import org.jetbrains.annotations.NotNull;

public interface BiomeSource {

    @NotNull NamespaceID getBiome(int x, int y, int z, @NotNull Climate.Sampler climateSampler);

    static @NotNull BiomeSource checkerBoard(int shift, @NotNull NamespaceID @NotNull ... biomes) {
        return new BiomeSources.CheckerboardBiomeSource(shift, biomes);
    }

    static @NotNull BiomeSource fixed(@NotNull NamespaceID biome) {
        return new BiomeSources.FixedBiomeSource(biome);
    }

    static @NotNull BiomeSource multiNoise(@NotNull Climate.Parameters<@NotNull NamespaceID> parameters) {
        return new BiomeSources.MultiNoiseBiomeSource(parameters);
    }

    static @NotNull BiomeSource theEnd() {
        return new BiomeSources.TheEndBiomeSource();
    }

    static @NotNull BiomeSource fromJson(Object obj) {
        JsonObject root = Util.jsonObject(obj);

        String type = Util.jsonRequire(root, "type", JsonElement::getAsString).replace("^minecraft:", "");
        return switch (type) {
            case "fixed" -> BiomeSources.FixedBiomeSource.fromJson(obj);
            case "checkerboard" -> BiomeSources.CheckerboardBiomeSource.fromJson(obj);
            case "multi_noise" -> BiomeSources.MultiNoiseBiomeSource.fromJson(obj);
            case "the_end" -> BiomeSources.TheEndBiomeSource.fromJson(obj);
            default -> fixed(NamespaceID.from("plains"));
        };
    }
}
