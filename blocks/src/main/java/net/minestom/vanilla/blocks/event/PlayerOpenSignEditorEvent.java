package net.minestom.vanilla.blocks.event;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class PlayerOpenSignEditorEvent implements Event, BlockEvent, CancellableEvent, PlayerInstanceEvent {
    private final Player player;
    private final BlockVec blockPosition;
    private final Block block;
    private boolean cancelled = false;

    public PlayerOpenSignEditorEvent(Player player, BlockVec blockPosition, Block block) {
        this.player = player;
        this.blockPosition = blockPosition;
        this.block = block;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockVec getBlockPosition() {
        return blockPosition;
    }

    @Override
    public Player getPlayer() {
        return player;
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
    public Instance getInstance() {
        return player.getInstance();
    }
}
