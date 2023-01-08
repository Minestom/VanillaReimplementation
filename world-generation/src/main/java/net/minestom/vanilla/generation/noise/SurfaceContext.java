package net.minestom.vanilla.generation.noise;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.vanilla.generation.NoiseChunkGenerator;
import net.minestom.vanilla.generation.Util;

import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class SurfaceContext {
    public int blockX, blockY, blockZ;
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

    SurfaceContext(SurfaceSystem system, NoiseChunkGenerator.TargetChunk chunk, NoiseChunk noiseChunk, VerticalAnchor.WorldgenContext context,
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
