package net.minestom.vanilla.datapack.worldgen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.moshi.JsonReader;
import net.minestom.server.instance.block.Block;
import net.kyori.adventure.key.Key;
import net.minestom.server.utils.math.FloatRange;
import net.minestom.vanilla.datapack.json.JsonUtils;
import net.minestom.vanilla.datapack.worldgen.noise.NormalNoise;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;
import net.minestom.vanilla.datapack.worldgen.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public record NoiseSettings(
        int sea_level,
        boolean disable_mob_generation,
        boolean ore_veins_enabled,
        boolean aquifers_enabled,
        boolean legacy_random_source,
        BlockState default_block,
        BlockState default_fluid,
        List<SpawnTarget> spawn_target,
        Noise noise,
        NoiseRouter noise_router,
        SurfaceRule surface_rule
) {

    public static int cellHeight(NoiseSettings settings) {
        return settings.noise().size_vertical() << 2;
    }
    public static int cellWidth(NoiseSettings settings) {
        return settings.noise().size_horizontal() << 2;
    }
    public static double cellCountY(NoiseSettings settings) {
        return (double) settings.noise().height() / cellHeight(settings);
    }
    public static double minCellY(NoiseSettings settings) {
        return (double) settings.noise().min_y() / cellHeight(settings);
    }

    // Noise parameter for biome
    public record SpawnTarget(FloatRange temperature,
                              FloatRange humidity,
                              FloatRange continentalness,
                              FloatRange erosion,
                              FloatRange weirdness,
                              FloatRange depth,
                              float offset) {
    }

    public record Noise(int min_y,
                        int height,
                        int size_horizontal,
                        int size_vertical) {

        interface SlideSettings {
            double target();

            double size();

            double offset();

            static SlideSettings fromJson(Object obj) {
                if (obj instanceof String str)
                    return SlideSettings.fromJson(new Gson().fromJson(str, JsonObject.class));
                if (!(obj instanceof JsonObject root))
                    throw new IllegalStateException("Root is not a JsonObject");
                double target = Util.<Double>jsonElse(root, "target", 0.0, JsonElement::getAsDouble);
                double size = Util.<Double>jsonElse(root, "size", 0.0, JsonElement::getAsDouble);
                double offset = Util.<Double>jsonElse(root, "offset", 0.0, JsonElement::getAsDouble);
                return new SlideSettings() {
                    @Override
                    public double target() {
                        return target;
                    }

                    @Override
                    public double size() {
                        return size;
                    }

                    @Override
                    public double offset() {
                        return offset;
                    }
                };
            }

            static double apply(SlideSettings slide, double density, double y) {
                if (slide.size() <= 0) return density;
                double t = (y - slide.offset()) / slide.size();
                return Util.clampedLerp(slide.target(), density, t);
            }
        }
    }

    /**
     *  noise_router: Routes density functions to noise parameters used for world generation. Each field can be an ID of density function or a density function (can be in constant form or object form).
     *
     *      initial_density_without_jaggedness: Related to the generation of aquifer and surface rule. At a horizonal position, starting from the top of the world, the game searches from top to bottom with the precision of size_vertical*4 blocks. The first Y-level whose noise value greater than 25/64 is used as the initial terrain height for world generation. This height should be generally lower than the actual terrain height (determined by the final density).
     *      final_density: Determines where there is an air or a default block. If positive, returns default block which will can be replaced by the  surface_rule. Otherwise, an air where aquifers can generate.
     *      barrier: Affects whether to separate between aquifers and open areas in caves. Larger values leads to higher probability to separate.
     *      fluid_level_floodedness: Affects the probability of generating liquid in an cave for aquifer. The larger value leads to higher probability. The noise value greater than 1.0 is regarded as 1.0, and value less than -1.0 is regarded as -1.0.
     *      fluid_level_spread: Affects the height of the liquid surface at a horizonal position. Smaller value leads to higher probability for lower height.
     *      lava: Affects whether an aquifer here uses lava instead of water. The threshold is 0.3.
     *      vein_toggle: Affects ore vein type and vertical range. If the noise value is greater than 0.0, the vein will be a copper vein. If the noise value is less than or equal to 0.0, the vein will be an iron vein.
     *      vein_ridged: Controls which blocks are part of a vein. If greater than or equal to 0.0, the block will not be part of a vein. If less than 0.0, the block will be either the vein type's stone block, or possibly an ore block.
     *      vein_gap: Affects which blocks in a vein will be ore blocks. If greater than -0.3, and a random number is less than the absolute value of vein_toggle mapped from 0.4 - 0.6 to 0.1 - 0.3, with values outside of this range clamped, an ore block will be placed, with a 2% chance for the ore block to be a raw metal block. Otherwise, the ore type's stone block will be placed.
     *      temperature: The temperature values for biome placement. This field and the following five fields are used for biome placement.
     *      vegetation: The humidity values for biome placement.
     *      continents: The continentalness values for terrain generation and biome placement.
     *      erosion: The erosion values for terrain generation and biome placement.
     *      depth: The depth values for terrain generation and biome placement.
     *      ridges: The weirdness values for terrain generation and biome placement.
     */
    public record NoiseRouter(
            DensityFunction initial_density_without_jaggedness,
            DensityFunction final_density,
            DensityFunction barrier,
            DensityFunction fluid_level_floodedness,
            DensityFunction fluid_level_spread,
            DensityFunction lava,
            DensityFunction vein_toggle,
            DensityFunction vein_ridged,
            DensityFunction vein_gap,
            DensityFunction temperature,
            DensityFunction vegetation,
            DensityFunction continents,
            DensityFunction erosion,
            DensityFunction depth,
            DensityFunction ridges
    ) {
        static final Map<String, NormalNoise> noiseCache = Collections.synchronizedMap(new HashMap<>());

        public static NormalNoise instantiate(WorldgenRandom.Positional random, NormalNoise.Config params) {
            var randomKey = random.seedKey();
            var cacheMapKey = Objects.hash(randomKey[0], randomKey[1]) + "|" + params.hashCode();
            return noiseCache.computeIfAbsent(cacheMapKey, k -> new NormalNoise(random.fromSeed(params.hashCode()), params));
        }
    }


    /**
     * Surface rule
     *  type: Type of the surface rule, one of: bandlands [sic], block, condition, or sequence. See below of extra fields for each type.
     * If  type is bandlands [sic] (used in badlands), no extra fields.
     * If  type is blocks (places the specified block), extra fields are as follows:
     *  result_state: The block to use.
     * If  type is sequence (attempts to apply surface rules in order, and only the first successful surface rule is applied), extra fields are as follows:
     *  sequence: (Required, but can be empty) List of surface rules.
     *     : A surface rule.
     * If  type is condition (applies surface rules if the condition is met), extra fields are as follows:
     *  if_true: A surface rule condition.
     *  then_run: A surface rule.
     */
    public interface SurfaceRule {
        Key type();

        Pos2Block apply(Context context);

        interface Context extends VerticalAnchor.Context {
            Key biome();

            int minY();
            int maxY();

            int blockX();
            int blockY();
            int blockZ();

            WorldgenRandom random(String string);

            // misc surface details
            int stoneDepthAbove();
            int surfaceDepth();
            int waterHeight();
            int minSurfaceLevel();
            int stoneDepthBelow();
            double surfaceSecondary();
        }

        interface Pos2Block {
            @Nullable Block apply(int x, int y, int z);
        }

        static SurfaceRule fromJson(JsonReader reader) throws IOException {
            return JsonUtils.unionStringTypeAdapted(reader, "type", type -> switch (type) {
                case "minecraft:bandlands" -> Bandlands.class;
                case "minecraft:block" -> Blocks.class;
                case "minecraft:sequence" -> Sequence.class;
                case "minecraft:condition" -> Condition.class;
                default -> null;
            });
        }

        record Bandlands() implements SurfaceRule {
            @Override
            public Key type() {
                return Key.key("bandlands");
            }

            @Override
            public Pos2Block apply(Context context) {
                // TODO: implement
                throw new UnsupportedOperationException("Not implemented");
            }
        }

        record Blocks(BlockState result_state) implements SurfaceRule {
            @Override
            public Key type() {
                return Key.key("blocks");
            }

            @Override
            public Pos2Block apply(Context context) {
                return (x, y, z) -> result_state().toMinestom();
            }
        }

        record Sequence(List<SurfaceRule> sequence) implements SurfaceRule {
            @Override
            public Key type() {
                return Key.key("sequence");
            }

            @Override
            public Pos2Block apply(Context context) {
                List<Pos2Block> rulesWithContext = sequence().stream()
                        .map(rule -> rule.apply(context))
                        .toList();
                return (x, y, z) -> {
                    for (Pos2Block rule : rulesWithContext) {
                        Block result = rule.apply(x, y, z);
                        if (result != null) return result;
                    }
                    return null;
                };
            }
        }

        record Condition(SurfaceRuleCondition if_true, SurfaceRule then_run) implements SurfaceRule {
            @Override
            public Key type() {
                return Key.key("condition");
            }

            @Override
            public Pos2Block apply(Context context) {
                return (x, y, z) -> {
                    if (if_true().test(context)) {
                        return then_run().apply(context).apply(x, y, z);
                    }
                    return null;
                };
            }
        }

        /**
         * Surface rule condition
         *  type: Type of the surface rule, one of: biome, noise_threshold, vertical_gradient, y_above, water, temperature, steep, not, hole, above_preliminary_surface, or stone_depth. See below of extra fields for each type.
         * If  type is biome (test for the biome), extra fields are as follows:
         *  biome_is: (Required, but can be empty) List of biomes that result in true.
         *     : The ID of a biome.
         * If  type is noise_threshold (Success when the noise value at this XZ losction with Y=0 is within the specified closed interval), extra fields are as follows:
         *  noise: The ID of a noise.
         *  min_threshold: Min threshold of the closed interval.
         *  max_threshold: Max threshold of the closed interval.
         * If  type is vertical_gradient (Makes the block fade upwards. Between the specified y-coords is the gradient itself. For example the gradient between bedrock and deepslate, or between deepslate and stone), extra fields are as follows:
         *  random_name: A namespace ID used as the seed of the random. For example, the seed between bedrock and deepslate in the vanilla game is "minecraft:bedrock_floor", and the seed between deepslate and stone is "minecraft:deepslate".
         *  true_at_and_below: Always succcess if the y-coord is at or below this value.
         *     Choices for a vertical anchor (must choose only one of three)
         *  false_at_and_above: Always fails if the y-coord is at or above this value. The y-coords between the two value produces a gradient, and the probability of success in this gradient is (false_at_and_above - Y) / (false_at_and_above - true_at_and_below)
         *     Choices for a vertical anchor (must choose only one of three)
         * If  type is y_above (checks if it is above a XZ plane at the specified Y level. E.g. block whose Y coordinate is 0 is above Y=0 plane), extra fields are as follows:
         *  anchor: Y level.
         *     Choices for a vertical anchor (must choose only one of three)
         *  surface_depth_multiplier: Value between -20 and 20 (both inclusive). How much it is affected by the surface layer thickness. surfaceLayerThickness * surface_depth_multiplier will be added into anchor.
         *  add_stone_depth: Instead of current block's Y-level, checks the value of "current block's Y-level" plus "the number of non-liquid blocks between current block's downward surface and the lowest air block directly above". For example, if block at Y=2 is air, Y=1 is water, and Y=0 is stone, when applied at the stone, the number of non-liquid blocks between current block's downward surface (in this case, Y=0 plane) and the lowest air block directly above (in this case, air at Y=2) is 1 (that is, this stone itself).
         * If  type is water (Check whether the offset height of the current block relative to the liquid surface (the contact surface between air and liquid) above (always a negative integer less than -1) is greater than the specified value. Always success if there's no liquid between them. For example, if there is only one liquid block between current block and the air block above, the value to check is -2), extra fields are as follows:
         *  offset: The offset height relative to the liquid surface (the contact surface between air and liquid) above. If it is set to a value greater than -1, the condition is successful only if there is no liquid between current block and the lowest air block above. If it is set to -1, it works the same with values greater than -1 in terrain generation, and always successful in carver generation.
         *  surface_depth_multiplier: Value between -20 and 20 (both inclusive). How much it is affected by the surface layer thickness. surfaceLayerThickness * surface_depth_multiplier will be added into the offset.
         *  add_stone_depth: Instead of current block's Y-level, checks the value of "current block's Y-level" plus "the number of non-liquid blocks between current block's downward surface and the lowest air block directly above". For example, if block at Y=2 is air, Y=1 is water, and Y=0 is stone, when applied at the stone, the number of non-liquid blocks between current block's downward surface (in this case, Y=0 plane) and the lowest air block directly above (in this case, air at Y=2) is 1 (that is, this stone itself).
         * If  type is temperature (success when the height-adjusted temperature is low enough to snow. The height-adjusted temperature depends on the biome's temperature and temperature_modifier fields and the current Y-level), no extra fields.
         * If  type is steep (checks current position for steep slopes (with height difference of more than 4 blocks) that are back sun (north or east facing)), no extra fields.
         * If  type is not (inverts the condition), extra fields are as follows:
         *  invert: The condition to invert.
         *     Surface rule condition
         * If  type is hole (check whether the surface layer thickness at this horizonal location is less than 0), no extra fields.
         * If  type is above_preliminary_surface (checks whether it is higher than the preliminary surface. The preliminary surface height is the interpolated initial terrain height (determined by initial_density_without_jaggedness) minus 8 and then plus (surfaceLayerThickness - 8)), no extra fields.
         * If  type is stone_depth (checks whether the distance between the current position and the terrain surface or the cave surface is less than or equal to the specified offset value), extra fields are as follows:
         *  offset: The offset value.
         *  add_surface_depth: Whether to be affected by surface layer thickness. If true, the surface layer thickness will be addded into the offset.
         *  secondary_depth_range: How much it is affected by the noise minecraft:surface_secondary. niseValue × secondary_depth_range will be added into the offset.
         *  surface_type: Either floor or ceiling. If ceiling, checks the distance to the upper surface of cave below (technically, it is the distance to the nearest liquid or air block directly below). For example, if where Y=-1 is water, and where Y=0 is stone, when applied to the stone, the distance to the nearest liquid or air block directly below (in this case, the water at Y=-1) is 0. If it isfloor, checks the distance to the terrain surface or the lower surface of cave above (technically, it is the number of non-liquid blocks between current block and the lowest air block directly above. If there is liquid between current block and the air block above, this value may be less than the actual distance to the surface of terrain or cave). For example, where Y=2 is air, Y=1 is water, and Y=0 is stone, when applying this condition at the stone, the number of non-liquid blocks between current block and the lowest air block directly above (in this case, air at Y=2) is 0.
         */
        interface SurfaceRuleCondition {
            Key type();

            boolean test(SurfaceRule.Context context);

            static SurfaceRuleCondition fromJson(JsonReader reader) throws IOException {
                return JsonUtils.unionStringTypeAdapted(reader, "type", type -> switch (type) {
                    case "minecraft:biome" -> Biome.class;
                    case "minecraft:noise_threshold" -> NoiseThreshold.class;
                    case "minecraft:vertical_gradient" -> VerticalGradient.class;
                    case "minecraft:y_above" -> YAbove.class;
                    case "minecraft:water" -> Water.class;
                    case "minecraft:temperature" -> Temperature.class;
                    case "minecraft:steep" -> Steep.class;
                    case "minecraft:not" -> Not.class;
                    case "minecraft:hole" -> Hole.class;
                    case "minecraft:above_preliminary_surface" -> AbovePreliminarySurface.class;
                    case "minecraft:stone_depth" -> StoneDepth.class;
                    default -> null;
                });
            }

            record Biome(List<Key> biome_is) implements SurfaceRuleCondition {
                @Override
                public Key type() {
                    return Key.key("biome");
                }

                @Override
                public boolean test(SurfaceRule.Context context) {
                    return biome_is.contains(context.biome());
                }
            }

            record NoiseThreshold(Key noise, double min_threshold, double max_threshold) implements SurfaceRuleCondition {
                @Override
                public Key type() {
                    return Key.key("noise_threshold");
                }

                @Override
                public boolean test(SurfaceRule.Context context) {
                    // TODO: Implement this
                    throw new UnsupportedOperationException("Not implemented yet");
                }
            }

            record VerticalGradient(Key random_name, VerticalAnchor true_at_and_below, VerticalAnchor false_at_and_above) implements SurfaceRuleCondition {
                @Override
                public Key type() {
                    return Key.key("vertical_gradient");
                }

                @Override
                public boolean test(SurfaceRule.Context context) {
                    int trueAtAndBelowY = true_at_and_below().apply(context);
                    int falseAtAndAboveY = false_at_and_above().apply(context);
                    if (context.blockY() <= trueAtAndBelowY) {
                        return true;
                    }
                    if (context.blockY() >= falseAtAndAboveY) {
                        return false;
                    }
                    WorldgenRandom random = context.random(random_name().toString());
                    double chance = Util.map(context.blockY(), trueAtAndBelowY, falseAtAndAboveY, 1, 0);
                    return random.nextFloat() < chance;
                }
            }

            record YAbove(VerticalAnchor anchor, int surface_depth_multiplier, boolean add_stone_depth) implements SurfaceRuleCondition {
                @Override
                public Key type() {
                    return Key.key("y_above");
                }

                @Override
                public boolean test(SurfaceRule.Context context) {
                    int stoneDepth = add_stone_depth() ? context.stoneDepthAbove() : 0;
                    return context.blockY() + stoneDepth >= anchor.apply(context) + context.surfaceDepth() * surface_depth_multiplier();
                }
            }

            record Water(int offset, int surface_depth_multiplier, boolean add_stone_depth) implements SurfaceRuleCondition {
                @Override
                public Key type() {
                    return Key.key("water");
                }

                @Override
                public boolean test(SurfaceRule.Context context) {
                    if (context.waterHeight() == Integer.MIN_VALUE) {
                        return true;
                    }
                    int stoneDepth = add_stone_depth() ? context.stoneDepthAbove() : 0;
                    return context.blockY() + stoneDepth >= context.waterHeight() + offset() + context.surfaceDepth() * surface_depth_multiplier();
                }
            }

            record Temperature() implements SurfaceRuleCondition {
                @Override
                public Key type() {
                    return Key.key("temperature");
                }

                @Override
                public boolean test(SurfaceRule.Context context) {
                    // TODO: Implement this
                    throw new UnsupportedOperationException("Not implemented yet");
                }
            }

            record Steep() implements SurfaceRuleCondition {
                @Override
                public Key type() {
                    return Key.key("steep");
                }

                @Override
                public boolean test(SurfaceRule.Context context) {
                    // TODO: Implement this
                    throw new UnsupportedOperationException("Not implemented yet");
                }
            }

            record Not(SurfaceRuleCondition invert) implements SurfaceRuleCondition {
                @Override
                public Key type() {
                    return Key.key("not");
                }

                @Override
                public boolean test(SurfaceRule.Context context) {
                    return !invert.test(context);
                }
            }

            record Hole() implements SurfaceRuleCondition {
                @Override
                public Key type() {
                    return Key.key("hole");
                }

                @Override
                public boolean test(SurfaceRule.Context context) {
                    // TODO: Implement this
                    throw new UnsupportedOperationException("Not implemented yet");
                }
            }

            record AbovePreliminarySurface() implements SurfaceRuleCondition {
                @Override
                public Key type() {
                    return Key.key("above_preliminary_surface");
                }

                @Override
                public boolean test(SurfaceRule.Context context) {
                    return context.blockY() >= context.minSurfaceLevel();
                }
            }

            record StoneDepth(int offset, boolean add_surface_depth, int secondary_depth_range, SurfaceType surface_type) implements SurfaceRuleCondition {
                @Override
                public Key type() {
                    return Key.key("stone_depth");
                }

                @Override
                public boolean test(SurfaceRule.Context context) {
                    int depth = switch (surface_type()) {
                        case ceiling -> context.stoneDepthBelow();
                        case floor -> context.stoneDepthAbove();
                    };
                    int surfaceDepth = add_surface_depth() ? context.surfaceDepth() : 0;
                    int secondaryDepth = secondary_depth_range() == 0 ? 0 : (int) Util.map(context.surfaceSecondary(), -1, 1, 0, secondary_depth_range());
                    return depth <= 1 + offset + surfaceDepth + secondaryDepth;
                }

                enum SurfaceType {
                    floor,
                    ceiling
                }
            }
        }
    }
}
