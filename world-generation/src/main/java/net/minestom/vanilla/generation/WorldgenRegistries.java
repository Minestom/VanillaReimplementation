package net.minestom.vanilla.generation;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.densityfunctions.DensityFunction;
import net.minestom.vanilla.generation.densityfunctions.DensityFunctions;
import net.minestom.vanilla.generation.math.Util;
import net.minestom.vanilla.generation.noise.NoiseGeneratorSettings;
import net.minestom.vanilla.generation.noise.NormalNoise;

import java.util.function.Function;

public class WorldgenRegistries {
    public static final Registry<NormalNoise.NoiseParameters> NOISE = register("worldgen/noise", NormalNoise.NoiseParameters::fromJson);
    public static final Registry<DensityFunction> DENSITY_FUNCTION = register("worldgen/density_function", DensityFunctions::fromJson);
    public static final Registry<NoiseGeneratorSettings> NOISE_SETTINGS = register("worldgen/noise_settings", NoiseGeneratorSettings::fromJson);

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
            NOISE_SETTINGS.register(NamespaceID.from(entry.getKey()), NoiseGeneratorSettings.fromJson(entry.getValue()));
        }
    }


    static <F, T> Registry<T> register(String name, Function<F, T> parser) {
        //noinspection unchecked
        Function<Object, T> ambigiousParser = (Function<Object, T>) parser;
        Registry<T> registry = new Registry<>(NamespaceID.from(name), ambigiousParser);
        Registry.REGISTRY.register(registry.key, registry);
        return registry;
    }

    public static final Holder<NormalNoise.NoiseParameters> SURFACE_NOISE = createNoise("surface", -6, new double[]{1, 1, 1});
    public static final Holder<NormalNoise.NoiseParameters> SURFACE_SECONDARY_NOISE = createNoise("surface_secondary", -6, new double[]{1, 1, 0, 1});

    static Holder<NormalNoise.NoiseParameters> createNoise(String name, double firstOctave, double[] amplitudes) {
        return WorldgenRegistries.NOISE.register(NamespaceID.from(name), NormalNoise.NoiseParameters.create(firstOctave, amplitudes), true);
    }
}
