package net.minestom.vanilla.blocks.oxidisable;

import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.blocks.VanillaBlockHandler;
import net.minestom.vanilla.blocks.VanillaBlocks;
import org.jetbrains.annotations.NotNull;

public abstract class OxidatedHandler extends WaxableBlockHandler implements OxygenSensitive {

    private final int oxidisedLevel;

    public OxidatedHandler(VanillaBlocks.@NotNull BlockContext context, Block waxed, int oxidisedLevel) {
        super(context, waxed);
        this.oxidisedLevel = oxidisedLevel;
    }

    @Override
    public int oxidisedLevel() {
        return oxidisedLevel;
    }
}