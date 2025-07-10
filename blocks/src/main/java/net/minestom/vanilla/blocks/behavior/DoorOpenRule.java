package net.minestom.vanilla.blocks.behavior;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class DoorOpenRule implements BlockHandler {
    private final Block baseDoorBlock;

    public DoorOpenRule(Block baseDoorBlock) {
        this.baseDoorBlock = baseDoorBlock;
    }

    @Override
    public @NotNull Key getKey() {
        return baseDoorBlock.key();
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        var instance = interaction.getInstance();
        Point clickedPosition = interaction.getBlockPosition();
        Block clickedBlock = interaction.getBlock();

        String half = clickedBlock.getProperty("half");
        boolean isPowered = Boolean.parseBoolean(clickedBlock.getProperty("powered"));

        if (isPowered) {
            return false;
        }

        if (interaction.getPlayer().isSneaking() && !interaction.getPlayer().getItemInMainHand().isAir()) {
            return BlockHandler.super.onInteract(interaction);
        }

        boolean currentOpen = Boolean.parseBoolean(clickedBlock.getProperty("open"));
        boolean newOpen = !currentOpen;
        Point otherHalfPos = "lower".equals(half) ?
            clickedPosition.add(0.0, 1.0, 0.0) :
            clickedPosition.sub(0.0, 1.0, 0.0);

        Block otherHalfBlock = instance.getBlock(otherHalfPos);

        if (!otherHalfBlock.compare(clickedBlock) || half.equals(otherHalfBlock.getProperty("half"))) {
            return false;
        }

        String facing = clickedBlock.getProperty("facing");
        String hinge = clickedBlock.getProperty("hinge");

        Block updatedBlockState = clickedBlock
            .withProperty("facing", facing)
            .withProperty("hinge", hinge)
            .withProperty("open", String.valueOf(newOpen))
            .withProperty("powered", "false");

        instance.setBlock(clickedPosition, updatedBlockState.withProperty("half", half));
        instance.setBlock(otherHalfPos, updatedBlockState.withProperty("half", "lower".equals(half) ? "upper" : "lower"));

        return false;
    }
}
