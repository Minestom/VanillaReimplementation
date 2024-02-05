package net.minestom.vanilla.datapack.worldgen;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonReader;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.json.NamespaceTag;
import net.minestom.vanilla.datapack.json.Optional;

import java.io.IOException;
import java.util.List;

public sealed interface ConfiguredFeature {
    default NamespaceID type() {
        return JsonUtils.getNamespaceTag(this.getClass());
    }
    static ConfiguredFeature fromJson(JsonReader reader) throws IOException {
        return JsonUtils.sealedUnionNamespace(reader, ConfiguredFeature.class, "type");
    }

    @NamespaceTag("bamboo")
    record Bamboo(Config config) implements ConfiguredFeature {

        /**
         * @param probability The probability for a podzol disk to generate below the bamboo. The disk has a radius of 1 to 4 blocks. Value between 0.0 and 1.0 (inclusive).
         */
        public record Config(float probability) {
        }
    }

    @NamespaceTag("basalt_columns")
    record BasaltColumns(Config config) implements ConfiguredFeature {

        /**
         * @param reach The max radius of a column in this column cluster. Value between 0 and 3 (inclusive).
         * @param height The max height is height + 1. Value between 1 and 10 (inclusive).
         */
        public record Config(IntProvider reach, IntProvider height) {
        }
    }

    @NamespaceTag("basalt_pillar")
    record BasaltPillar() implements ConfiguredFeature {
    }

    @NamespaceTag("block_blob")
    record BlockColumn(Config config) implements ConfiguredFeature {

        /**
         * @param direction The direction of the column. One of up, down, north, east, south, or west.
         * @param allowed_placement A block predicate. The predicate must be passed to generate this feature.
         * @param prioritize_tip Determines where to cut off blocks when space is restricted. If true, will start removing layers from the start of the column.
         * @param layers The layers of this column.
         */
        public record Config(String direction, BlockPredicate allowed_placement, boolean prioritize_tip, List<Layer> layers) {
        }

        /**
         * @param height Specifying the height of the layer. Must be a non-negative int.
         * @param provider The block to use for this layer.
         */
        public record Layer(IntProvider height, BlockStateProvider provider) {
        }
    }

    @NamespaceTag("block_pile")
    record BlockPile(Config config) implements ConfiguredFeature {

        /**
         * @param state_provider The block to use.
         */
        public record Config(BlockStateProvider state_provider) {
        }
    }

    @NamespaceTag("blue_ice")
    record BlueIce() implements ConfiguredFeature {
    }

    @NamespaceTag("bonus_chest")
    record BonusChest() implements ConfiguredFeature {
    }

    @NamespaceTag("chorus_plant")
    record ChorusPlant() implements ConfiguredFeature {
    }

    @NamespaceTag("coral_claw")
    record CoralClaw() implements ConfiguredFeature {
    }

    @NamespaceTag("coral_mushroom")
    record CoralMushroom() implements ConfiguredFeature {
    }

    @NamespaceTag("coral_tree")
    record CoralTree() implements ConfiguredFeature {
    }

    @NamespaceTag("delta_feature")
    record DeltaFeature(Config config) implements ConfiguredFeature {

        /**
         * @param contents The block to use on the inside of the delta.
         * @param rim The block to use for the rim of the delta.
         * @param size The size of the inside of the delta. Value between 0 and 16 (inclusive).
         * @param rim_size The size of the rim of the delta. Value between 0 and 16 (inclusive).
         */
        public record Config(BlockState contents, BlockState rim, IntProvider size, IntProvider rim_size) {
        }
    }

    @NamespaceTag("desert_well")
    record DesertWell() implements ConfiguredFeature {
    }

    @NamespaceTag("disk")
    record Disk(Config config) implements ConfiguredFeature {

        /**
         * @param state_provider The block to use.
         * @param radius The radius of this disk. Value between 0 and 8 (inclusive).
         * @param half_height Half of the height of this disk. Value between 0 and 4 (inclusive).
         * @param target This predicate must be passed to generate this feature.
         */
        public record Config(StateProvider state_provider, IntProvider radius, int half_height, BlockPredicate target) {
        }

        /**
         * @param fallback The block to use when all the rules' predicates are not passed.
         * @param rules Rules of the block to use.
         */
        public record StateProvider(BlockStateProvider fallback, List<Rule> rules) {
        }

        /**
         * @param if_true The predicate of this rule.
         * @param then The block to use when the predicate is passed.
         */
        public record Rule(BlockPredicate if_true, BlockStateProvider then) {
        }
    }

    @NamespaceTag("dripstone_cluster")
    record DripstoneCluster(Config config) implements ConfiguredFeature {

        /**
         * @param floor_to_ceiling_search_range For how many blocks the feature will search for the floor or ceiling. Value between 1 and 512 (inclusive).
         * @param height The height of the cluster. Value between 1 and 128 (inclusive).
         * @param radius The radius of the cluster. Value between 1 and 128 (inclusive).
         * @param max_stalagmite_stalactite_height_diff The maximum height difference between stalagmites and stalactites. Value between 0 and 64 (inclusive).
         * @param height_deviation The height deviation. Value between 1 and 64 (inclusive).
         * @param dripstone_block_layer_thickness The dripstone block layer's thickness. Value between 0 and 128 (inclusive).
         * @param density Value between 0.0 and 2.0 (inclusive).
         * @param wetness Value between 0.0 and 2.0 (inclusive).
         * @param chance_of_dripstone_column_at_max_distance_from_center Value between 0.0 and 1.0 (inclusive).
         * @param max_distance_from_edge_affecting_chance_of_dripstone_column Value between 1 and 64 (inclusive).
         * @param max_distance_from_center_affecting_height_bias Value between 1 and 64 (inclusive).
         */
        public record Config(int floor_to_ceiling_search_range, IntProvider height, IntProvider radius, int max_stalagmite_stalactite_height_diff, int height_deviation, IntProvider dripstone_block_layer_thickness, FloatProvider density, FloatProvider wetness, float chance_of_dripstone_column_at_max_distance_from_center, int max_distance_from_edge_affecting_chance_of_dripstone_column, int max_distance_from_center_affecting_height_bias) {
        }
    }

    @NamespaceTag("end_gateway")
    record EndGateway(Config config) implements ConfiguredFeature {

        /**
         * @param exact Whether the gateway should teleport entities in the exact exit position.
         * @param exit The block position where the gateway should exit.
         */
        public record Config(boolean exact, IntList exit) {
        }
    }

    @NamespaceTag("end_island")
    record EndIsland() implements ConfiguredFeature {
    }

    @NamespaceTag("end_spike")
    record EndSpike(Config config) implements ConfiguredFeature {

        /**
         * @param crystal_invulnerable Whether the end crystals on it are invulnerable.
         * @param crystal_beam_target Block position of the beam target.
         * @param spikes Configurations of each spike.
         */
        public record Config(@Optional Boolean crystal_invulnerable, IntList crystal_beam_target, List<Spike> spikes) {
        }

        /**
         * @param centerX The X coordinate.
         * @param centerZ The Z coordinate.
         * @param radius The radius of the spike.
         * @param height The height of the spike.
         * @param guarded Whether to generate an iron bar cage around the crystal.
         */
        public record Spike(@Optional Integer centerX, @Optional Integer centerZ, @Optional Integer radius, @Optional Integer height, @Optional Boolean guarded) {
        }
    }

    @NamespaceTag("fill_layer")
    record FillLayer(Config config) implements ConfiguredFeature {

        /**
         * @param state The block to fill with.
         * @param height The layer to fill, starting at the bottom of the world. Value between 0 and 4064 (inclusive).
         */
        public record Config(BlockState state, int height) {
        }
    }

    @NamespaceTag("flower")
    record Flower(Config config) implements ConfiguredFeature {

        /**
         * @param tries The number of attempts to generate. Must be a positive integer.
         * @param xz_spread The horizontal spread range. Must be a non-negative integer.
         * @param y_spread the vertical spread range. Must be a non-negative integer.
         * @param feature The placed feature that the this patch generates.
         */
        public record Config(@Optional Integer tries, @Optional Integer xz_spread, @Optional Integer y_spread, PlacedFeature feature) {
        }
    }

    @NamespaceTag("forest_rock")
    record ForestRock(Config config) implements ConfiguredFeature {

        /**
         * @param state The block to use.
         */
        public record Config(BlockState state) {
        }
    }

    @NamespaceTag("fossil")
    record Fossil(Config config) implements ConfiguredFeature {

        /**
         * @param fossil_structures A list of fossil structure templates to choose from.
         * @param overlay_structures A list of overlay structure templates to choose from. Has to have the same length as  fossil_structures.
         * @param fossil_processors The processor for fossil structure templates.
         * @param overlay_processors The processor for overlay structure templates.
         * @param max_empty_corners_allowed How many corners of the structure are allowed to be empty for it to generate. Prevents structures floating in the air.
         */
        public record Config(List<StructureTemplate> fossil_structures, List<StructureTemplate> overlay_structures, ProcessorList fossil_processors, ProcessorList overlay_processors, int max_empty_corners_allowed) {
        }
    }

    @NamespaceTag("freeze_top_layer")
    record FreezeTopLayer() implements ConfiguredFeature {
    }

    @NamespaceTag("geode")
    record Geode(Config config) implements ConfiguredFeature {

        /**
         * @param blocks The blocks to use.
         * @param layers The max radius of each layer. Higher value results in higher max radius of each layer.
         * @param crack The configuration of the crack on the geode.
         * @param noise_multiplier Value between 0.0 and 1.0 (inclusive).
         * @param use_potential_placements_chance The probability for placing the inner placement on a block of inner layer. Value between 0 and 1 (inclusive).
         * @param use_alternate_layer0_chance The probability of the alternate blocks on inner layer. Value between 0 and 1 (inclusive).
         * @param placements_require_layer0_alternate Whether the inner placements are only allowed on the alternate inner blocks.
         * @param outer_wall_distance The offset on each coordinate of the center from the feature start. Value between 1 and 20 (inclusive).
         * @param distribution_points Value between 1 and 20 (inclusive).
         * @param invalid_blocks_threshold Check distribution_points times near the center of the geode, and if the number of invalid blocks found exceeds this number, the feature will not be generated.
         * @param point_offset Value between 1 and 10.
         * @param min_gen_offset The minimum Chebyshev distance between the block and the center.
         * @param max_gen_offset The maximum Chebyshev distance between the block and the center.
         */
        public record Config(Blocks blocks, Layers layers, Crack crack, @Optional Double noise_multiplier, @Optional Double use_potential_placements_chance, @Optional Double use_alternate_layer0_chance, @Optional Boolean placements_require_layer0_alternate, @Optional IntProvider outer_wall_distance, @Optional IntProvider distribution_points, int invalid_blocks_threshold, @Optional IntProvider point_offset, @Optional Integer min_gen_offset, @Optional Integer max_gen_offset) {
        }

        /**
         * @param filling_provider The block used for filling.
         * @param inner_layer_provider The block of the inner layer.
         * @param alternate_inner_layer_provider The alternate block of the inner layer.
         * @param middle_layer_provider The block of the middle layer.
         * @param outer_layer_provider The block of the outer layer.
         * @param inner_placements The blocks to place in the geode.
         * @param cannot_replace A block tag with # listing which blocks not to replace.
         * @param invalid_blocks A block tag with # listing invalid blocks. Due to MC-264886, any value is treated as #minecraftgeode_invalid_blocks. Additionally, air is an invalid block.
         */
        public record Blocks(BlockStateProvider filling_provider, BlockStateProvider inner_layer_provider, BlockStateProvider alternate_inner_layer_provider, BlockStateProvider middle_layer_provider, BlockStateProvider outer_layer_provider, List<BlockState> inner_placements, String cannot_replace, String invalid_blocks) {
        }

        /**
         * @param filling (optional, defaults to 1.7) Value between 0.01 and 50 (inclusive).
         * @param inner_layer (optional, defaults to 2.2) Value between 0.01 and 50 (inclusive).
         * @param middle_layer (optional, defaults to 3.2) Value between 0.01 and 50 (inclusive).
         * @param outer_layer (optional, defaults to 4.2) Value between 0.01 and 50 (inclusive).
         */
        public record Layers(@Optional Double filling, @Optional Double inner_layer, @Optional Double middle_layer, @Optional Double outer_layer) {
        }

        /**
         * @param generate_crack_chance (optional, defaults to 1.0) The probability for generating crack. Value between 0.0 and 1.0 (inclusive).
         * @param base_crack_size (optional, defaults to 2) Value between 0.0 and 5.0 (inclusive).
         * @param crack_point_offset (optional,defaults to 2) Value between 0 and 10 (inclusive).
         */
        public record Crack(@Optional Double generate_crack_chance, @Optional Double base_crack_size, @Optional Integer crack_point_offset) {
        }
    }

    @NamespaceTag("glowstone_blob")
    record GlowstoneBlob() implements ConfiguredFeature {
    }

    @NamespaceTag("huge_brown_mushroom")
    record HugeBrownMushroom(Config config) implements ConfiguredFeature {

        /**
         * @param cap_provider The block to use for the cap.
         * @param stem_provider The block to use for the stem.
         * @param foliage_radius The size of the cap.
         */
        public record Config(BlockStateProvider cap_provider, BlockStateProvider stem_provider, @Optional Integer foliage_radius) {
        }
    }

    @NamespaceTag("huge_fungus")
    record HugeFungus(Config config) implements ConfiguredFeature {

        /**
         * @param hat_state The block to use for the hat.
         * @param decor_state The block to use as decoration.
         * @param stem_state The block to use for the stem.
         * @param valid_base_block The block to place this feature on.
         * @param planted Whether this huge fungus is planted. If false, it can't exceed the world ceiling, can replace blocks whose material is plant, and doesn't drop items when replaced by other blocks.
         * @param replaceable_blocks The predicate must pass to be replaced with this feature.
         */
        public record Config(BlockState hat_state, BlockState decor_state, BlockState stem_state, BlockState valid_base_block, @Optional Boolean planted, BlockPredicate replaceable_blocks) {
        }
    }

    @NamespaceTag("huge_red_mushroom")
    record HugeRedMushroom(Config config) implements ConfiguredFeature {

        /**
         * @param cap_provider The block to use for the cap.
         * @param stem_provider The block to use for the stem.
         * @param foliage_radius The size of the cap.
         */
        public record Config(BlockStateProvider cap_provider, BlockStateProvider stem_provider, @Optional Integer foliage_radius) {
        }
    }

    @NamespaceTag("iceberg")
    record Iceberg(Config config) implements ConfiguredFeature {

        /**
         * @param state The block to use.
         */
        public record Config(BlockState state) {
        }
    }

    @NamespaceTag("ice_patch")
    record IceSpike() implements ConfiguredFeature {
    }

    @NamespaceTag("kelp")
    record Kelp() implements ConfiguredFeature {
    }

    @NamespaceTag("lake")
    record Lake(Config config) implements ConfiguredFeature {

        /**
         * @param fluid The block to use for the fluid of the lake.
         * @param barrier The block to use for the barrier of the lake.
         */
        public record Config(BlockStateProvider fluid, BlockStateProvider barrier) {
        }
    }

    @NamespaceTag("large_dripstone")
    record LargeDripstone(Config config) implements ConfiguredFeature {

        /**
         * @param floor_to_ceiling_search_range The search range form start point to cave floor or ceiling (rather than from floor to ceiling). Value between 1 and 512 (inclusive).
         * @param column_radius Used to provider a min and max value for radius. Note that this int provider doesn't provider a single int, but provider the min and max value of the specified distribution. Value between 1 and 60 (inclusive). See [https//www.desmos.com/calculator/8epce7fyjr the graph for details].
         * @param height_scale Higher value leads to higher height. Value between 0.0 and 20.0 (inclusive).
         * @param max_column_radius_to_cave_height_ratio The ratio of the max radius to the height of the cave. Value between 0.0 and 1.0 (inclusive).
         * @param stalactite_bluntness Trancate the tip of stalactite. Higher value leads to lower height. Value between 0.1 and 10.0 (inclusive).
         * @param stalagmite_bluntness Trancate the tip of stalagmite. Higher value leads to lower height. Value between 0.1 and 10.0 (inclusive).
         * @param wind_speed Larger value results in larger inclination. Value between 0.0 and 2.0 (inclusive).
         * @param min_radius_for_wind The min column radius to used the wind. Value between 0 and 100.
         * @param min_bluntness_for_wind The min value of the bluntnesses to used the wind. Value between 0.0 and 5.0 (inclusive).
         */
        public record Config(@Optional Integer floor_to_ceiling_search_range, IntProvider column_radius, FloatProvider height_scale, float max_column_radius_to_cave_height_ratio, FloatProvider stalactite_bluntness, FloatProvider stalagmite_bluntness, FloatProvider wind_speed, int min_radius_for_wind, float min_bluntness_for_wind) {
        }
    }

    @NamespaceTag("monster_room")
    record MonsterRoom() implements ConfiguredFeature {
    }

    @NamespaceTag("multiface_growth")
    record MultifaceGrowth(Config config) implements ConfiguredFeature {

        /**
         * @param block The block to place, currently must be glow_lichen or sculk_vein.
         * @param search_range Value between 1 and 64 (inclusive).
         * @param chance_of_spreading Value between 0.0 and 1.0 (inclusive).
         * @param can_place_on_floor
         * @param can_place_on_ceiling
         * @param can_place_on_wall
         * @param can_be_placed_on Can be a block ID or a block tag, or a list of block IDs.
         */
        public record Config(@Optional String block, @Optional Integer search_range, @Optional Float chance_of_spreading, @Optional Boolean can_place_on_floor, @Optional Boolean can_place_on_ceiling, @Optional Boolean can_place_on_wall, @Optional JsonUtils.SingleOrList<String> can_be_placed_on) {
        }
    }

    @NamespaceTag("nether_forest_vegetation")
    record NetherForestVegetation(Config config) implements ConfiguredFeature {

        /**
         * @param state_provider The block to use.
         * @param spread_width The horizonal distance to spread to. The max width is spread_width * 2 -1. Must be a positive integer.
         * @param spread_height The vertical distance to spread. The max height is spread_height * 2 -1. Must be a positive integer.
         */
        public record Config(BlockStateProvider state_provider, int spread_width, int spread_height) {
        }
    }

    @NamespaceTag("netherrack_replace_blobs")
    record NetherrackReplaceBlobs(Config config) implements ConfiguredFeature {

        /**
         * @param state The block to use.
         * @param target The block to replace.
         * @param radius Value between 0 and 12 (inclusive).
         */
        public record Config(BlockState state, BlockState target, IntProvider radius) {
        }
    }

    @NamespaceTag("no_bonemeal_flower")
    record NoBonemealFlower(Config config) implements ConfiguredFeature {

        /**
         * @param tries The number of attempts to generate. Must be a positive integer.
         * @param xz_spread The horizontal spread range. Must be a non-negative integer.
         * @param y_spread the vertical spread range. Must be a non-negative integer.
         * @param feature The placed feature that the patch generates.
         */
        public record Config(@Optional Integer tries, @Optional Integer xz_spread, @Optional Integer y_spread, PlacedFeature feature) {
        }
    }

    @NamespaceTag("no_op")
    record NoOp() implements ConfiguredFeature {
    }

    @NamespaceTag("ore")
    record Ore(Config config) implements ConfiguredFeature {

        /**
         * @param size Value between 0 and 64 (inclusive). Determine the volume size of the ore. See also Ore_(feature).
         * @param discard_chance_on_air_exposure Value between 0 and 1 (inclusive). The chance for an ore block to be discarded when it is exposed to air. Setting this to 1 makes the ore completely hidden.
         * @param targets A list of targets.
         */
        public record Config(int size, float discard_chance_on_air_exposure, List<Target> targets) {
        }

        /**
         * @param target Rule test. The blocks to replace.
         * @param state The block to use.
         */
        public record Target(RuleTest target, BlockState state) {
        }
    }

    @NamespaceTag("pointed_dripstone")
    record PointedDripstone(Config config) implements ConfiguredFeature {

        /**
         * @param chance_of_taller_dripstone (optional, defaults to 0.2) Value between 0.0 and 1.0 (inclusive). Probability for double-block dripstone.
         * @param chance_of_directional_spread (optional, defaults to 0.7) Value between 0.0 and 1.0 (inclusive). Probability that the dripstone spreads in a horizontal direction.
         * @param chance_of_spread_radius2 (optional, defaults to 0.5) Value between 0.0 and 1.0 (inclusive). Probability of horizontal spread by two blocks.
         * @param chance_of_spread_radius3 (optional. defaults to 0.5) Value between 0.0 and 1.0 (inclusive). After the spread by two blocks, probability of spreading the third block.
         */
        public record Config(@Optional Float chance_of_taller_dripstone, @Optional Float chance_of_directional_spread, @Optional Float chance_of_spread_radius2, @Optional Float chance_of_spread_radius3) {
        }
    }

    @NamespaceTag("random_boolean_selector")
    record RandomBooleanSelector(Config config) implements ConfiguredFeature {

        /**
         * @param feature_false placed feature (referenced by ID or inlined)
         * @param feature_true placed feature (referenced by ID or inlined)
         */
        public record Config(PlacedFeature feature_false, PlacedFeature feature_true) {
        }
    }

    @NamespaceTag("random_selector")
    record RandomSelector(Config config) implements ConfiguredFeature {

        /**
         * @param features A list of placed features from which to randomly choose. Try in order from the first, generate the placed feature or try the next one according to the chance.
         * @param _default Used if none of the above features are chosen.
         */
        public record Config(List<FeatureAndChance> features, @Json(name="default") PlacedFeature _default) {
        }

        /**
         * @param feature placed feature (referenced by ID or inlined)
         * @param chance The chance of this feature being chosen.
         */
        public record FeatureAndChance(PlacedFeature feature, float chance) {
        }
    }

    @NamespaceTag("random_patch")
    record RandomPatch(Config config) implements ConfiguredFeature {

        /**
         * @param tries The number of attempts to generate. Must be a positive integer.
         * @param xz_spread The horizontal spread range. Must be a non-negative integer.
         * @param y_spread the vertical spread range. Must be a non-negative integer.
         * @param feature The placed feature that the this patch generates.
         */
        public record Config(@Optional Integer tries, @Optional Integer xz_spread, @Optional Integer y_spread, PlacedFeature feature) {
        }
    }

    @NamespaceTag("replace_single_block")
    record ReplaceSingleBlock(Config config) implements ConfiguredFeature {

        /**
         * @param targets A list of targets.
         *
         */
        public record Config(List<Target> targets) {
        }

        /**
         * @param target Rule test. The blocks to replace.
         * @param state The block to use.
         */
        public record Target(RuleTest target, BlockState state) {
        }
    }

    @NamespaceTag("root_system")
    record RootSystem(Config config) implements ConfiguredFeature {

        /**
         * @param required_vertical_space_for_tree Value between 1 and 64 (inclusive).
         * @param root_radius Value between 1 and 64 (inclusive).
         * @param root_placement_attempts Value between 1 and 256 (inclusive).
         * @param root_column_max_height Value between 1 and 4096 (inclusive).
         * @param hanging_root_radius Value between 1 and 64 (inclusive).
         * @param hanging_roots_vertical_span Value between 0 and 16 (inclusive).
         * @param hanging_root_placement_attempts Value between 1 and 256 (inclusive).
         * @param allowed_vertical_water_for_tree Value between 1 and 64 (inclusive).
         * @param root_replaceable A block tag with # specifying which blocks can be replaced by the root column.
         * @param root_state_provider The block to use for the root column.
         * @param hanging_root_state_provider The block to use hanging below the root column.
         * @param allowed_tree_position The block predicate used to check if the tree position is valid.
         * @param feature The placed feature to place on top of the root system. Can be an ID of a placed feature, or a placed feature object.
         */
        public record Config(int required_vertical_space_for_tree, int root_radius, int root_placement_attempts, int root_column_max_height, int hanging_root_radius, int hanging_roots_vertical_span, int hanging_root_placement_attempts, int allowed_vertical_water_for_tree, String root_replaceable, BlockStateProvider root_state_provider, BlockStateProvider hanging_root_state_provider, BlockPredicate allowed_tree_position, PlacedFeature feature) {
        }
    }

    @NamespaceTag("scattered_ore")
    record ScatteredOre(Config config) implements ConfiguredFeature {

        /**
         * @param size Value between 0 and 64 (inclusive). Determine the volume size of the ore. See also <a href="https://minecraft.wiki/w/Ore_(feature)">Ore_(feature)</a>.
         * @param discard_chance_on_air_exposure Value between 0 and 1 (inclusive). The chance for an ore block to be discarded when it is exposed to air. Setting this to 1 makes the ore completely hidden.
         * @param targets A list of targets.
         */
        public record Config(int size, float discard_chance_on_air_exposure, List<Target> targets) {
        }

        /**
         * @param target Rule test. The blocks to replace.
         * @param state The block to use.
         */
        public record Target(RuleTest target, BlockState state) {
        }
    }

    @NamespaceTag("sculk_patch")
    record SculkPatch(Config config) implements ConfiguredFeature {

        /**
         * @param charge_count The number of charges. Value between 1 and 32 (inclusive).
         * @param amount_per_charge The initial value of each charge. Value between 1 and 500 (inclusive).
         * @param spread_attempts The number of attempts to spread. Value between 1 and 64 (inclusive).
         * @param growth_rounds The number of times to generate. Value between 0 and 8 (inclusive).
         * @param spread_rounds The number of times to spread. Value between 0 and 8 (inclusive).
         * @param extra_rare_growths The number of extra shriekers generated.
         * @param catalyst_chance The probability of generating a catalyst. Value between 0.0 and 1.0 (inclusive).
         */
        public record Config(int charge_count, int amount_per_charge, int spread_attempts, int growth_rounds, int spread_rounds, IntProvider extra_rare_growths, float catalyst_chance) {
        }
    }

    @NamespaceTag("seagrass")
    record Seagrass(Config config) implements ConfiguredFeature {

        /**
         * @param probability Value between 0.0 and 1.0 (inclusive). Probability of using tall seagrass instead of seagrass
         */
        public record Config(float probability) {
        }
    }

    @NamespaceTag("sea_pickle")
    record SeaPickle(Config config) implements ConfiguredFeature {

        /**
         * @param count Value between 0 and 256 (inclusive). The max count of the sea pickle block (not single sea pickle).
         */
        public record Config(IntProvider count) {
        }
    }

    @NamespaceTag("simple_block")
    record SimpleBlock(Config config) implements ConfiguredFeature {

        /**
         * @param to_place The block to use.
         */
        public record Config(BlockStateProvider to_place) {
        }
    }

    @NamespaceTag("simple_random_selector")
    record SimpleRandomSelector(Config config) implements ConfiguredFeature {

        /**
         * @param features Features to choose from.
         */
        public record Config(JsonUtils.SingleOrList<PlacedFeature> features) {
        }
    }

    @NamespaceTag("spring_feature")
    record SpringFeature(Config config) implements ConfiguredFeature {

        /**
         * @param state The fluid to use.
         * @param rock_count The required number of blocks adjacent to the spring that belong to valid_blocks.
         * @param hole_count The required number of air blocks adjacent to the spring.
         * @param requires_block_below Whether the spring feature requires a block in  valid_blocks below the fluid.
         * @param valid_blocks Can be a block ID or a block tag, or a list of block IDs.
         */
        public record Config(BlockState state, @Optional Integer rock_count, @Optional Integer hole_count, @Optional Boolean requires_block_below, JsonUtils.SingleOrList<String> valid_blocks) {
        }
    }

    @NamespaceTag("tree")
    record Tree(Config config) implements ConfiguredFeature {

        /**
         * @param ignore_vines (optional, defaults to false) Allows the tree to generate even if there are vines blocking it.
         * @param force_dirt (optional, defaults to false) If true, places the dirt provider even when the block below the tree is a valid dirt-like block.
         * @param dirt_provider The block to place below the trunk. Places only if force_dirt is true, or if there is not a valid dirt-like block below the trunk.
         * @param trunk_provider The block to use for the trunk. Note that when the trunk placer is fancy_trunk_placer, the block must have axis property, such as logs.
         * @param foliage_provider The block to use for the foliage.
         * @param minimum_size Defines the width of the tree at different heights relative to the lowest trunk block, for the minimum size of the feature.
         * @param root_placer Controls how tree's roots are generated.
         * @param trunk_placer trunk_placer
         * @param foliage_placer foliage_placer
         * @param decorators Decorations to add to the tree apart from the trunk and leaves.
         */
        public record Config(@Optional Boolean ignore_vines, @Optional Boolean force_dirt, BlockStateProvider dirt_provider, BlockStateProvider trunk_provider, BlockStateProvider foliage_provider, MinimumSize minimum_size, RootPlacer root_placer, TrunkPlacer trunk_placer, FoliagePlacer foliage_placer, List<Decorator> decorators) {
        }

        public interface MinimumSize {
            /**
             * @return One of two_layers_feature_size or three_layers_feature_size
             */
            String type();

            /**
             * @param min_clipped_height (optional) Value between 0 and 80 (inclusive). If the possible height at this location is lower than trunk height, but greater or equal to this value, the tree generates anyway. If not specified, the tree won't generate as long as the possible height is lower than trunk height. If the possible height at this location is lower than this value, the tree cannot generate.
             * @param limit (optional, defaults to 1) Value between 0 and 81 (inclusive). At heights lower than this value, lower_size is used, otherwise upper_size.
             * @param lower_size (optional, defaults to 0) Value between 0 and 16 (inclusive). Minimum width of the tree at heights under limit.
             * @param upper_size (optional, defaults to 1) Value between 0 and 16 (inclusive). Minimum width of the tree at heights greater than or equals limit.
             */
            record TwoLayersFeatureSize(@Optional Integer min_clipped_height, @Optional Integer limit, @Optional Integer lower_size, @Optional Integer upper_size) implements MinimumSize {
                @Override
                public String type() {
                    return "two_layers_feature_size";
                }
            }

            /**
             * @param min_clipped_height (optional) Value between 0 and 80 (inclusive). If the possible height at this location is lower than trunk height, but greater or equal to this value, the tree generates anyway. If not specified, the tree won't generate as long as the possible height is lower than trunk height. If the possible height at this location is lower than this value, the tree cannot generate.
             * @param limit (optional, defaults to 1) Value between 0 and 80 (inclusive). At heights lower than this value, lower_size is used, otherwise upper_size or middle_size.
             * @param upper_limit (optional, defaults to 1) Value between 0 and 80 (inclusive). At heights between this and limit, middle_size is used. If the height is greater or equals, it uses upper_size.
             * @param lower_size (optional. defaults to 0) Value between 0 and 16 (inclusive). Minimum width of the tree at the lowest layer.
             * @param upper_size (optional, defaults to 1) Value between 0 and 16 (inclusive). Minimum width of the tree at the upper layer.
             * @param middle_size (optional, defaults to 1) Value between 0 and 16 (inclusive). Minimum width of the tree at the middle layer.
             */
            record ThreeLayersFeatureSize(@Optional Integer min_clipped_height, @Optional Integer limit, @Optional Integer upper_limit, @Optional Integer lower_size, @Optional Integer upper_size, @Optional Integer middle_size) implements MinimumSize {
                @Override
                public String type() {
                    return "three_layers_feature_size";
                }
            }
        }

        public interface RootPlacer {
            String type();

            /**
             * @return The block used as the root of the tree.
             */
            BlockStateProvider root_provider();

            /**
             * @return Offset perpendicular to the trunk.
             */
            IntProvider trunk_offset_y();

            /**
             * @return The blocks above the root.
             */
            @Optional AboveRootPlacement above_root_placement();

            /**
             * @param above_root_provider The block above the root.
             * @param above_root_placement_chance The probability of generating the block. Value between 0.0 and 1.0 (inclusive).
             */
            record AboveRootPlacement(BlockStateProvider above_root_provider, float above_root_placement_chance) {
            }

            /**
             * @param max_root_width Value between 1 and 12 (inclusive).
             * @param max_root_length Value between 1 and 64 (inclusive).
             * @param random_skew_chance Value between 0.0 and 1.0 (inclusive).
             * @param can_grow_through A block ID or a block tag, or a list of block IDs. Blocks that roots can grow through.
             * @param muddy_roots_in A block ID or a block tag, or a list of block IDs. Roots in it will turn into muddy root blocks.
             * @param muddy_roots_provider Blocks used as muddy roots.
             */
            record MangroveRootPlacer(BlockStateProvider root_provider, IntProvider trunk_offset_y, @Optional AboveRootPlacement above_root_placement, int max_root_width, int max_root_length, float random_skew_chance, JsonUtils.SingleOrList<String> can_grow_through, JsonUtils.SingleOrList<String> muddy_roots_in, BlockStateProvider muddy_roots_provider) implements RootPlacer {
                @Override
                public String type() {
                    return "mangrove_root_placer";
                }
            }
        }

        public interface TrunkPlacer {
            /**
             * @return One of straight_trunk_placer, forking_trunk_placer, giant_trunk_placer, mega_jungle_trunk_placer, dark_oak_trunk_placer, fancy_trunk_placer, bending_trunk_placer, upwards_branching_trunk_placer, or cherry_trunk_placer.
             */
            String type();

            /**
             * @return base_height Value between 0 and 32 (inclusive).
             */
            int base_height();

            /**
             * @return height_rand_a Value between 0 and 24 (inclusive).
             */
            int height_rand_a();

            /**
             * @return height_rand_b Value between 0 and 24 (inclusive).
             */
            int height_rand_b();

            /**
             * @param bend_length Value between 1 and 64 (inclusive).
             * @param min_height_for_leaves (optional, defaults to 1) Must be a positive integer.
             */
            record BendingTrunkPlacer(int base_height, int height_rand_a, int height_rand_b, IntProvider bend_length, int min_height_for_leaves) implements TrunkPlacer {
                @Override
                public String type() {
                    return "bending_trunk_placer";
                }
            }

            /**
             * @param extra_branch_steps The number of steps to generate extra branches. Must be a positive integer.
             * @param extra_branch_length Generates extra branch length. Must be a non-negative integer.
             * @param place_branch_per_log_probability The probability of each log producing a branch. Value between 0.0 and 1.0 (inclusive).
             * @param can_grow_through A block ID or a block tag, or a list of block IDs. Represents blocks that tree trunks can grow through.
             */
            record UpwardsBranchingTrunkPlacer(int base_height, int height_rand_a, int height_rand_b, IntProvider extra_branch_steps, IntProvider extra_branch_length, float place_branch_per_log_probability, JsonUtils.SingleOrList<String> can_grow_through) implements TrunkPlacer {
                @Override
                public String type() {
                    return "upwards_branching_trunk_placer";
                }
            }

            /**
             * @param branch_count Value between 1 and 3 (inclusive).
             * @param branch_horizontal_length Value between 2 and 16 (inclusive).
             * @param branch_start_offset_from_top A uniform int provider, which provides a number between two bounds with uniform distribution. Must between -16 and 0 (inclusive). And since it needs at least 2 blocks variation for the branch starts to fit both branches, max_inclusive must be at least min_inclusive + 1.
             * @param branch_end_offset_from_top Value between -16 and 16 (inclusive).
             */
            record CherryTrunkPlacer(int base_height, int height_rand_a, int height_rand_b, IntProvider branch_count, IntProvider branch_horizontal_length, IntProvider.Uniform branch_start_offset_from_top, IntProvider branch_end_offset_from_top) implements TrunkPlacer {
                @Override
                public String type() {
                    return "cherry_trunk_placer";
                }
            }
        }

        public interface FoliagePlacer {
            /**
             * @return One of blob_foliage_placer, spruce_foliage_placer, pine_foliage_placer, acacia_foliage_placer, bush_foliage_placer, fancy_foliage_placer, jungle_foliage_placer, mega_pine_foliage_placer, dark_oak_foliage_placer, random_spread_foliage_placer, or cherry_foliage_placer.
             */
            String type();

            /**
             * @return radius The radius of the foliage.
             */
            IntProvider radius();

            /**
             * @return offset The vertical offest from the top of trunk to the top of the foliage.[needs testing]
             */
            IntProvider offset();

            /**
             * @param height The foliage's height. Value between 0 and 16 (inclusive).
             */
            record BlobFoliagePlacer(IntProvider radius, IntProvider offset, int height) implements FoliagePlacer {
                @Override
                public String type() {
                    return "blob_foliage_placer";
                }
            }

            /**
             * @param height The foliage's height. Value between 0 and 16 (inclusive).
             */
            record BushFoliagePlacer(IntProvider radius, IntProvider offset, int height) implements FoliagePlacer {
                @Override
                public String type() {
                    return "bush_foliage_placer";
                }
            }

            /**
             * @param height The foliage's height. Value between 0 and 16 (inclusive).
             */
            record FancyFoliagePlacer(IntProvider radius, IntProvider offset, int height) implements FoliagePlacer {
                @Override
                public String type() {
                    return "fancy_foliage_placer";
                }
            }

            /**
             * @param height The foliage's height. Value between 0 and 16 (inclusive).
             */
            record JungleFoliagePlacer(IntProvider radius, IntProvider offset, int height) implements FoliagePlacer {
                @Override
                public String type() {
                    return "jungle_foliage_placer";
                }
            }

            /**
             * @param trunk_height Value between 0 and 24 (inclusive).
             */
            record SpruceFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider trunk_height) implements FoliagePlacer {
                @Override
                public String type() {
                    return "spruce_foliage_placer";
                }
            }

            /**
             * @param height Value between 0 and 24 (inclusive).
             */
            record PineFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height) implements FoliagePlacer {
                @Override
                public String type() {
                    return "pine_foliage_placer";
                }
            }

            /**
             * @param crown_height Value between 0 and 24 (inclusive).
             */
            record MegaPineFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider crown_height) implements FoliagePlacer {
                @Override
                public String type() {
                    return "mega_pine_foliage_placer";
                }
            }

            /**
             * @param foliage_height Value between 1 and 512 (inclusive).
             * @param leaf_placement_attempts Value between 0 and 256 (inclusive).
             */
            record RandomSpreadFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider foliage_height, int leaf_placement_attempts) implements FoliagePlacer {
                @Override
                public String type() {
                    return "random_spread_foliage_placer";
                }
            }

            /**
             * @param height Value between 4 and 16 (inclusive).
             * @param wide_bottom_layer_hole_chance Value between 0.0 and 1.0 (inclusive).
             * @param corner_hole_chance Value between 0.0 and 1.0 (inclusive).
             * @param hanging_leaves_chance Value between 0.0 and 1.0 (inclusive).
             * @param hanging_leaves_extension_chance Value between 0.0 and 1.0 (inclusive).
             */
            record CherryFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height, float wide_bottom_layer_hole_chance, float corner_hole_chance, float hanging_leaves_chance, float hanging_leaves_extension_chance) implements FoliagePlacer {
                @Override
                public String type() {
                    return "cherry_foliage_placer";
                }
            }
        }

        public interface Decorator {
            /**
             * @return The type of decoration to add. One of trunk_vine, leave_vine, cocoa, beehive, alter_ground, or attached_to_leaves.
             */
            String type();

            /**
             * @param probability Value between 0.0 and 1.0 (inclusive).
             */
            record LeaveVine(float probability) implements Decorator {
                @Override
                public String type() {
                    return "leave_vine";
                }
            }

            /**
             * @param probability Value between 0.0 and 1.0 (inclusive).
             */
            record Cocoa(float probability) implements Decorator {
                @Override
                public String type() {
                    return "cocoa";
                }
            }

            /**
             * @param probability Value between 0.0 and 1.0 (inclusive).
             */
            record Beehive(float probability) implements Decorator {
                @Override
                public String type() {
                    return "beehive";
                }
            }

            /**
             * @param provider The block to replace the ground with.
             */
            record AlterGround(BlockStateProvider provider) implements Decorator {
                @Override
                public String type() {
                    return "alter_ground";
                }
            }

            /**
             * @param probability Value between 0.0 and 1.0 (inclusive).
             * @param exclusion_radius_xz The minimum value of the horizontal distance between two decorations. Value between 0 and 16 (inclusive).
             * @param exclusion_radius_y The minimum value of the vertical distance between two decorations. Value between 0 and 16 (inclusive).
             * @param required_empty_blocks The number of empty blocks required by the decoration. Value between 0 and 16 (inclusive).
             * @param block_provider The block of the decoration.
             * @param directions (Cannot be empty) Directions to generate.
             */
            record AttachedToLeaves(float probability, int exclusion_radius_xz, int exclusion_radius_y, int required_empty_blocks, BlockStateProvider block_provider, List<String> directions) implements Decorator {
                @Override
                public String type() {
                    return "attached_to_leaves";
                }
            }
        }

        @Override
        public NamespaceID type() {
            return NamespaceID.from("tree");
        }
    }

    @NamespaceTag("twisting_vines")
    record TwistingVines(Config config) implements ConfiguredFeature {

        /**
         * @param spread_width The max spread width is spread_width * 2 + 1
         * @param spread_height The max spread height is spread_height * 2 + 1
         * @param max_height The max length is max_height * 2, and the min length is 1.
         */
        public record Config(int spread_width, int spread_height, int max_height) {
        }
    }

    @NamespaceTag("underwater_magma")
    record UnderwaterMagma(Config config) implements ConfiguredFeature {

        /**
         * @param floor_search_range Value between 0 and 512 (inclusive).
         * @param placement_radius_around_floor Value between 0 and 64 (inclusive).
         * @param placement_probability_per_valid_position Value between 0.0 and 1.0 (inclusive).
         */
        public record Config(int floor_search_range, int placement_radius_around_floor, float placement_probability_per_valid_position) {
        }
    }

    @NamespaceTag("vegetation_patch")
    record VegetationPatch(Config config) implements ConfiguredFeature {

        /**
         * @param surface The surface to place on. One of floor, or ceiling
         * @param depth Value between 1 and 128 (inclusive).
         * @param vertical_range Value between 1 and 256 (inclusive).
         * @param extra_bottom_block_chance Value between 0.0 and 1.0 (inclusive).
         * @param extra_edge_column_chance Value between 0.0 and 1.0 (inclusive).
         * @param vegetation_chance Value between 0.0 and 1.0 (inclusive). The chance that a vegetation feature will be placed, is evaluated for each block.
         * @param xz_radius The radius of the patch.
         * @param replaceable A block tag with # specifying what blocks this feature can replace.
         * @param ground_state The block to use for the ground of the patch.
         * @param vegetation_feature The placed feature to place on top of the patch. Can be a placed feature ID, or a placed feature object.
         */
        public record Config(String surface, IntProvider depth, int vertical_range, float extra_bottom_block_chance, float extra_edge_column_chance, float vegetation_chance, IntProvider xz_radius, String replaceable, BlockStateProvider ground_state, PlacedFeature vegetation_feature) {
        }
    }

    @NamespaceTag("vines")
    record Vines() implements ConfiguredFeature {
        @Override
        public NamespaceID type() {
            return NamespaceID.from("vines");
        }
    }

    @NamespaceTag("void_start_platform")
    record VoidStartPlatform() implements ConfiguredFeature {
    }

    @NamespaceTag("waterlogged_vegetation_patch")
    record WaterloggedVegetationPatch(Config config) implements ConfiguredFeature {

        /**
         * @param surface The surface to place on. One of floor, or ceiling
         * @param depth Value between 1 and 128 (inclusive).
         * @param vertical_range Value between 1 and 256 (inclusive).
         * @param extra_bottom_block_chance Value between 0.0 and 1.0 (inclusive).
         * @param extra_edge_column_chance Value between 0.0 and 1.0 (inclusive).
         * @param vegetation_chance Value between 0.0 and 1.0 (inclusive). The chance that a vegetation feature will be placed, is evaluated for each block.
         * @param xz_radius The radius of the patch.
         * @param replaceable A block tag with # specifying what blocks this feature can replace.
         * @param ground_state The block to use for the ground of the patch.
         * @param vegetation_feature The placed feature to place on top of the patch. Can be a placed feature ID, or a placed feature object.
         */
        public record Config(String surface, IntProvider depth, int vertical_range, float extra_bottom_block_chance, float extra_edge_column_chance, float vegetation_chance, IntProvider xz_radius, String replaceable, BlockStateProvider ground_state, PlacedFeature vegetation_feature) {
        }
    }

    @NamespaceTag("weeping_vines")
    record WeepingVines() implements ConfiguredFeature {
    }
}
