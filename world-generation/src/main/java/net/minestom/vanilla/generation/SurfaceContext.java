package net.minestom.vanilla.generation;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.biome.Biome;
import net.minestom.vanilla.datapack.worldgen.*;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;
import net.minestom.vanilla.datapack.worldgen.util.Util;

import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class SurfaceContext implements NoiseSettings.SurfaceRule.Context {
    public int blockX;
    public int blockY;
    public int blockZ;
    public int stoneDepthAbove;
    public int stoneDepthBelow;
    public int surfaceDepth;
    public int waterHeight;

    public Supplier<NamespaceID> fetchBiome = Biome.PLAINS::namespace;
    public IntSupplier surfaceSecondary = () -> 0;
    public IntSupplier minSurfaceLevel = () -> 0;

    public final SurfaceSystem system;
    public final NoiseChunkGenerator.TargetChunk chunk;
    public final NoiseChunk noiseChunk;
    public final WorldgenContext context;
    private final Function<Point, NamespaceID> getBiome;

    public SurfaceContext(SurfaceSystem system, NoiseChunkGenerator.TargetChunk chunk, NoiseChunk noiseChunk, WorldgenContext context,
                   Function<Point, NamespaceID> getBiome) {
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
        this.fetchBiome = Util.lazy(() -> this.getBiome.apply(new Vec(this.blockX, this.blockY, this.blockZ)));
    }

    private int calculateMinSurfaceLevel(int x, int z) {
        int cellX = x >> 4;
        int cellZ = z >> 4;
        int level00 = this.noiseChunk.getPreliminarySurfaceLevel(cellX << 4, cellZ << 4);
        int level10 = this.noiseChunk.getPreliminarySurfaceLevel((cellX + 1) << 4, cellZ << 4);
        int level01 = this.noiseChunk.getPreliminarySurfaceLevel(cellX << 4, (cellZ + 1) << 4);
        int level11 = this.noiseChunk.getPreliminarySurfaceLevel((cellX + 1) << 4, (cellZ + 1) << 4);
        int level = (int) Math.floor(Util.lerp2((double) (x & 0xF) / 16, (double) (z & 0xF) / 16, level00, level10, level01, level11));
        return level + this.surfaceDepth - 8;
    }

    private DensityFunction.Context asDFContext() {
        return DensityFunction.context(this.blockX, this.blockY, this.blockZ);
    }

    @Override
    public NamespaceID biome() {
        return this.fetchBiome.get();
    }

    @Override
    public int minY() {
        return this.blockY - this.stoneDepthAbove;
    }

    @Override
    public int maxY() {
        return context.minY();
    }

    @Override
    public int blockX() {
        return this.blockX;
    }

    @Override
    public int blockY() {
        return this.blockY;
    }

    @Override
    public int blockZ() {
        return this.blockZ;
    }

    @Override
    public WorldgenRandom random(String string) {
        return this.system.getRandom(string);
    }

    @Override
    public int stoneDepthAbove() {
        return this.stoneDepthAbove;
    }

    @Override
    public int surfaceDepth() {
        return this.surfaceDepth;
    }

    @Override
    public int waterHeight() {
        return this.waterHeight;
    }

    @Override
    public int minSurfaceLevel() {
        return this.minSurfaceLevel.getAsInt();
    }

    @Override
    public int stoneDepthBelow() {
        return this.stoneDepthBelow;
    }

    @Override
    public double surfaceSecondary() {
        return this.surfaceSecondary.getAsInt();
    }
}
