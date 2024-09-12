package net.minestom.vanilla.generation;

import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.datapack.worldgen.DensityFunction;
import net.minestom.vanilla.datapack.worldgen.random.WorldgenRandom;

public final class OreVeinifier {
    private static final float VEININESS_THRESHOLD = 0.4F;
    private static final int EDGE_ROUNDOFF_BEGIN = 20;
    private static final double MAX_EDGE_ROUNDOFF = 0.2;
    private static final float VEIN_SOLIDNESS = 0.7F;
    private static final float MIN_RICHNESS = 0.1F;
    private static final float MAX_RICHNESS = 0.3F;
    private static final float MAX_RICHNESS_THRESHOLD = 0.6F;
    private static final float CHANCE_OF_RAW_ORE_BLOCK = 0.02F;
    private static final float SKIP_ORE_IF_GAP_NOISE_IS_BELOW = -0.3F;

    private OreVeinifier() {
    }

    public static NoiseChunk.MaterialRule create(DensityFunction densityFunction, DensityFunction richnessFunction, DensityFunction gapFunction, WorldgenRandom.Positional positional) {
        return (context) -> {
            double density = densityFunction.compute(context);
            int blockY = context.blockY();
            VeinType veinType = density > 0.0 ? VeinType.COPPER : VeinType.IRON;
            double absDensity = Math.abs(density);
            int maxYDistance = veinType.maxY - blockY;
            int minYDistance = blockY - veinType.minY;

            if (minYDistance < 0 || maxYDistance < 0) {
                return null;
            }

            int distance = Math.min(maxYDistance, minYDistance);
            double veininess = Util.clampedMap(distance, 0.0, 20.0, -0.2, 0.0);
            if (absDensity + veininess < 0.4000000059604645) {
                return null;
            }

            WorldgenRandom random = positional.at(context.blockX(), blockY, context.blockZ());
            if (random.nextFloat() > 0.7F) {
                return null;
            }
            if (richnessFunction.compute(context) >= 0.0) {
                return null;
            }

            double richness = Util.clampedMap(absDensity, 0.4000000059604645, 0.6000000238418579, 0.10000000149011612, 0.30000001192092896);

            if (random.nextFloat() < richness && gapFunction.compute(context) > -0.30000001192092896) {
                return random.nextFloat() < CHANCE_OF_RAW_ORE_BLOCK ? veinType.rawOreBlock : veinType.ore;
            }

            return veinType.filler;
        };
    }

    protected enum VeinType {
        COPPER(Block.COPPER_ORE, Block.RAW_COPPER_BLOCK, Block.GRANITE, 0, 50),
        IRON(Block.DEEPSLATE_IRON_ORE, Block.RAW_IRON_BLOCK, Block.TUFF, -60, -8);

        final Block ore;
        final Block rawOreBlock;
        final Block filler;

        private final int minY;
        private final int maxY;

        VeinType(Block ore, Block rawOreBlock, Block filler, int minY, int maxY) {
            this.ore = ore;
            this.rawOreBlock = rawOreBlock;
            this.filler = filler;
            this.minY = minY;
            this.maxY = maxY;
        }
    }
}
