package net.minestom.vanilla.generation;

import net.kyori.adventure.key.Key;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.datapack.Datapack;
import net.minestom.vanilla.datapack.DatapackLoadingFeature;
import net.minestom.vanilla.datapack.worldgen.NoiseSettings;
import net.minestom.vanilla.instance.SetupVanillaInstanceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class VanillaWorldGenerationFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull HookContext context) {
        context.vri().process().eventHandler().addListener(SetupVanillaInstanceEvent.class, event -> {

            Key plains = Key.key("minecraft:plains");
            DatapackLoadingFeature datapackLoading = context.vri().feature(DatapackLoadingFeature.class);
            Datapack datapack = datapackLoading.current();

            Datapack.NamespacedData data = datapack.namespacedData().get("minecraft");
            if (data == null) {
                throw new IllegalStateException("minecraft namespace not found");
            }

            NoiseSettings settings = data.world_gen().noise_settings().file("overworld.json");
//            BiomeSource.fromJson()

//            ThreadLocal<NoiseChunkGenerator> generators = ThreadLocal.withInitial(() -> new NoiseChunkGenerator(datapack, (x, y, z, sampler) -> plains, settings, event.getInstance().getDimensionType()));
//            event.getInstance().setChunkGenerator(new ChunkGenerator() {
//                @Override
//                public void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ) {
//                    generators.get().generateChunkData(batch, chunkX, chunkZ);
//                }
//
//                @Override
//                public @Nullable List<ChunkPopulator> getPopulators() {
//                    return null;
//                }
//            });
        });
    }

    @Override
    public @NotNull Key key() {
        return Key.key("vri:worldgeneration");
    }

    @Override
    public @NotNull Set<Class<? extends VanillaReimplementation.Feature>> dependencies() {
        return Set.of(DatapackLoadingFeature.class);
    }
}
