package net.minestom.vanilla.blocks.event;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class CakeEatEvent implements Event, PlayerInstanceEvent, CancellableEvent, BlockEvent {
    private final Player player;
    private final Block block;
    private final BlockVec blockPosition;
    private boolean cancelled = false;

    public CakeEatEvent(Player player, Block block, BlockVec blockPosition) {
        this.player = player;
        this.block = block;
        this.blockPosition = blockPosition;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull Block getBlock() {
        return block;
    }

    @Override
    public @NotNull BlockVec getBlockPosition() {
        return blockPosition;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull Instance getInstance() {
        return player.getInstance();
    }
}
