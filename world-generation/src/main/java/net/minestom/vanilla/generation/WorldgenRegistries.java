package net.minestom.vanilla.generation;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.generation.densityfunctions.DensityFunction;
import net.minestom.vanilla.generation.densityfunctions.DensityFunctions;
import net.minestom.vanilla.generation.noise.Noise;
import net.minestom.vanilla.generation.noise.NoiseGeneratorSettings;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class WorldgenRegistries {
    public static final Registry<Noise.NoiseParameters> NOISE = register("worldgen/noise", Noise.NoiseParameters::fromJson);
    public static final Registry<DensityFunction> DENSITY_FUNCTION = register("worldgen/density_function", DensityFunctions::fromJson);
    public static final Registry<NoiseGeneratorSettings> NOISE_SETTINGS = register("worldgen/noise_settings", NoiseGeneratorSettings::fromJson);

    static {
        // Noise parameters
        for (var entry : Util.noiseParametersJsons().entrySet()) {
            NOISE.register(NamespaceID.from(entry.getKey()), Noise.NoiseParameters.fromJson(entry.getValue()));
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

    public static final Holder<Noise.NoiseParameters> SURFACE_NOISE = createNoise("surface", -6, 1, 1, 1);
    public static final Holder<Noise.NoiseParameters> SURFACE_SECONDARY_NOISE = createNoise("surface_secondary", -6, 1, 1, 0, 1);

    static Holder<Noise.NoiseParameters> createNoise(String name, double firstOctave, double @NotNull ... amplitudes) {
        return WorldgenRegistries.NOISE.register(NamespaceID.from(name), Noise.NoiseParameters.create(firstOctave, amplitudes), true);
    }
}
