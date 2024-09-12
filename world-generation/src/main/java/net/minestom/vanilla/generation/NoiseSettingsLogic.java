package net.minestom.vanilla.generation;

import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.datapack.worldgen.NoiseSettings;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;

public class NoiseSettingsLogic {

    private static void checkForErrors(NoiseSettings.Noise noiseSettings) {
        // TODO: Get dimension type from datapack
        DimensionType dimensionType = VanillaDimensionTypes.OVERWORLD;

        if (noiseSettings.min_y() + noiseSettings.height() > dimensionType.getMaxY() + 1) {
            throw new IllegalStateException("min_y + height cannot be higher than: " + (dimensionType.getMaxY() + 1));
        }

        if (noiseSettings.height() % 16 != 0) {
            throw new IllegalStateException("height has to be a multiple of 16");
        }

        if (noiseSettings.min_y() % 16 != 0) {
            throw new IllegalStateException("min_y has to be a multiple of 16");
        }
    }

    public static NoiseSettings.Noise create(int minY, int height, int sizeHorizontal, int sizeVertical) {
        NoiseSettings.Noise noiseSettings = new NoiseSettings.Noise(minY, height, sizeHorizontal, sizeVertical);
        checkForErrors(noiseSettings);
        return noiseSettings;
    }

    public static NoiseSettings.Noise clampToHeight(NoiseSettings.Noise settings, int minBuildHeight, int maxBuildHeight) {
        int clampedMinY = Math.max(settings.min_y(), minBuildHeight);
        int clampedHeight = Math.min(settings.min_y() + settings.height(), maxBuildHeight) - clampedMinY;
        return new NoiseSettings.Noise(clampedMinY, clampedHeight, settings.size_horizontal(), settings.size_vertical());
    }

    public static int getCellHeight(NoiseSettings.Noise settings) {
        return Util.blockFromQuart(settings.size_vertical());
    }

    public static int getCellWidth(NoiseSettings.Noise settings) {
        return Util.blockFromQuart(settings.size_horizontal());
    }
}
