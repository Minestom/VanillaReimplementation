package net.minestom.vanilla.datapack.worldgen;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minestom.vanilla.datapack.worldgen.noise.NormalNoise;

public class WorldgenRegistries {
    public static final NormalNoise.NoiseParameters SURFACE_NOISE = new NormalNoise.NoiseParameters(-6, DoubleList.of(1, 1, 1));
    public static final NormalNoise.NoiseParameters SURFACE_SECONDARY_NOISE = new NormalNoise.NoiseParameters(-6, DoubleList.of(1, 1, 0, 1));
}