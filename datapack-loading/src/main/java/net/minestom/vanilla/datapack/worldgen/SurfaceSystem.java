package net.minestom.vanilla.datapack.worldgen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.worldgen.noise.NoiseChunk;
import net.minestom.vanilla.datapack.worldgen.noise.NormalNoise;
import net.minestom.vanilla.datapack.worldgen.noise.SurfaceContext;
import net.minestom.vanilla.datapack.worldgen.noise.VerticalAnchor;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;
import net.minestom.vanilla.datapack.worldgen.random.XoroshiroRandom;

import java.util.*;
import java.util.function.Function;

public class SurfaceSystem {
    private final NormalNoise surfaceNoise;
    private final NormalNoise surfaceSecondaryNoise;
    private final WorldgenRandom.Positional random;
    private final Map<String, WorldgenRandom> positionalRandoms;

    private final SurfaceRule rule;
    private final Block defaultBlock;

    public SurfaceSystem(SurfaceRule rule, Block defaultBlock, long seed) {
        this.random = XoroshiroRandom.create(seed).forkPositional();
        this.surfaceNoise = NoiseSettings.NoiseRouter.instantiate(this.random, WorldgenRegistries.SURFACE_NOISE);
        this.surfaceSecondaryNoise = NoiseSettings.NoiseRouter.instantiate(this.random, WorldgenRegistries.SURFACE_SECONDARY_NOISE);
        this.positionalRandoms = new HashMap<>();
        this.rule = rule;
        this.defaultBlock = defaultBlock;
    }

    public void buildSurface(NoiseChunkGenerator.TargetChunk chunk, NoiseChunk noiseChunk, VerticalAnchor.WorldgenContext worldgenContext, Function<Point, NamespaceID> getBiome) {
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

    //    public interface SurfaceRule = (context: SurfaceContext) => (x: number, y: number, z: number) => BlockState | undefined
    public interface SurfaceRule {
        Pos2Block apply(SurfaceContext context);

        interface Pos2Block {
            Block apply(int x, int y, int z);
        }

        SurfaceRule NOOP = context -> (x, y, z) -> null;

//        export function fromJson(obj: unknown): SurfaceRule {
//            const root = Json.readObject(obj) ?? {}
//            const type = Json.readString(root.type)?.replace(/^minecraft:/, '')
//            switch (type) {
//                case 'block': return block(BlockState.fromJson(root.result_state))
//                case 'sequence': return sequence(Json.readArray(root.sequence, SurfaceRule.fromJson) ?? [])
//                case 'condition': return condition(SurfaceCondition.fromJson(root.if_true), SurfaceRule.fromJson(root.then_run))
//            }
//            return NOOP
//        }

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

//        export function block(state: BlockState): SurfaceRule {
//            return () => () => state
//        }

        static SurfaceRule block(Block state) {
            return context -> (x, y, z) -> state;
        }

//        export function sequence(rules: SurfaceRule[]): SurfaceRule {
//            return context => {
//            const rulesWithContext = rules.map(rule => rule(context))
//                return (x, y, z) => {
//                    for (const rule of rulesWithContext) {
//            const result = rule(x, y, z)
//                        if (result) return result
//                    }
//                    return undefined
//                }
//            }
//        }

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
//        export function condition(ifTrue: SurfaceCondition, thenRun: SurfaceRule): SurfaceRule {
//            return context => (x, y, z) => {
//                if (ifTrue(context)) {
//                    return thenRun(context)(x, y, z)
//                }
//                return undefined
//            }
//        }

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

        //        export const FALSE = () => false
        SurfaceCondition FALSE = context -> false;
        //        export const TRUE = () => true
        SurfaceCondition TRUE = context -> true;

//        export function fromJson(obj: unknown): SurfaceCondition {
//        const root = Json.readObject(obj) ?? {}
//        const type = Json.readString(root.type)?.replace(/^minecraft:/, '')
//            switch (type) {
//                case 'above_preliminary_surface': return abovePreliminarySurface()
//                case 'biome': return biome(
//                        Json.readArray(root.biome_is, e => Json.readString(e) ?? '') ?? []
//        )
//                case 'not': return not(SurfaceCondition.fromJson(root.invert))
//                case 'stone_depth': return stoneDepth(
//                        Json.readInt(root.offset) ?? 0,
//                Json.readBoolean(root.add_surface_depth) ?? false,
//                        Json.readInt(root.secondary_depth_range) ?? 0,
//                        Json.readString(root.surface_type) === 'ceiling',
//        )
//                case 'vertical_gradient': return verticalGradient(
//                        Json.readString(root.random_name) ?? '',
//                VerticalAnchor.fromJson(root.true_at_and_below),
//                        VerticalAnchor.fromJson(root.false_at_and_above),
//        )
//                case 'water': return water(
//                        Json.readInt(root.offset) ?? 0,
//                Json.readInt(root.surface_depth_multiplier) ?? 0,
//                        Json.readBoolean(root.add_surface_depth) ?? false,
//        )
//                case 'y_above': return yAbove(
//                        VerticalAnchor.fromJson(root.anchor),
//                        Json.readInt(root.surface_depth_multiplier) ?? 0,
//                Json.readBoolean(root.add_surface_depth) ?? false,
//        )
//            }
//            return FALSE
//        }

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

//        export function abovePreliminarySurface(): SurfaceCondition {
//            return context => context.blockY >= context.minSurfaceLevel()
//        }

        static SurfaceCondition abovePreliminarySurface() {
            return context -> context.blockY >= context.minSurfaceLevel.getAsInt();
        }

        static SurfaceCondition biome(List<String> biomes) {
            Set<String> biomeSet = new HashSet<>(biomes);
            return context -> biomeSet.contains(context.fetchBiome);
        }

//        export function not(invert: SurfaceCondition): SurfaceCondition {
//            return context => !invert(context)
//        }

        static SurfaceCondition not(SurfaceCondition invert) {
            return context -> !invert.test(context);
        }

//        export function stoneDepth(offset: number, addSurfaceDepth: boolean, secondaryDepthRange: number, ceiling: boolean): SurfaceCondition {
//            return context => {
//            const depth = ceiling ? context.stoneDepthBelow : context.stoneDepthAbove
//            const surfaceDepth = addSurfaceDepth ? context.surfaceDepth : 0
//            const secondaryDepth = secondaryDepthRange === 0 ? 0 : map(context.surfaceSecondary(), -1, 1, 0, secondaryDepthRange)
//                return depth <= 1 + offset + surfaceDepth + secondaryDepth
//            }
//        }

        static SurfaceCondition stoneDepth(int offset, boolean addSurfaceDepth, int secondaryDepthRange, boolean ceiling) {
            return context -> {
                int depth = ceiling ? context.stoneDepthBelow : context.stoneDepthAbove;
                int surfaceDepth = addSurfaceDepth ? context.surfaceDepth : 0;
                int secondaryDepth = secondaryDepthRange == 0 ? 0 : (int) Util.map(context.surfaceSecondary.getAsInt(), -1, 1, 0, secondaryDepthRange);
                return depth <= 1 + offset + surfaceDepth + secondaryDepth;
            };
        }

//        export function verticalGradient(randomName: string, trueAtAndBelow: VerticalAnchor, falseAtAndAbove: VerticalAnchor): SurfaceCondition {
//            return context => {
//        const trueAtAndBelowY = trueAtAndBelow(context.context)
//        const falseAtAndAboveY = falseAtAndAbove(context.context)
//                if (context.blockY <= trueAtAndBelowY) {
//                    return true
//                }
//                if (context.blockY >= falseAtAndAboveY) {
//                    return false
//                }
//        const random = context.system.getRandom(randomName)
//        const chance = map(context.blockY, trueAtAndBelowY, falseAtAndAboveY, 1, 0)
//                return random.nextFloat() < chance
//            }
//        }

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

//        export function water(offset: number, surfaceDepthMultiplier: number, addStoneDepth: boolean): SurfaceCondition {
//            return context => {
//                if (context.waterHeight === Number.MIN_SAFE_INTEGER) {
//                    return true
//                }
//                const stoneDepth = addStoneDepth ? context.stoneDepthAbove : 0
//                return context.blockY + stoneDepth >= context.waterHeight + offset + context.surfaceDepth * surfaceDepthMultiplier
//            }
//        }

        static SurfaceCondition water(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) {
            return context -> {
                if (context.waterHeight == Integer.MIN_VALUE) {
                    return true;
                }
                int stoneDepth = addStoneDepth ? context.stoneDepthAbove : 0;
                return context.blockY + stoneDepth >= context.waterHeight + offset + context.surfaceDepth * surfaceDepthMultiplier;
            };
        }

//        export function yAbove(anchor: VerticalAnchor, surfaceDepthMultiplier: number, addStoneDepth: boolean): SurfaceCondition {
//            return context => {
//                const stoneDepth = addStoneDepth ? context.stoneDepthAbove : 0
//                return context.blockY + stoneDepth >= anchor(context.context) + context.surfaceDepth * surfaceDepthMultiplier
//            }
//        }

        static SurfaceCondition yAbove(VerticalAnchor anchor, int surfaceDepthMultiplier, boolean addStoneDepth) {
            return context -> {
                int stoneDepth = addStoneDepth ? context.stoneDepthAbove : 0;
                return context.blockY + stoneDepth >= anchor.apply(context.context) + context.surfaceDepth * surfaceDepthMultiplier;
            };
        }
    }
}