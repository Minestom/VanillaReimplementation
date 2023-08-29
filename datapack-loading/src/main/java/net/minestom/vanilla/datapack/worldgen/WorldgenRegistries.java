package net.minestom.vanilla.datapack.worldgen;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.datapack.worldgen.noise.NormalNoise;

import java.util.function.Function;

import static net.minestom.vanilla.datapack.DatapackLoader.recordJson;

public class WorldgenRegistries {
    public static final Registry<NormalNoise.NoiseParameters> NOISE = register("worldgen/noise", NormalNoise.NoiseParameters::fromJson);
    public static final Registry<DensityFunction> DENSITY_FUNCTION = register("worldgen/density_function", DensityFunctions::fromJson);
    public static final Registry<NoiseSettings> NOISE_SETTINGS = register("worldgen/noise_settings", recordJson(NoiseSettings.class));

    static {
        // Noise parameters
        for (var entry : Util.noiseParametersJsons().entrySet()) {
            NOISE.register(NamespaceID.from(entry.getKey()), NormalNoise.NoiseParameters.fromJson(entry.getValue()));
        }

        // Density functions
        for (var entry : Util.densityFunctionJsons().entrySet()) {
            DENSITY_FUNCTION.register(NamespaceID.from(entry.getKey()), DensityFunctions.fromJson(entry.getValue()));
        }

        // Noise settings
        for (var entry : Util.noiseSettingsJsons().entrySet()) {
            NOISE_SETTINGS.register(NamespaceID.from(entry.getKey()), recordJson(NoiseSettings.class).apply(entry.getValue()));
        }
    }


    static <F, T> Registry<T> register(String name, Function<F, T> parser) {
        //noinspection unchecked
        Function<Object, T> ambigiousParser = (Function<Object, T>) parser;
        Registry<T> registry = new Registry<>(NamespaceID.from(name), ambigiousParser);
        Registry.REGISTRY.register(registry.key, registry);
        return registry;
    }

    public static final NormalNoise.NoiseParameters SURFACE_NOISE = NormalNoise.NoiseParameters.create(-6, new double[]{1, 1, 1});
    public static final NormalNoise.NoiseParameters SURFACE_SECONDARY_NOISE = NormalNoise.NoiseParameters.create(-6, new double[]{1, 1, 0, 1});
}