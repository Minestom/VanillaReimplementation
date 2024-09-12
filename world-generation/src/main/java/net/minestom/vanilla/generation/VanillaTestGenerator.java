package net.minestom.vanilla.generation;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.generators.noisegen.white.WhiteNoiseGenerator;
import de.articdive.jnoise.pipeline.JNoise;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.NotNull;

public class VanillaTestGenerator implements Generator {

    private final JNoise noise = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder()
                    .build())
            .scale(1.0 / 32.0)
            .build();
    private final JNoise treeNoise = JNoise.newBuilder()
            .white(WhiteNoiseGenerator.newBuilder().build())
            .build();

    private synchronized double noise(JNoise noise, double x, double z) {
        return noise.evaluateNoise(x, 0, z);
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        var modifier = unit.modifier();
        modifier.fillBiome(Biome.PLAINS);

        Point start = unit.absoluteStart();
        Point end = unit.absoluteEnd();

        for (int x = start.blockX(); x < end.blockX(); x++) {
            for (int z = start.blockZ(); z < end.blockZ(); z++) {

                double heightDelta = noise(noise, x, z);
                int height = (int) (64 - heightDelta * 16);
                int bottom = 0;
                int stone = height;
                int dirt = stone + 5;
                int grass = dirt + 1;

                for (int y = bottom; y < stone; y++) {
                    modifier.setBlock(x, y, z, Block.STONE);
                }

                for (int y = stone; y < dirt; y++) {
                    modifier.setBlock(x, y, z, Block.DIRT);
                }

                for (int y = dirt; y < grass; y++) {
                    modifier.setBlock(x, y, z, Block.GRASS_BLOCK);
                }

                if (height < 64) {
                    // Too low for a tree
                    // However we can put water here
                    for (int y = height; y < 64; y++) {
                        modifier.setBlock(x, y, z, Block.WATER);
                    }
                    continue;
                }

                if (noise(treeNoise, x, z) > 0.9) {
                    Point treePos = new Vec(x, grass, z);
                    unit.fork(setter -> spawnTree(setter, treePos));
                }
            }
        }
    }

    private void spawnTree(Block.Setter setter, Point pos) {
        int trunkX = pos.blockX();
        int trunkBottomY = pos.blockY();
        int trunkZ = pos.blockZ();

        for (int i = 0; i < 2; i++) {
            setter.setBlock(trunkX + 1, trunkBottomY + 3 + i, trunkZ, Block.OAK_LEAVES);
            setter.setBlock(trunkX - 1, trunkBottomY + 3 + i, trunkZ, Block.OAK_LEAVES);
            setter.setBlock(trunkX, trunkBottomY + 3 + i, trunkZ + 1, Block.OAK_LEAVES);
            setter.setBlock(trunkX, trunkBottomY + 3 + i, trunkZ - 1, Block.OAK_LEAVES);

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    setter.setBlock(trunkX + x, trunkBottomY + 2 + i, trunkZ - z, Block.OAK_LEAVES);
                }
            }
        }

        setter.setBlock(trunkX, trunkBottomY, trunkZ, Block.OAK_LOG);
        setter.setBlock(trunkX, trunkBottomY + 1, trunkZ, Block.OAK_LOG);
        setter.setBlock(trunkX, trunkBottomY + 2, trunkZ, Block.OAK_LOG);
        setter.setBlock(trunkX, trunkBottomY + 3, trunkZ, Block.OAK_LOG);
        setter.setBlock(trunkX, trunkBottomY + 4, trunkZ, Block.OAK_LEAVES);
    }
}
