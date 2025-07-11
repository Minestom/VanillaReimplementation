package net.minestom.vanilla.blocks.behavior;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.vanilla.blocks.event.CopperOxidationEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class CopperOxidationRule implements BlockHandler {
    private final Block block;
    public static final Map<Block, Block> oxidationStages = new HashMap<>();

    static {
        oxidationStages.put(Block.COPPER_BLOCK, Block.EXPOSED_COPPER);
        oxidationStages.put(Block.EXPOSED_COPPER, Block.WEATHERED_COPPER);
        oxidationStages.put(Block.WEATHERED_COPPER, Block.OXIDIZED_COPPER);

        oxidationStages.put(Block.CUT_COPPER, Block.EXPOSED_CUT_COPPER);
        oxidationStages.put(Block.EXPOSED_CUT_COPPER, Block.WEATHERED_CUT_COPPER);
        oxidationStages.put(Block.WEATHERED_CUT_COPPER, Block.OXIDIZED_CUT_COPPER);

        oxidationStages.put(Block.CUT_COPPER_STAIRS, Block.EXPOSED_CUT_COPPER_STAIRS);
        oxidationStages.put(Block.EXPOSED_CUT_COPPER_STAIRS, Block.WEATHERED_CUT_COPPER_STAIRS);
        oxidationStages.put(Block.WEATHERED_CUT_COPPER_STAIRS, Block.OXIDIZED_CUT_COPPER_STAIRS);

        oxidationStages.put(Block.CUT_COPPER_SLAB, Block.EXPOSED_CUT_COPPER_SLAB);
        oxidationStages.put(Block.EXPOSED_CUT_COPPER_SLAB, Block.WEATHERED_CUT_COPPER_SLAB);
        oxidationStages.put(Block.WEATHERED_CUT_COPPER_SLAB, Block.OXIDIZED_CUT_COPPER_SLAB);

        oxidationStages.put(Block.CHISELED_COPPER, Block.EXPOSED_CHISELED_COPPER);
        oxidationStages.put(Block.EXPOSED_CHISELED_COPPER, Block.WEATHERED_CHISELED_COPPER);
        oxidationStages.put(Block.WEATHERED_CHISELED_COPPER, Block.OXIDIZED_CHISELED_COPPER);

        oxidationStages.put(Block.COPPER_GRATE, Block.EXPOSED_COPPER_GRATE);
        oxidationStages.put(Block.EXPOSED_COPPER_GRATE, Block.WEATHERED_COPPER_GRATE);
        oxidationStages.put(Block.WEATHERED_COPPER_GRATE, Block.OXIDIZED_COPPER_GRATE);

        oxidationStages.put(Block.COPPER_BULB, Block.EXPOSED_COPPER_BULB);
        oxidationStages.put(Block.EXPOSED_COPPER_BULB, Block.WEATHERED_COPPER_BULB);
        oxidationStages.put(Block.WEATHERED_COPPER_BULB, Block.OXIDIZED_COPPER_BULB);
    }

    public CopperOxidationRule(Block block) {
        this.block = block;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("vri:copper_oxidation");
    }

    public Block getNextOxidationStage() {
        return oxidationStages.get(block);
    }

    @Override
    public void tick(@NotNull Tick tick) {
        if (ThreadLocalRandom.current().nextInt(10000) > 1) return;

        Instance instance = tick.getInstance();
        Point blockPosition = tick.getBlockPosition();
        int exposedToAir = countExposedSides(instance, blockPosition);

        if (exposedToAir > 0 && ThreadLocalRandom.current().nextInt(100) < 20) {
            oxidizeBlock(instance, blockPosition, tick.getBlock());
        }
    }

    @Override
    public boolean isTickable() {
        return true;
    }

    private int countExposedSides(Instance instance, Point blockPosition) {
        int exposedSides = 0;

        for (BlockFace face : BlockFace.values()) {
            Point neighborPos = blockPosition.add(
                face.toDirection().vec()
            );
            Block neighborBlock = instance.getBlock(neighborPos);
            if (neighborBlock == Block.AIR) {
                exposedSides++;
            }
        }
        return exposedSides;
    }

    private void oxidizeBlock(Instance instance, Point blockPosition, Block block) {
        Block nextStage = getNextOxidationStage();
        if (nextStage == null) return;

        BlockHandler handler = MinecraftServer.getBlockManager().getHandler(nextStage.key().asString());
        if (handler != null) {
            nextStage = nextStage.withHandler(handler);
        }

        CopperOxidationEvent event = new CopperOxidationEvent(
            block,
            nextStage,
            new BlockVec(blockPosition),
            instance
        );

        EventDispatcher.callCancellable(event, () -> {
            instance.setBlock(blockPosition, event.getBlockAfterOxidation());
        });
    }
}
