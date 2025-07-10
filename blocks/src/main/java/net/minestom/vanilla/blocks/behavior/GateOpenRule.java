package net.minestom.vanilla.blocks.behavior;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.Direction;
import net.minestom.vanilla.common.utils.DirectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class GateOpenRule implements BlockHandler {
    private final Block block;

    public GateOpenRule(Block block) {
        this.block = block;
    }

    @Override
    public @NotNull Key getKey() {
        return block != null ? block.key() : getKey().key();
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        if (interaction.getPlayer().isSneaking() && !interaction.getPlayer().getItemInMainHand().isAir())
            return true;

        Collection<Direction> allowedDirections = getAllowedDirections(interaction.getBlock());
        Direction direction = DirectionUtils.getNearestLookingDirection(interaction, allowedDirections).opposite();
        String bool = String.valueOf(!Boolean.parseBoolean(interaction.getBlock().getProperty("open")));

        interaction.getInstance().setBlock(interaction.getBlockPosition(),
            interaction.getBlock()
                .withProperty("open", bool)
                .withProperty("facing", direction.toString().toLowerCase())
        );
        return false;
    }

    public Collection<Direction> getAllowedDirections(Block block) {
        Direction currentDirection = Direction.valueOf(block.getProperty("facing").toUpperCase());
        return Arrays.asList(currentDirection, currentDirection.opposite());
    }
}
