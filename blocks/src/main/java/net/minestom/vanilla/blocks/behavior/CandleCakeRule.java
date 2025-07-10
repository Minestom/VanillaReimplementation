package net.minestom.vanilla.blocks.behavior;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.vanilla.blocks.event.CakeEatEvent;
import net.minestom.vanilla.blocks.placement.CandlePlacementRule;
import net.minestom.vanilla.common.item.DroppedItemFactory;
import net.minestom.vanilla.common.utils.BlockUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class CandleCakeRule implements BlockHandler {
    private final Block block;
    private static final Map<Block, Block> CAKE_CANDLE = new HashMap<>();

    static {
        // Reverse the map from CandlePlacementRule
        for (Map.Entry<Block, Block> entry : CandlePlacementRule.getCANDLE_CAKE().entrySet()) {
            CAKE_CANDLE.put(entry.getValue(), entry.getKey());
        }
    }

    public CandleCakeRule(Block block) {
        this.block = block;
    }

    @Override
    public @NotNull Key getKey() {
        return block.key();
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        if (interaction.getPlayer().isSneaking()) {
            return BlockHandler.super.onInteract(interaction);
        }

        EventDispatcher.callCancellable(
            new CakeEatEvent(
                interaction.getPlayer(),
                interaction.getBlock(),
                new BlockVec(interaction.getBlockPosition())
            ),
            () -> {
                interaction.getInstance().setBlock(
                    interaction.getBlockPosition(),
                    BlockUtil
                      .withDefaultHandler(Block.CAKE)
                      .withProperty("bites", "1")
                );

                Block candle = CAKE_CANDLE.get(block);
                if (candle != null) {
                    DroppedItemFactory.maybeDrop(
                        interaction.getInstance(),
                        interaction.getBlockPosition(),
                        candle
                    );
                }
            }
        );
        return false;
    }
}
