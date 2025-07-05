package net.minestom.vanilla.generation;

import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.worldgen.DensityFunction;
import net.minestom.vanilla.datapack.worldgen.NoiseSettings;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoiseChunk {
    public final int cellWidth;
    public final int cellHeight;
    public final int firstCellX;
    public final int firstCellZ;
    public final double firstNoiseX;
    public final double firstNoiseZ;
    public final double noiseSizeXZ;
    private final Map<Long, Integer> preliminarySurfaceLevel = new HashMap<>();
    private final Aquifer aquifer;
    private final MaterialRule materialRule;
    private final DensityFunction initialDensity;

    public int cellCountXZ;
    public int cellCountY;
    public int cellNoiseMinY;

    public int minX;
    public int minZ;
    public NoiseSettings settings;

    public NoiseChunk(
            int cellCountXZ,
            int cellCountY,
            int cellNoiseMinY,
            RandomState randomState,
            int minX,
            int minZ,
            NoiseSettings settings,
            boolean aquifersEnabled,
            Aquifer.FluidPicker fluidPicker) {
        this.cellWidth = NoiseSettings.cellWidth(settings);
        this.cellHeight = NoiseSettings.cellHeight(settings);
        this.firstCellX = (int) (double) (minX / this.cellWidth);
        this.firstCellZ = (int) (double) (minZ / this.cellWidth);
        this.firstNoiseX = minX >> 2;
        this.firstNoiseZ = minZ >> 2;
        this.noiseSizeXZ = (cellCountXZ * this.cellWidth) >> 2;

        if (true) { // WIP: Noise aquifers don't work yet
            this.aquifer = Aquifer.createDisabled(fluidPicker);
        } else {
            Point chunkPos = new Vec(minX, 0, minZ);
            int minY = cellNoiseMinY * NoiseSettings.cellHeight(settings);
            int height = cellCountY * NoiseSettings.cellHeight(settings);
            this.aquifer = new Aquifer.NoiseAquifer(this, chunkPos, randomState.router, randomState.aquiferRandom, minY, height, fluidPicker);
        }
        DensityFunction finalDensity = randomState.router.final_density();
        this.materialRule = MaterialRule.fromList(List.of(
                (context) -> this.aquifer.compute(context, finalDensity.compute(context))
        ));
        this.initialDensity = randomState.router.initial_density_without_jaggedness();
    }

    public @Nullable Block getFinalState(Datapack datapack, int x, int y, int z) {
        return this.materialRule.compute(DensityFunction.context(x, y, z));
    }

    public int getPreliminarySurfaceLevel(int quartX, int quartZ) {
        return preliminarySurfaceLevel.computeIfAbsent(CoordConversion.chunkIndex(quartX, quartZ), (key) -> {
            int x = quartX << 2;
            int z = quartZ << 2;
            for (int y = this.settings.noise().min_y() + this.settings.noise().height(); y >= this.settings.noise().min_y(); y -= this.cellHeight) {
                double density = this.initialDensity.compute(DensityFunction.context(x, y, z));
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
            return (context) -> {
                for (MaterialRule rule : rules) {
                    Block state = rule.compute(context);
                    if (state != null) return state;
                }
                return null;
            };
        }
    }
}
