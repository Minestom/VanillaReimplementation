package net.minestom.vanilla.generation.biome;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.math.Util;

public interface BiomeSource {
//    export interface BiomeSource {
//        getBiome(x: number, y: number, z: number, climateSampler: Climate.Sampler): Identifier
//    }

    NamespaceID getBiome(int x, int y, int z, Climate.Sampler climateSampler);

//    export namespace BiomeSource {
//        export function fromJson(obj: unknown): BiomeSource {
//		const root = Json.readObject(obj) ?? {}
//		const type = Json.readString(root.type)?.replace(/^minecraft:/, '')
//            switch (type) {
//                case 'fixed': return FixedBiomeSource.fromJson(obj)
//                case 'checkerboard': return CheckerboardBiomeSource.fromJson(obj)
//                case 'multi_noise': return MultiNoiseBiomeSource.fromJson(obj)
//                case 'the_end': return TheEndBiomeSource.fromJson(obj)
//                default: return { getBiome: () => Identifier.create('plains') }
//            }
//        }
//    }

    static BiomeSource fromJson(Object obj) {
        JsonObject root = Util.jsonObject(obj);

        String type = Util.jsonRequire(root, "type", JsonElement::getAsString).replace("^minecraft:", "");
        return //            case "fixed" -> FixedBiomeSource.fromJson(obj);
                //            case "checkerboard" -> CheckerboardBiomeSource.fromJson(obj);
                //            case "multi_noise" -> MultiNoiseBiomeSource.fromJson(obj);
                //            case "the_end" -> TheEndBiomeSource.fromJson(obj);
                (x, y, z, climateSampler) -> NamespaceID.from("plains");
    }
}
