package net.minestom.vanilla.generation;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.worldgen.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoiseChunk implements DensityFunction.Context {
    private final NoiseSettings.Noise noiseSettings;
    final int cellCountXZ;
    final int cellCountY;
    final int cellNoiseMinY;
    private final int firstCellX;
    private final int firstCellZ;
    final int firstNoiseX;
    final int firstNoiseZ;
    final List<DensityFunction.Interpolated> interpolators;
    final List<DensityFunction.CacheAllInCell> cellCaches;
    private final Map<DensityFunction, DensityFunction> wrapped = new HashMap();
    private final Long2IntMap preliminarySurfaceLevel = new Long2IntOpenHashMap();
    private final Aquifer aquifer;
    private final DensityFunction initialDensityNoJaggedness;
    private final MaterialRule materialRule;
    private final DensityFunction.FlatCache blendAlpha;
    private final DensityFunction.FlatCache blendOffset;
    private final DensityFunction.Beardifier beardifier;
    final int noiseSizeXZ;
    final int cellWidth;
    final int cellHeight;
    boolean interpolating;
    boolean fillingCell;
    private int cellStartBlockX;
    int cellStartBlockY;
    private int cellStartBlockZ;
    int inCellX;
    int inCellY;
    int inCellZ;
    long interpolationCounter;
    long arrayInterpolationCounter;
    int arrayIndex;
    private final ContextProvider sliceFillingContextProvider;

    @Override
    public double x() {
        return this.cellStartBlockX + this.inCellX;
    }

    @Override
    public double y() {
        return this.cellStartBlockY + this.inCellY;
    }

    @Override
    public double z() {
        return this.cellStartBlockZ + this.inCellZ;
    }

    public Aquifer aquifer() {
        return this.aquifer;
    }

    public int cellWidth() {
        return this.cellWidth;
    }

    public int cellHeight() {
        return this.cellHeight;
    }

    public MaterialRule materialRule() {
        return this.materialRule;
    }

    interface ContextProvider {
        DensityFunction.Context forIndex(int index);
        void fillAllDirectly(double[] values, DensityFunction function);
    }

    public NoiseChunk(int cellCountXZ, RandomState randomState, int x, int z, NoiseSettings.Noise noiseSettings, DensityFunction.Beardifier beardifier, NoiseSettings settings, Aquifer.FluidPicker fluidPicker) {
        this.sliceFillingContextProvider = new ContextProvider() {
            public DensityFunction.Context forIndex(int index) {
                NoiseChunk.this.cellStartBlockY = (index + NoiseChunk.this.cellNoiseMinY) * NoiseChunk.this.cellHeight;
                ++NoiseChunk.this.interpolationCounter;
                NoiseChunk.this.inCellY = 0;
                NoiseChunk.this.arrayIndex = index;
                return NoiseChunk.this;
            }

            public void fillAllDirectly(double[] array, DensityFunction function) {
                for (int i = 0; i < NoiseChunk.this.cellCountY + 1; ++i) {
                    NoiseChunk.this.cellStartBlockY = (i + NoiseChunk.this.cellNoiseMinY) * NoiseChunk.this.cellHeight;
                    ++NoiseChunk.this.interpolationCounter;
                    NoiseChunk.this.inCellY = 0;
                    NoiseChunk.this.arrayIndex = i;
                    array[i] = function.compute(NoiseChunk.this);
                }
            }
        };
        this.noiseSettings = noiseSettings;
        this.cellWidth = NoiseSettingsLogic.getCellWidth(noiseSettings);
        this.cellHeight = NoiseSettingsLogic.getCellHeight(noiseSettings);
        this.cellCountXZ = cellCountXZ;
        this.cellCountY = Math.floorDiv(noiseSettings.height(), this.cellHeight);
        this.cellNoiseMinY = Math.floorDiv(noiseSettings.min_y(), this.cellHeight);
        this.firstCellX = Math.floorDiv(x, this.cellWidth);
        this.firstCellZ = Math.floorDiv(z, this.cellWidth);
        this.interpolators = new ArrayList<>();
        this.cellCaches = new ArrayList<>();
        this.firstNoiseX = Util.quartFromBlock(x);
        this.firstNoiseZ = Util.quartFromBlock(z);
        this.noiseSizeXZ = Util.quartFromBlock(cellCountXZ * this.cellWidth);
        this.beardifier = beardifier;
        this.blendAlpha = new DensityFunction.FlatCache(new DensityFunction.BlendAlpha());
        this.blendOffset = new DensityFunction.FlatCache(new DensityFunction.BlendOffset());

        NoiseSettings.NoiseRouter noiseRouter = randomState.router();
        if (!settings.aquifers_enabled()) {
            this.aquifer = Aquifer.createDisabled(fluidPicker);
        } else {
            int chunkX = ChunkUtils.getChunkCoordinate(x);
            int chunkZ = ChunkUtils.getChunkCoordinate(z);
            this.aquifer = Aquifer.create(this, ChunkUtils.getChunkIndex(chunkX, chunkZ), noiseRouter, randomState.aquiferRandom(), noiseSettings.min_y(), noiseSettings.height(), fluidPicker);
        }

        List<MaterialRule> materialRules = new ArrayList<>();
        DensityFunction densityFunction = new DensityFunction.CacheAllInCell(new DensityFunction.Add(noiseRouter.final_density(), new DensityFunction.Beardifier()));
        materialRules.add((context) -> this.aquifer.compute(context, densityFunction.compute(context)));
        if (settings.ore_veins_enabled()) {
            materialRules.add(OreVeinifier.create(noiseRouter.vein_toggle(), noiseRouter.vein_ridged(), noiseRouter.vein_gap(), randomState.oreRandom()));
        }

        this.materialRule = MaterialRule.fromList(materialRules);
        this.initialDensityNoJaggedness = noiseRouter.initial_density_without_jaggedness();
    }

    public static NoiseChunk forChunk(TargetChunk targetChunk, RandomState randomState, DensityFunction.Beardifier beardifier, NoiseSettings settings, Aquifer.FluidPicker fluidPicker) {
        NoiseSettings.Noise noiseSettings = NoiseSettingsLogic.clampToHeight(settings.noise(), targetChunk.minY(), targetChunk.maxY());
        long chunkIndex = targetChunk.chunk();
        int cellCountXZ = 16 / NoiseSettingsLogic.getCellWidth(noiseSettings);
        return new NoiseChunk(cellCountXZ, randomState, targetChunk.minY(), targetChunk.minZ(), noiseSettings, beardifier, settings, fluidPicker);
    }

    public @Nullable Block getFinalState(Datapack datapack, int x, int y, int z) {
        return this.materialRule.compute(DensityFunction.context(x, y, z));
    }

    public int getPreliminarySurfaceLevel(int quartX, int quartZ) {
        return preliminarySurfaceLevel.computeIfAbsent(ChunkUtils.getChunkIndex(quartX, quartZ), (key) -> {
            int x = quartX << 2;
            int z = quartZ << 2;
            for (int y = this.noiseSettings.min_y() + this.noiseSettings.height(); y >= this.noiseSettings.min_y(); y -= this.cellHeight) {
                double density = this.initialDensityNoJaggedness.compute(DensityFunction.context(x, y, z));
                if (density > 0.390625) {
                    return y;
                }
            }
            return Integer.MIN_VALUE;
        });
    }

    public interface MaterialRule {
        @Nullable Block compute(DensityFunction.Context context);

        static MaterialRule fromList(List<MaterialRule> rules) {
            List<MaterialRule> finalRules = List.copyOf(rules);
            return (context) -> {
                for (MaterialRule rule : finalRules) {
                    Block state = rule.compute(context);
                    if (state != null) return state;
                }
                return null;
            };
        }
    }
}
