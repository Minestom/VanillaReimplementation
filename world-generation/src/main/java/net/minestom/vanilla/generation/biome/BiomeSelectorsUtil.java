package net.minestom.vanilla.generation.biome;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.Util;

class BiomeSelectorsUtil {
    static BiomeSelector fromJson(Object obj) {
        JsonObject root = Util.jsonObject(obj);

        String type = Util.jsonRequire(root, "type", JsonElement::getAsString).replace("^minecraft:", "");
        return switch (type) {
            case "fixed" -> NativeBiomeSelectors.FixedBiomeSelector.fromJson(obj);
            case "checkerboard" -> NativeBiomeSelectors.CheckerboardBiomeSelector.fromJson(obj);
            case "multi_noise" -> NativeBiomeSelectors.MultiNoiseBiomeSelector.fromJson(obj);
            case "the_end" -> NativeBiomeSelectors.TheEndBiomeSelector.fromJson(obj);
            default -> (x, y, z, climateSampler) -> NamespaceID.from("plains");
        };
    }
}
