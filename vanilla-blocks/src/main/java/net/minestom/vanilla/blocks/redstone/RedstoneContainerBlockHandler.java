package net.minestom.vanilla.blocks.redstone;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.blocks.VanillaBlockHandler;
import net.minestom.vanilla.blocks.redstone.signal.RedstoneSignalManager;
import net.minestom.vanilla.blocks.redstone.signal.info.RedstoneSignal;
import net.minestom.vanilla.blocks.redstone.signal.info.RedstoneSignalTarget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.PriorityQueue;

/**
 * A block handler to make redstone signal use easier
 */
public abstract class RedstoneContainerBlockHandler extends VanillaBlockHandler {
    protected RedstoneContainerBlockHandler(@NotNull Block baseBlock) {
        super(baseBlock);
    }


    @Override
    // TODO: Remove this debug log:
    public boolean onInteract(@NotNull Interaction interaction) {
        Instance instance = interaction.getInstance();
        Point blockPosition = interaction.getBlockPosition();

        RedstoneSignal redstoneSignal = this.getRedstoneSignal(instance, blockPosition);

        if (redstoneSignal == null) {
            return true;
        }

        interaction.getPlayer().sendMessage(String.valueOf(redstoneSignal.strength()));

        return true;
    }

    /**
     * Gets the redstone signal at this position if applicable
     *
     * @param instance the instance to get the redstone signal from
     * @param point    the position to get the redstone signal from
     * @return the redstone signal, null if none
     */
    public @Nullable RedstoneSignal getRedstoneSignal(Instance instance, Point point) {
        var redstoneSignalTable = RedstoneSignalManager.of(instance).getSignalMap();
        PriorityQueue<RedstoneSignal> cachedRedstoneSignals = redstoneSignalTable.get(Pos.fromPoint(point));

        if (cachedRedstoneSignals == null) {
            return null;
        }

        return cachedRedstoneSignals.peek();
    }

    /**
     * This gets ran when a new redstone signal overrides an old one, or fills the absence with one.
     *
     * @param redstoneSignalTarget the redstone signal block
     * @param newRedstoneSignal    the new redstone signal info
     * @param oldRedstoneSignal    the old redstone signal info, null if not any
     */
    public void newRedstoneSignal(
            @NotNull RedstoneSignalTarget redstoneSignalTarget,
            @NotNull RedstoneSignal newRedstoneSignal,
            @Nullable RedstoneSignal oldRedstoneSignal
    ) {
    }

    /**
     * This gets ran when all redstone signals are removed
     *
     * @param redstoneSignalTarget the last redstone signal to be removed
     * @param redstoneSignal       the info of the last redstone signal
     */
    public void noRedstoneSignal(@NotNull RedstoneSignalTarget redstoneSignalTarget, RedstoneSignal redstoneSignal) {
    }
}
