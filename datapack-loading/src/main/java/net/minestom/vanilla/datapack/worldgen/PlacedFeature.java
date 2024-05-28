package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.JsonReader;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.DatapackLoader;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.NamespaceTag;
import net.minestom.vanilla.datapack.json.Optional;
import net.minestom.vanilla.datapack.json.StringTag;
import net.minestom.vanilla.files.FileSystem;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

// https://minecraft.wiki/w/Placed_feature

/**
 * A placed feature determines where a configured feature should be attempted to be placed using placement modifiers.
 * They can be referenced in biomes. Placed features are stored as JSON files within a data pack in the
 * data/<namespace>/worldgen/placed_feature folder.
 */
public interface PlacedFeature {

    /**
     * @return configured feature (referenced by ID or inlined) — The feature to place.
     */
    ConfiguredFeature feature();

    /**
     * @return A list of placement modifiers. They are applied in order.
     */
    List<PlacementModifier> placement();

    static PlacedFeature fromJson(JsonReader reader) throws IOException {
        return JsonUtils.typeMap(reader, token -> switch (token) {
            case BEGIN_OBJECT -> DatapackLoader.moshi(Inline.class);
            case STRING -> json -> new ID(json.nextString());
            default -> null;
        });
    }

    class ID implements PlacedFeature {

        private @Nullable PlacedFeature feature;

        public ID(String id) {
            NamespaceID namespaceId = NamespaceID.from(id);

            // Load the feature from the datapack once the datapack has finished loading
            DatapackLoader.loading().whenFinished(finisher -> {
                finisher.datapack().namespacedData().forEach((domain, data) -> {
                    for (String file : data.world_gen().placed_feature().files()) {
                        String fileId = file.substring(0, file.length() - 5); // Remove .json

                        if (namespaceId.domain().equals(domain) && namespaceId.path().equals(fileId)) {
                            this.feature = data.world_gen().placed_feature().file(file);
                            return;
                        }
                    }
                });
            });
        }

        private PlacedFeature placedFeature() {
            if (feature == null) {
                throw new IllegalStateException("Feature not loaded yet");
            }
            return feature;
        }

        @Override
        public ConfiguredFeature feature() {
            return placedFeature().feature();
        }

        @Override
        public List<PlacementModifier> placement() {
            return placedFeature().placement();
        }
    }

    record Inline(ConfiguredFeature feature, List<PlacementModifier> placement) implements PlacedFeature {
    }

    /**
     * When a placed feature is referenced through a biome file, the placed feature will tell the configured feature in
     * the feature field to place once on the farthest northwest corner of each chunk at the bottom layer of the world.
     * When a placed feature is referenced from a configured feature file or through the /place command, the placed
     * feature will tell the configured feature in the feature field to place once where the original feature/player
     * is located respectively. Placement modifiers can change the position of the feature and the amount of placements.
     * <br/><br/>
     * Placed features are applied in order to determine where feature placement attempt(s) should occur. This can
     * include moving the placement's position, number of positions, and filtering out positions based on given conditions.
     * Each placement attempt applies placement modifiers separately.
     */
    sealed interface PlacementModifier {
        default String type() {
            return JsonUtils.getStringTag(this.getClass());
        }

        static PlacementModifier fromJson(JsonReader reader) throws IOException {
            return JsonUtils.sealedUnionNamespace(reader, PlacementModifier.class, "type");
        }

        /**
         * Either returns the current position or empty. Only passes if the biome at the current position includes this
         * placed feature. No additional field. Important: This modifier type cannot be used in placed features that are
         * referenced from other configured features.
         */
        @NamespaceTag("biome")
        record Biome() implements PlacementModifier {
        }

        /**
         * Returns the current position when the predicate is passed, otherwise return empty.
         * @param predicate The block predicate to test.
         */
        @NamespaceTag("block_predicate_filter")
        record BlockPredicateFilter(BlockPredicate predicate) implements PlacementModifier {
        }

        /**
         * Returns all positions in the current chunk that have been carved out by a carver. This does not include blocks
         * carved out by noise caves.
         * @param step The carving step. Either air or liquid.
         */
        @NamespaceTag("carving_mask")
        record CarvingMask(String step) implements PlacementModifier {
        }

        /**
         * Returns multiple copies of the current block position.
         * @param count Value between 0 and 256 (inclusive).
         */
        @NamespaceTag("count")
        record Count(IntProvider count) implements PlacementModifier {
        }

        /**
         * In the horizontal relative range (0,0) to (16,16), at each vertical layer separated by air, lava or water, tries
         * to randomly select the specified number of horizontal positions, whose Y coordinate is one block above this
         * layer at this selected horizontal position. Return these selected positions.
         * @param count Count on each layer. Value between 0 and 256 (inclusive).
         */
        @NamespaceTag("count_on_every_layer")
        record CountOnEveryLayer(IntProvider count) implements PlacementModifier {
        }

        /**
         * Scans blocks either up or down, until the target condition is met. Returns the block position for which the
         * target condition matches. If no target can be found within the maximum number of steps, returns empty.
         * @param direction_of_search One of up or down.
         * @param max_steps Value between 1 and 32 (inclusive).
         * @param target_condition The block predicate that is searched for.
         * @param allowed_search_condition If specified, each step must match this block position in order to continue the
         * scan. If a block that doesn't match it is met, but no target block found, returns empty.
         */
        @NamespaceTag("environment_scan")
        record EnvironmentScan(String direction_of_search, int max_steps, BlockPredicate target_condition, BlockPredicate allowed_search_condition) implements PlacementModifier {
        }

        /**
         * Sets the Y coordinate to a value provided by a height provider. Returns the new position.
         * @param height The new Y coordinate.
         */
        @NamespaceTag("height_range")
        record HeightRange(HeightProvider height) implements PlacementModifier {
        }

        /**
         * Sets the Y coordinate to one block above the heightmap. Returns the new position.
         * @param heightmap The heightmap to use. One of MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES, OCEAN_FLOOR,
         * OCEAN_FLOOR_WG, WORLD_SURFACE or WORLD_SURFACE_WG.
         */
        @NamespaceTag("heightmap")
        record Heightmap(String heightmap) implements PlacementModifier {
        }

        /**
         * For both X and Z, it adds a random value between 0 and 15 (both inclusive). This is a shortcut for a
         * random_offset modifier with y_spread set to 0 and xz_spread as a uniform int from 0 to 15. No additional fields.
         */
        @NamespaceTag("in_square")
        record InSquare() implements PlacementModifier {
        }

        /**
         * When the noise value at the current block position is positive, returns multiple copies of the current block
         * position, whose count is based on a noise value and can gradually change based on the noise value. When noise
         * value is negative or 0, returns empty. The count is calculated by ceil((noise(x / noise_factor, z / noise_factor)
         * + noise_offset) * noise_to_count_ratio).
         * @param noise_factor Scales the noise input horizontally. Higher values make for wider and more spaced out peaks.
         * @param noise_offset (optional, defaults to 0) Vertical offset of the noise.
         * @param noise_to_count_ratio Ratio of noise value to count.
         */
        @NamespaceTag("noise_based_count")
        record NoiseBasedCount(double noise_factor, @Optional Double noise_offset, double noise_to_count_ratio) implements PlacementModifier {
        }

        /**
         * Returns multiple copies of the current block position. The count is either below_noise or above_noise, based
         * on the noise value at the current block position. First checks noise(x / 200, z / 200) < noise_level. If that
         * is true, uses below_noise, otherwise above_noise.
         * @param noise_level The threshold within the noise of when to use below_noise or above_noise.
         * @param below_noise The count when the noise is below the threshold. Value lower than 0 is treated as 0.
         * @param above_noise The count when the noise is above the threshold. Value lower than 0 is treated as 0.
         */
        @NamespaceTag("noise_threshold_count")
        record NoiseThresholdCount(double noise_level, int below_noise, int above_noise) implements PlacementModifier {
        }

        /**
         * Applies an offset to the current position. Note that the even though X and Z share the same int provider, they
         * are individually sampled, so a different offset can be applied to X and Z.
         * @param xz_spread Value between -16 and 16 (inclusive).
         * @param y_spread Value between -16 and 16 (inclusive).
         */
        @NamespaceTag("random_offset")
        record RandomOffset(IntProvider xz_spread, IntProvider y_spread) implements PlacementModifier {
        }

        /**
         * Either returns the current position or empty. The chance is calculated by 1 / chance.
         * @param chance Must be a positive integer.
         */
        @NamespaceTag("rarity_filter")
        record RarityFilter(int chance) implements PlacementModifier {
        }

        /**
         * Returns the current position if the surface is inside a range. Otherwise returns empty.
         * @param heightmap The heightmap to use. One of MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES, OCEAN_FLOOR,
         * OCEAN_FLOOR_WG, WORLD_SURFACE or WORLD_SURFACE_WG.
         * @param min_inclusive The minimum relative height from the surface to current position.
         * @param max_inclusive The maximum relative height from the surface to current position.
         */
        @NamespaceTag("surface_relative_threshold_filter")
        record SurfaceRelativeThresholdFilter(String heightmap, @Optional Integer min_inclusive, int max_inclusive) implements PlacementModifier {
        }

        /**
         * If the number of blocks of a motion blocking material under the surface (the top non-air block) is less than
         * the specified depth, return the current position. Otherwise, return empty.
         * @param max_water_depth The maximum allowed depth.
         */
        @NamespaceTag("surface_water_depth_filter")
        record SurfaceWaterDepthFilter(int max_water_depth) implements PlacementModifier {
        }
    }
}
