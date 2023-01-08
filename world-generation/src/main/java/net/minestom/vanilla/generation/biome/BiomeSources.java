package net.minestom.vanilla.generation.biome;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.Util;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

interface BiomeSources {

    record CheckerboardBiomeSource(int n, int shift, List<NamespaceID> biomes) implements BiomeSource {

        public CheckerboardBiomeSource(int shift, NamespaceID[] biomes) {
            this(biomes.length, shift, List.of(biomes));
        }

        public CheckerboardBiomeSource {
            if (biomes.isEmpty()) {
                throw new IllegalArgumentException("Cannot create checkerboard biome source without biomes");
            }
        }

        @Override
        public NamespaceID getBiome(int x, int y, int z, Climate.Sampler climateSampler) {
            int i = (((x >> this.shift) + (z >> this.shift)) % this.n + this.n) % this.n;
            return this.biomes.get(i);
        }

        public static CheckerboardBiomeSource fromJson(Object obj) {
            int scale = Util.jsonElse(Util.jsonObject(obj), "scale", 2, JsonElement::getAsInt);
            NamespaceID[] biomes;
            if (obj instanceof String) {
                biomes = new NamespaceID[]{NamespaceID.from((String) obj)};
            } else if (obj instanceof JsonElement) {
                biomes = Util.jsonArray((JsonElement) obj, element -> NamespaceID.from(element.getAsString())).toArray(new NamespaceID[0]);
            } else {
                throw new IllegalArgumentException("Cannot parse biome source from " + obj);
            }
            return new CheckerboardBiomeSource(scale + 2, biomes);
        }
    }

    record FixedBiomeSource(NamespaceID biome) implements BiomeSource {

        @Override
        public NamespaceID getBiome(int x, int y, int z, Climate.Sampler climateSampler) {
            return this.biome;
        }

        public static FixedBiomeSource fromJson(Object obj) {
            JsonObject root = Util.jsonObject(obj);
            NamespaceID biome = NamespaceID.from(Util.jsonElse(root, "biome", "plains", JsonElement::getAsString));
            return new FixedBiomeSource(biome);
        }
    }

    record MultiNoiseBiomeSource(Climate.Parameters<NamespaceID> parameters) implements BiomeSource {

        @Override
        public NamespaceID getBiome(int x, int y, int z, Climate.Sampler climateSampler) {
            Climate.TargetPoint target = climateSampler.sample(x, y, z);
            return this.parameters.find(target);
        }

        public static MultiNoiseBiomeSource fromJson(Object obj) {
            JsonObject root = Util.jsonObject(obj);
            JsonArray biomes = Util.jsonArray(root.get("biomes"));

            var biomesList = StreamSupport.stream(biomes.spliterator(), false).map(b -> {
                JsonObject json = Util.jsonObject(b);
                var biomeName = NamespaceID.from(Util.jsonRequire(json, "biome", JsonElement::getAsString));
                var parameters = Climate.ParamPoint.fromJson(json.get("parameters"));
                return Map.entry(biomeName, parameters);
            }).toList();

            Map<Climate.ParamPoint, Supplier<NamespaceID>> parameters = biomesList.stream().collect(Collectors.toMap(
                    Map.Entry::getValue,
                    e -> e::getKey
            ));

            return new MultiNoiseBiomeSource(new Climate.Parameters<>(parameters));
        }
    }

    record TheEndBiomeSource() implements BiomeSource {
        private static final NamespaceID END = NamespaceID.from("the_end");
        private static final NamespaceID HIGHLANDS = NamespaceID.from("end_highlands");
        private static final NamespaceID MIDLANDS = NamespaceID.from("end_midlands");
        private static final NamespaceID ISLANDS = NamespaceID.from("small_end_islands");
        private static final NamespaceID BARRENS = NamespaceID.from("end_barrens");

        @Override
        public NamespaceID getBiome(int x, int y, int z, Climate.Sampler climateSampler) {
            int blockX = x << 2;
            int blockY = y << 2;
            int blockZ = z << 2;

            int sectionX = blockX >> 4;
            int sectionZ = blockZ >> 4;

            if (sectionX * sectionX + sectionZ * sectionZ <= 4096) {
                return END;
            }

            Point context = new Vec((sectionX * 2 + 1) * 8, blockY, (sectionZ * 2 + 1) * 8);
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
