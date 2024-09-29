package net.minestom.vanilla.datapack.worldgen.biome;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;
import net.minestom.vanilla.datapack.worldgen.DensityFunction;
import net.minestom.vanilla.datapack.worldgen.util.Util;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

interface BiomeSources {

    record CheckerboardBiomeSource(int n, int shift, List<Key> biomes) implements BiomeSource {

        public CheckerboardBiomeSource(int shift, Key[] biomes) {
            this(biomes.length, shift, List.of(biomes));
        }

        public CheckerboardBiomeSource {
            if (biomes.isEmpty()) {
                throw new IllegalArgumentException("Cannot create checkerboard biome source without biomes");
            }
        }

        @Override
        public Key getBiome(int x, int y, int z, Climate.Sampler climateSampler) {
            int i = (((x >> this.shift) + (z >> this.shift)) % this.n + this.n) % this.n;
            return this.biomes.get(i);
        }

        public static CheckerboardBiomeSource fromJson(Object obj) {
            int scale = Util.jsonElse(Util.jsonObject(obj), "scale", 2, JsonElement::getAsInt);
            Key[] biomes;
            if (obj instanceof String) {
                biomes = new Key[]{Key.key((String) obj)};
            } else if (obj instanceof JsonElement) {
                biomes = Util.jsonArray((JsonElement) obj, element -> Key.key(element.getAsString())).toArray(new Key[0]);
            } else {
                throw new IllegalArgumentException("Cannot parse biome source from " + obj);
            }
            return new CheckerboardBiomeSource(scale + 2, biomes);
        }
    }

    record FixedBiomeSource(Key biome) implements BiomeSource {

        @Override
        public Key getBiome(int x, int y, int z, Climate.Sampler climateSampler) {
            return this.biome;
        }

        public static FixedBiomeSource fromJson(Object obj) {
            JsonObject root = Util.jsonObject(obj);
            Key biome = Key.key(Util.jsonElse(root, "biome", "plains", JsonElement::getAsString));
            return new FixedBiomeSource(biome);
        }
    }

    record MultiNoiseBiomeSource(Climate.Parameters<Key> parameters) implements BiomeSource {

        @Override
        public Key getBiome(int x, int y, int z, Climate.Sampler climateSampler) {
            Climate.TargetPoint target = climateSampler.sample(x, y, z);
            return this.parameters.find(target);
        }

        public static MultiNoiseBiomeSource fromJson(Object obj) {
            JsonObject root = Util.jsonObject(obj);
            JsonArray biomes = Util.jsonArray(root.get("biomes"));

            var biomesList = StreamSupport.stream(biomes.spliterator(), false).map(b -> {
                JsonObject json = Util.jsonObject(b);
                var biomeName = Key.key(Util.jsonRequire(json, "biome", JsonElement::getAsString));
                var parameters = Climate.ParamPoint.fromJson(json.get("parameters"));
                return Map.entry(biomeName, parameters);
            }).toList();

            Map<Climate.ParamPoint, Supplier<Key>> parameters = biomesList.stream().collect(Collectors.toMap(
                    Map.Entry::getValue,
                    e -> e::getKey
            ));

            return new MultiNoiseBiomeSource(new Climate.Parameters<>(parameters));
        }
    }

    record TheEndBiomeSource() implements BiomeSource {
        private static final Key END = Key.key("the_end");
        private static final Key HIGHLANDS = Key.key("end_highlands");
        private static final Key MIDLANDS = Key.key("end_midlands");
        private static final Key ISLANDS = Key.key("small_end_islands");
        private static final Key BARRENS = Key.key("end_barrens");

        @Override
        public Key getBiome(int x, int y, int z, Climate.Sampler climateSampler) {
            int blockX = x << 2;
            int blockY = y << 2;
            int blockZ = z << 2;

            int sectionX = blockX >> 4;
            int sectionZ = blockZ >> 4;

            if (sectionX * sectionX + sectionZ * sectionZ <= 4096) {
                return END;
            }

            DensityFunction.Context context = DensityFunction.context((sectionX * 2 + 1) * 8, blockY, (sectionZ * 2 + 1) * 8);
            double erosion = climateSampler.erosion().compute(context);

            if (erosion > 0.25) {
                return HIGHLANDS;
            } else if (erosion >= -0.0625) {
                return MIDLANDS;
            } else if (erosion >= -0.21875) {
                return BARRENS;
            } else {
                return ISLANDS;
            }
        }

        public static TheEndBiomeSource fromJson(Object obj) {
            return new TheEndBiomeSource();
        }
    }
}
