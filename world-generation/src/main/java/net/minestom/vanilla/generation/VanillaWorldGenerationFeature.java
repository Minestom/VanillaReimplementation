package net.minestom.vanilla.generation;

import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.generation.densityfunctions.DensityFunctions;
import net.minestom.vanilla.generation.noise.*;
import net.minestom.vanilla.instance.SetupVanillaInstanceEvent;
import org.jetbrains.annotations.NotNull;

public class VanillaWorldGenerationFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {
        vri.process().eventHandler().addListener(SetupVanillaInstanceEvent.class, event -> {
//            event.getInstance().setGenerator(new VanillaTestGenerator());
//            event.getInstance().setGenerator(unit -> unit.modifier().setAll((x, y, z) -> (y > 0 && y < 16) ? Block.COPPER_BLOCK : Block.AIR));

//            const fooNoise = WorldgenRegistries.NOISE.register(Identifier.parse('test:foo'),
//                    { firstOctave: -5, amplitudes: [1, 1] })
//const noiseSettings = NoiseSettings.create({ minY: 0, height: 256 })
//const settings = NoiseGeneratorSettings.create({
//                    defaultBlock: BlockState.STONE,
//                    defaultFluid: BlockState.WATER,
//                    seaLevel: 63,
//                    noise: noiseSettings,
//                    noiseRouter: NoiseRouter.create({
//                    finalDensity: new DensityFunction.Ap2('add',
//                    new DensityFunction.YClampedGradient(0, 256, 1, -1),
//                    new DensityFunction.Noise(1, 1, fooNoise)
//            ),
//    }),
//})
            var fooNoise = WorldgenRegistries.NOISE.register(
                    NamespaceID.from("test:foo"),
                    new NormalNoise.NoiseParameters(-5, 1, 1));
            NoiseSettings noiseSettings = NoiseSettings.create(0, 256);

            //                    surfaceRule:SurfaceRule.NOOP,
//                    noise:NoiseSettings.create({}),
//                    defaultBlock:BlockState.STONE,
//                    defaultFluid:BlockState.WATER,
//                    noiseRouter:NoiseRouter.create({}),
//                    seaLevel:0,
//                    disableMobGeneration:false,
//                    aquifersEnabled:false,
//                    oreVeinsEnabled:false,
//                    legacyRandomSource:false,
            NoiseGeneratorSettings settings = new NoiseGeneratorSettings(
                    noiseSettings,
                    SurfaceSystem.SurfaceRule.NOOP,
                    Block.STONE,
                    Block.WATER,
                    new NoiseRouter(
                            DensityFunctions.Constant.ZERO,
                            DensityFunctions.Constant.ZERO,
                            DensityFunctions.Constant.ZERO,
                            DensityFunctions.Constant.ZERO,
                            DensityFunctions.Constant.ZERO,
                            DensityFunctions.Constant.ZERO,
                            DensityFunctions.Constant.ZERO,
                            DensityFunctions.Constant.ZERO,
                            DensityFunctions.Constant.ZERO,
                            DensityFunctions.Constant.ZERO,
                            DensityFunctions.Constant.ZERO,
                            new DensityFunctions.Ap2("add",
                                    new DensityFunctions.YClampedGradient(0, 256, 1, -1),
                                    new DensityFunctions.Noise(1, 1, fooNoise)
                            ),
                            DensityFunctions.Constant.ZERO,
                            DensityFunctions.Constant.ZERO,
                            DensityFunctions.Constant.ZERO
                    ),
            0,
                    false,
        false,
        false,
                    false
                );

            NamespaceID plains = NamespaceID.from("minecraft:plains");

            NoiseChunkGenerator generator = new NoiseChunkGenerator((x, y, z, sampler) -> plains, settings, event.getInstance().getDimensionType());
            event.getInstance().setChunkGenerator(generator);
        });
    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("vri:worldgeneration");
    }
}
