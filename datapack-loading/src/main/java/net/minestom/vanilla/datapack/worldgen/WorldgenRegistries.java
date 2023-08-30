package net.minestom.vanilla.datapack.worldgen;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.worldgen.noise.NormalNoise;

import java.util.function.Function;

import static net.minestom.vanilla.datapack.DatapackLoader.recordJson;

public class WorldgenRegistries {
    public static final NormalNoise.NoiseParameters SURFACE_NOISE = NormalNoise.NoiseParameters.create(-6, new double[]{1, 1, 1});
    public static final NormalNoise.NoiseParameters SURFACE_SECONDARY_NOISE = NormalNoise.NoiseParameters.create(-6, new double[]{1, 1, 0, 1});
}