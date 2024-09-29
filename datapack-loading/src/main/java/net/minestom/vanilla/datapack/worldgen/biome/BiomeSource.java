package net.minestom.vanilla.datapack.worldgen.biome;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;
import net.minestom.vanilla.datapack.worldgen.util.Util;

public interface BiomeSource extends BiomeSources {
//    export interface BiomeSource {
//        getBiome(x: number, y: number, z: number, climateSampler: Climate.Sampler): Identifier
//    }

    Key getBiome(int x, int y, int z, Climate.Sampler climateSampler);

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

    static BiomeSource checkerBoard(int shift, Key... biomes) {
        return new CheckerboardBiomeSource(shift, biomes);
    }

    static BiomeSource fixed(Key biome) {
        return new FixedBiomeSource(biome);
    }

    static BiomeSource multiNoise(Climate.Parameters<Key> parameters) {
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
            default -> (x, y, z, climateSampler) -> Key.key("plains");
        };
    }
}
