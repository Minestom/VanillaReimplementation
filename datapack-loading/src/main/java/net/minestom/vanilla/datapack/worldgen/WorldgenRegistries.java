package net.minestom.vanilla.datapack.worldgen;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minestom.vanilla.datapack.worldgen.noise.NormalNoise;

public class WorldgenRegistries {
    public static final NormalNoise.Config SURFACE_NOISE = new NormalNoise.Config(-6, DoubleList.of(1, 1, 1));
    public static final NormalNoise.Config SURFACE_SECONDARY_NOISE = new NormalNoise.Config(-6, DoubleList.of(1, 1, 0, 1));
}