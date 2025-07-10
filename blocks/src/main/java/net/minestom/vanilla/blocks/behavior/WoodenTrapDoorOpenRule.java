package net.minestom.vanilla.blocks.behavior;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class WoodenTrapDoorOpenRule implements BlockHandler {
    private final Block block;

    public WoodenTrapDoorOpenRule(Block block) {
        this.block = block;
    }

    @Override
    public @NotNull Key getKey() {
        return block != null ? block.key() : getKey().key();
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        if (interaction.getPlayer().isSneaking() && !interaction.getPlayer().getItemInMainHand().isAir()) {
            return BlockHandler.super.onInteract(interaction);
        }

        String bool = interaction.getBlock().getProperty("open");
        if ("true".equals(bool)) {
            bool = "false";
        } else {
            bool = "true";
        }

        interaction.getInstance().setBlock(interaction.getBlockPosition(),
            interaction.getBlock().withProperty("open", bool));

        return false;
    }
}
