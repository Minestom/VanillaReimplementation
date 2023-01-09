package net.minestom.vanilla.generation.noise;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.generation.NoiseChunkGenerator;
import net.minestom.vanilla.generation.WorldgenRegistries;
import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.random.WorldgenRandom;
import net.minestom.vanilla.generation.random.XoroshiroRandom;

import java.util.*;
import java.util.function.Function;

public class SurfaceSystem {
    private final Noise.Bounded surfaceNoise;
    private final Noise.Bounded surfaceSecondaryNoise;
    private final WorldgenRandom.Positional random;
    private final Map<String, WorldgenRandom> positionalRandoms;

    private final SurfaceRule rule;
    private final Block defaultBlock;

    public SurfaceSystem(SurfaceRule rule, Block defaultBlock, long seed) {
        this.random = XoroshiroRandom.create(seed).forkPositional();
        this.surfaceNoise = NoiseRouter.instantiate(this.random, WorldgenRegistries.SURFACE_NOISE);
        this.surfaceSecondaryNoise = NoiseRouter.instantiate(this.random, WorldgenRegistries.SURFACE_SECONDARY_NOISE);
        this.positionalRandoms = new HashMap<>();
        this.rule = rule;
        this.defaultBlock = defaultBlock;
    }

    public void buildSurface(NoiseChunkGenerator.TargetChunk chunk, NoiseChunk noiseChunk, VerticalAnchor.WorldgenContext worldgenContext, Function<Point, String> getBiome) {
        int minX = chunk.minX();
        int minZ = chunk.minZ();
        int minY = chunk.minY();
        int maxY = chunk.maxY();
        SurfaceContext surfaceContext = new SurfaceContext(this, chunk, noiseChunk, worldgenContext, getBiome);
        var ruleWithContext = this.rule.apply(surfaceContext);

        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x += 1) {
            int worldX = minX + x;
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z += 1) {
                int worldZ = minZ + z;
                surfaceContext.updateXZ(worldX, worldZ);
                int stoneDepthAbove = 0;
                int waterHeight = Integer.MIN_VALUE;
                int stoneDepthOffset = Integer.MAX_VALUE;

                for (int y = minY; y >= maxY; y -= 1) {
                    var worldPos = new Vec(worldX, y, worldZ);
                    var oldState = chunk.getBlock(worldPos);
                    if (oldState.compare(Block.AIR)) {
                        stoneDepthAbove = 0;
                        waterHeight = Integer.MIN_VALUE;
                        continue;
                    }
                    if (oldState.registry().isLiquid()) {
                        if (waterHeight == Integer.MIN_VALUE) {
                            waterHeight = y + 1;
                        }
                        continue;
                    }
                    if (stoneDepthOffset >= y) {
                        stoneDepthOffset = Integer.MIN_VALUE;
                        for (int i = y - 1; i >= minY; i -= 1) {
                            Block state = chunk.getBlock(new Vec(worldX, i, worldZ));
                            if (state.compare(Block.AIR) || state.registry().isLiquid()) {
                                stoneDepthOffset = i + 1;
                                break;
                            }
                        }
                    }
                    stoneDepthAbove += 1;
                    int stoneDepthBelow = y - stoneDepthOffset + 1;

                    if (!oldState.equals(this.defaultBlock)) {
                        continue;
                    }
                    surfaceContext.updateY(stoneDepthAbove, stoneDepthBelow, waterHeight, y);
                    var newState = ruleWithContext.apply(worldX, y, worldZ);
                    if (newState != null) {
                        chunk.setBlock(worldPos, newState);
                    }
                }
            }
        }
    }

    public int getSurfaceDepth(double x, double z) {
        double noise = this.surfaceNoise.sample(x, 0, z);
        double offset = this.random.at((int) x, 0, (int) z).nextDouble() * 0.25;
        return (int) (noise * 2.75 + 3 + offset);
    }

    public double getSurfaceSecondary(double x, double z) {
        return this.surfaceSecondaryNoise.sample(x, 0, z);
    }

    public WorldgenRandom getRandom(String name) {
        return positionalRandoms.computeIfAbsent(name, this.random::fromHashOf);
    }

    public interface SurfaceRule {
        Pos2Block apply(SurfaceContext context);

        interface Pos2Block {
            Block apply(int x, int y, int z);
        }

        SurfaceRule NOOP = context -> (x, y, z) -> null;

        static SurfaceRule fromJson(Object obj) {
            JsonObject root = Util.jsonObject(obj);
            String type = Util.jsonElse(root, "type", "", JsonElement::getAsString).replace("minecraft:", "");
            return switch (type) {
                case "block" -> block(Util.jsonRequire(root, "result_state", Util.jsonVanillaBlock()));
                case "sequence" ->
                        sequence(Util.jsonRequire(root, "sequence", Util.jsonReadArray(SurfaceRule::fromJson)));
                case "condition" ->
                        condition(SurfaceCondition.fromJson(Util.jsonRequire(root, "if_true", JsonElement::getAsJsonObject)),
                                fromJson(Util.jsonRequire(root, "then_run", JsonElement::getAsJsonObject)));
                default -> NOOP;
            };
        }

        static SurfaceRule block(Block state) {
            return context -> (x, y, z) -> state;
        }

        static SurfaceRule sequence(List<SurfaceRule> rules) {
            return context -> {
                List<SurfaceRule.Pos2Block> rulesWithContext = rules.stream()
                        .map(rule -> rule.apply(context))
                        .toList();
                return (x, y, z) -> {
                    for (SurfaceRule.Pos2Block rule : rulesWithContext) {
                        Block result = rule.apply(x, y, z);
                        if (result != null) return result;
                    }
                    return null;
                };
            };
        }

        static SurfaceRule condition(SurfaceCondition ifTrue, SurfaceRule thenRun) {
            return context -> (x, y, z) -> {
                if (ifTrue.test(context)) {
                    return thenRun.apply(context).apply(x, y, z);
                }
                return null;
            };
        }
    }

    public interface SurfaceCondition {

        boolean test(SurfaceContext context);

        SurfaceCondition FALSE = context -> false;
        SurfaceCondition TRUE = context -> true;

        static SurfaceCondition fromJson(Object obj) {
            JsonObject root = Util.jsonObject(obj);
            String type = Util.jsonElse(root, "type", "", JsonElement::getAsString).replace("minecraft:", "");
            return switch (type) {
                case "above_preliminary_surface" -> abovePreliminarySurface();
                case "biome" -> biome(Util.jsonRequire(root, "biome_is", Util.jsonReadArray(JsonElement::getAsString)));
                case "not" ->
                        SurfaceCondition.not(fromJson(Util.jsonRequire(root, "invert", JsonElement::getAsJsonObject)));
                case "stone_depth" -> stoneDepth(
                        Util.jsonElse(root, "offset", 0, JsonElement::getAsInt),
                        Util.jsonElse(root, "add_surface_depth", false, JsonElement::getAsBoolean),
                        Util.jsonElse(root, "secondary_depth_range", 0, JsonElement::getAsInt),
                        Util.jsonElse(root, "surface_type", "", JsonElement::getAsString).equals("ceiling")
                );
                case "vertical_gradient" -> verticalGradient(
                        Util.jsonElse(root, "random_name", "", JsonElement::getAsString),
                        VerticalAnchor.fromJson(Util.jsonRequire(root, "true_at_and_below", JsonElement::getAsJsonObject)),
                        VerticalAnchor.fromJson(Util.jsonRequire(root, "false_at_and_above", JsonElement::getAsJsonObject))
                );
                case "water" -> water(
                        Util.jsonElse(root, "offset", 0, JsonElement::getAsInt),
                        Util.jsonElse(root, "surface_depth_multiplier", 0, JsonElement::getAsInt),
                        Util.jsonElse(root, "add_surface_depth", false, JsonElement::getAsBoolean)
                );
                case "y_above" -> yAbove(
                        VerticalAnchor.fromJson(Util.jsonRequire(root, "anchor", JsonElement::getAsJsonObject)),
                        Util.jsonElse(root, "surface_depth_multiplier", 0, JsonElement::getAsInt),
                        Util.jsonElse(root, "add_surface_depth", false, JsonElement::getAsBoolean)
                );
                default -> FALSE;
            };
        }

        static SurfaceCondition abovePreliminarySurface() {
            return context -> context.blockY >= context.minSurfaceLevel.getAsInt();
        }

        static SurfaceCondition biome(List<String> biomes) {
            Set<String> biomeSet = new HashSet<>(biomes);
            return context -> biomeSet.contains(context.biome);
        }

        static SurfaceCondition not(SurfaceCondition invert) {
            return context -> !invert.test(context);
        }

        static SurfaceCondition stoneDepth(int offset, boolean addSurfaceDepth, int secondaryDepthRange, boolean ceiling) {
            return context -> {
                int depth = ceiling ? context.stoneDepthBelow : context.stoneDepthAbove;
                int surfaceDepth = addSurfaceDepth ? context.surfaceDepth : 0;
                int secondaryDepth = secondaryDepthRange == 0 ? 0 : (int) Util.map(context.surfaceSecondary.getAsInt(), -1, 1, 0, secondaryDepthRange);
                return depth <= 1 + offset + surfaceDepth + secondaryDepth;
            };
        }

        static SurfaceCondition verticalGradient(String randomName, VerticalAnchor trueAtAndBelow, VerticalAnchor falseAtAndAbove) {
            return context -> {
                int trueAtAndBelowY = trueAtAndBelow.apply(context.context);
                int falseAtAndAboveY = falseAtAndAbove.apply(context.context);
                if (context.blockY <= trueAtAndBelowY) {
                    return true;
                }
                if (context.blockY >= falseAtAndAboveY) {
                    return false;
                }
                WorldgenRandom random = context.system.getRandom(randomName);
                double chance = Util.map(context.blockY, trueAtAndBelowY, falseAtAndAboveY, 1, 0);
                return random.nextFloat() < chance;
            };
        }

        static SurfaceCondition water(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) {
            return context -> {
                if (context.waterHeight == Integer.MIN_VALUE) {
                    return true;
                }
                int stoneDepth = addStoneDepth ? context.stoneDepthAbove : 0;
                return context.blockY + stoneDepth >= context.waterHeight + offset + context.surfaceDepth * surfaceDepthMultiplier;
            };
        }

        static SurfaceCondition yAbove(VerticalAnchor anchor, int surfaceDepthMultiplier, boolean addStoneDepth) {
            return context -> {
                int stoneDepth = addStoneDepth ? context.stoneDepthAbove : 0;
                return context.blockY + stoneDepth >= anchor.apply(context.context) + context.surfaceDepth * surfaceDepthMultiplier;
            };
        }
    }
}