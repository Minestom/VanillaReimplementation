package net.minestom.vanilla.generation.noise;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.generation.NoiseChunkGenerator;
import net.minestom.vanilla.generation.WorldgenRegistries;
import net.minestom.vanilla.generation.random.WorldgenRandom;
import net.minestom.vanilla.generation.random.XoroshiroRandom;

import java.util.*;
import java.util.function.Function;

public class SurfaceSystem {
    private final NormalNoise surfaceNoise;
    private final NormalNoise surfaceSecondaryNoise;
    private final WorldgenRandom.Positional random;
    private final Map<String, WorldgenRandom> positionalRandoms;

    private final Surface.Rule rule;
    private final Block defaultBlock;

    public SurfaceSystem(Surface.Rule rule, Block defaultBlock, long seed) {
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
        Surface.Context surfaceContext = new Surface.Context(this, chunk, noiseChunk, worldgenContext, getBiome);
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
}