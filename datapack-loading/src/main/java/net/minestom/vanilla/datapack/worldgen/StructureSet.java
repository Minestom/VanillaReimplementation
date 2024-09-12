package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonReader;
import net.minestom.server.coordinate.Point;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.NamespaceTag;
import net.minestom.vanilla.datapack.json.Optional;

import java.io.IOException;
import java.util.List;

/**
 *
 * @param structures required, but can be empty. The structures that may be placed. One configured structure feature shouldn't be included by two structure sets.
 * @param placement how the structures should be placed.
 */
public record StructureSet(List<StructureEntry> structures, Placement placement) {

    /**
     * A structure to be placed.
     * @param structure the structure to be placed.
     * @param weight determines the chance of it being chosen over others. Must be a positive integer.
     */
    public record StructureEntry(Structure structure, int weight) {
    }

    /**
     * How the structures should be placed.
     */
    public sealed interface Placement {

        default NamespaceID type() {
            return JsonUtils.getNamespaceTag(this.getClass());
        }

        /**
         * @return a number that assists in randomization; see salt (cryptography). Must be a non-negative integer.
         */
        int salt();

        /**
         * @return probability to try to generate if other conditions below are met. Values between 0.0 to 1.0 (inclusive). Setting it to a number does not mean one structure is generated this often, only that the game attempts to generate one; biomes or terrain could lead to the structure not being generated.
         */
        @Optional Float frequency();

        /**
         * @return provides a random number generator algorithm for frequency. One of default (the random number depends on the seed, position and  salt), legacy_type_1 (the random number depends only on the seed and position, and randomness only occurs when the locations differ greatly), legacy_type_2 (same as default, but with fixed salt: 10387320) and legacy_type_3 (the random number depends only on seed and position).
         */
        FrequencyReductionMethod frequencyReductionMethod();

        /**
         * @return specifies that it cannot be placed near certain structures.
         */
        @Optional ExclusionZone exclusionZone();

        /**
         * @return the chunk coordinate offset given when using /locate structure.
         */
        Point locateOffset();

        static Placement fromJson(JsonReader reader) throws IOException {
            return JsonUtils.sealedUnionNamespace(reader, Placement.class, "type");
        }

        /**
         * Structures are spread evenly in the entire world. In vanilla, this placement type is used for most structures (like bastion remnants or swamp huts). The world is split into squares with side length of  spacing chunks. One structure is placed in a random position within each square. A structure can't be placed in  separation chunks along the positive X/Z edge of a square.
         * @param spreadType (optional, defaults to linear) One of linear or triangular.
         * @param spacing Average distance between two neighboring generation attempts. Value between 0 and 4096 (inclusive).
         * @param separation Minimum distance (in chunks) between two neighboring attempts. Value between 0 and 4096 (inclusive). Has to be strictly smaller than  spacing. The maximum distance of two neighboring generation attempts is 2*spacing - separation.
         */
        @NamespaceTag("minecraft:random_spread")
        record RandomSpread(int salt, @Optional Float frequency, FrequencyReductionMethod frequencyReductionMethod,
                            ExclusionZone exclusionZone, Point locateOffset, SpreadType spreadType, int spacing,
                            int separation) implements Placement {

            public enum SpreadType {
                linear,
                triangular
            }
        }

        /**
         * A fixed number of structures is placed in concentric rings around the origin of the world. In vanilla, this placement is only used for strongholds.
         * @param distance the thickness of a ring plus that of a gap between two rings. Value between 0 and 1023 (inclusive). Unit is 6 chunks
         * @param count the total number of generation attempts in this dimension. Value between 1 and 4095 (inclusive).
         * @param preferredBiomes biome (referenced by ID), or biome #tag or list (containing IDs) — Biomes in which the structure is likely to be generated.
         * @param spread how many attempts are on the closest ring to spawn. Value between 0 and 1023 (inclusive). The number of attempts on the Nth ring is: spread * (N^2 + 3 * N + 2) / 6, until the number of attempts reaches the total  count.
         */
        @NamespaceTag("minecraft:concentric_rings")
        record ConcentricRings(int salt, @Optional Float frequency, FrequencyReductionMethod frequencyReductionMethod,
                               ExclusionZone exclusionZone, Point locateOffset, int distance, int count,
                               List<NamespaceID> preferredBiomes, int spread) implements Placement {
        }
    }

    public enum FrequencyReductionMethod {
        @Json(name = "default")
        default_,
        legacy_type_1,
        legacy_type_2,
        legacy_type_3
    }

    public record ExclusionZone(int chunkCount, String otherSet) {
    }

    public enum PlacementType {
        minecraft_concentric_rings,
        minecraft_random_spread
    }
}
