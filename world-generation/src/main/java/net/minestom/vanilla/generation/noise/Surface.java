package net.minestom.vanilla.generation.noise;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.generation.NoiseChunkGenerator;
import net.minestom.vanilla.generation.Util;
import net.minestom.vanilla.generation.random.WorldgenRandom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface Surface {
    interface Rule {
        Pos2Block apply(Context context);

        interface Pos2Block {
            Block apply(int x, int y, int z);
        }

        Rule NOOP = context -> (x, y, z) -> null;

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

        static Rule fromJson(Object obj) {
            JsonObject root = Util.jsonObject(obj);
            String type = Util.jsonElse(root, "type", "", JsonElement::getAsString).replace("minecraft:", "");
            return switch (type) {
                case "block" -> block(Util.jsonRequire(root, "result_state", Util.jsonVanillaBlock()));
                case "sequence" -> sequence(Util.jsonRequire(root, "sequence", Util.jsonReadArray(Rule::fromJson)));
                case "condition" ->
                        condition(Condition.fromJson(Util.jsonRequire(root, "if_true", JsonElement::getAsJsonObject)),
                                fromJson(Util.jsonRequire(root, "then_run", JsonElement::getAsJsonObject)));
                default -> NOOP;
            };
        }

//        export function block(state: BlockState): SurfaceRule {
//            return () => () => state
//        }

        static Rule block(Block state) {
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

        static Rule sequence(List<Rule> rules) {
            return context -> {
                List<Pos2Block> rulesWithContext = rules.stream()
                        .map(rule -> rule.apply(context))
                        .toList();
                return (x, y, z) -> {
                    for (Pos2Block rule : rulesWithContext) {
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

        static Rule condition(Condition ifTrue, Rule thenRun) {
            return context -> (x, y, z) -> {
                if (ifTrue.test(context)) {
                    return thenRun.apply(context).apply(x, y, z);
                }
                return null;
            };
        }
    }

    interface Condition {
        boolean test(Context context);

        Condition FALSE = context -> false;
        Condition TRUE = context -> true;

        static Condition fromJson(Object obj) {
            JsonObject root = Util.jsonObject(obj);
            String type = Util.jsonElse(root, "type", "", JsonElement::getAsString).replace("minecraft:", "");
            return switch (type) {
                case "above_preliminary_surface" -> abovePreliminarySurface();
                case "biome" -> biome(Util.jsonRequire(root, "biome_is", Util.jsonReadArray(JsonElement::getAsString)));
                case "not" -> Condition.not(fromJson(Util.jsonRequire(root, "invert", JsonElement::getAsJsonObject)));
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

        static Condition abovePreliminarySurface() {
            return context -> context.blockY >= context.minSurfaceLevel.getAsInt();
        }

        static Condition biome(List<String> biomes) {
            Set<String> biomeSet = new HashSet<>(biomes);
            return context -> biomeSet.contains(context.biome);
        }

//        export function not(invert: SurfaceCondition): SurfaceCondition {
//            return context => !invert(context)
//        }

        static Condition not(Condition invert) {
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

        static Condition stoneDepth(int offset, boolean addSurfaceDepth, int secondaryDepthRange, boolean ceiling) {
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

        static Condition verticalGradient(String randomName, VerticalAnchor trueAtAndBelow, VerticalAnchor falseAtAndAbove) {
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

        static Condition water(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) {
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

        static Condition yAbove(VerticalAnchor anchor, int surfaceDepthMultiplier, boolean addStoneDepth) {
            return context -> {
                int stoneDepth = addStoneDepth ? context.stoneDepthAbove : 0;
                return context.blockY + stoneDepth >= anchor.apply(context.context) + context.surfaceDepth * surfaceDepthMultiplier;
            };
        }
    }

    class Context {
        public int blockX;
        public int blockY;
        public int blockZ;
        public int stoneDepthAbove;
        public int stoneDepthBelow;
        public int surfaceDepth;
        public int waterHeight;

        public Supplier<String> biome = () -> "";
        public IntSupplier surfaceSecondary = () -> 0;
        public IntSupplier minSurfaceLevel = () -> 0;

        public final SurfaceSystem system;
        public final NoiseChunkGenerator.TargetChunk chunk;
        public final NoiseChunk noiseChunk;
        public final VerticalAnchor.WorldgenContext context;
        private final Function<Point, String> getBiome;

        Context(SurfaceSystem system, NoiseChunkGenerator.TargetChunk chunk, NoiseChunk noiseChunk, VerticalAnchor.WorldgenContext context,
                Function<Point, String> getBiome) {
            this.system = system;
            this.chunk = chunk;
            this.noiseChunk = noiseChunk;
            this.context = context;
            this.getBiome = getBiome;
        }

        public void updateXZ(int x, int z) {
            this.blockX = x;
            this.blockZ = z;
            this.surfaceDepth = this.system.getSurfaceDepth(x, z);
            this.surfaceSecondary = Util.lazyInt(() -> (int) this.system.getSurfaceSecondary(x, z));
            this.minSurfaceLevel = Util.lazyInt(() -> this.calculateMinSurfaceLevel(x, z));
        }

        public void updateY(int stoneDepthAbove, int stoneDepthBelow, int waterHeight, int y) {
            this.blockY = y;
            this.stoneDepthAbove = stoneDepthAbove;
            this.stoneDepthBelow = stoneDepthBelow;
            this.waterHeight = waterHeight;
            this.biome = Util.lazy(() -> this.getBiome.apply(new Vec(this.blockX, this.blockY, this.blockZ)));
        }

        private int calculateMinSurfaceLevel(int x, int z) {
            int cellX = x >> 4;
            int cellZ = z >> 4;
            int level00 = this.noiseChunk.getPreliminarySurfaceLevel(cellX << 4, cellZ << 4);
            int level10 = this.noiseChunk.getPreliminarySurfaceLevel((cellX + 1) << 4, cellZ << 4);
            int level01 = this.noiseChunk.getPreliminarySurfaceLevel(cellX << 4, (cellZ + 1) << 4);
            int level11 = this.noiseChunk.getPreliminarySurfaceLevel((cellX + 1) << 4, (cellZ + 1) << 4);
            int level = (int) Math.floor(Util.lerp2((x & 0xF) / 16, (z & 0xF) / 16, level00, level10, level01, level11));
            return level + this.surfaceDepth - 8;
        }
    }
}
