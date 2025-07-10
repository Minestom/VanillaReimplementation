package net.minestom.vanilla.blocks.behavior;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import net.minestom.vanilla.blocks.event.CakeEatEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class CakeEatRule implements BlockHandler {
    private final Block block;
    private final int maxSlices = 7;
    private final RegistryTag<Block> candles = Block.staticRegistry().getTag(TagKey.ofHash("#minecraft:candles"));

    public CakeEatRule(Block block) {
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

        int currentSlices = Integer.parseInt(interaction.getBlock().getProperty("bites"));
        Block heldBlock = interaction.getPlayer().getItemInHand(interaction.getHand()).material().block();
        if (currentSlices == 0 && heldBlock != Block.AIR && candles.contains(heldBlock)) {
            return BlockHandler.super.onInteract(interaction);
        }

        EventDispatcher.callCancellable(
            new CakeEatEvent(
                interaction.getPlayer(),
                interaction.getBlock(),
                new BlockVec(interaction.getBlockPosition())
            ),
            () -> {
                if (currentSlices < (maxSlices - 1)) {
                    interaction.getInstance().setBlock(
                        interaction.getBlockPosition(),
                        interaction.getBlock().withProperty("bites", String.valueOf(currentSlices + 1))
                    );
                } else {
                    interaction.getInstance().setBlock(interaction.getBlockPosition(), Block.AIR);
                }
            }
        );
        return false;
    }
}
