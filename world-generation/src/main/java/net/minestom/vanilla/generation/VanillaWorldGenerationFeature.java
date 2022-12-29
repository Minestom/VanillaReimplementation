package net.minestom.vanilla.generation;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.generation.biome.BiomeSource;
import net.minestom.vanilla.generation.noise.NoiseGeneratorSettings;
import net.minestom.vanilla.instance.SetupVanillaInstanceEvent;
import org.jetbrains.annotations.NotNull;

public class VanillaWorldGenerationFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {
        vri.process().eventHandler().addListener(SetupVanillaInstanceEvent.class, event -> {

            NamespaceID plains = NamespaceID.from("minecraft:plains");
            NoiseGeneratorSettings settings = WorldgenRegistries.NOISE_SETTINGS.getOrThrow(NamespaceID.from("minecraft:overworld"));
//            BiomeSource.fromJson()

            NoiseChunkGenerator generator = new NoiseChunkGenerator((x, y, z, sampler) -> plains, settings, event.getInstance().getDimensionType());
            event.getInstance().setChunkGenerator(generator);
        });
    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("vri:worldgeneration");
    }
}
