package net.minestom.vanilla.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.utils.BlockPosition;
import net.minestom.vanilla.system.NetherPortal;

/**
 * Called when a nether portal updates the counter inside an entity
 */
public class NetherPortalUpdateEvent extends Event {

    private final Entity entity;
    private final BlockPosition position;
    private final NetherPortal portal;
    private final long tickSpentInPortal;

    public NetherPortalUpdateEvent(Entity entity, BlockPosition position, NetherPortal portal, long tickSpentInPortal) {
        this.entity = entity;
        this.position = position;
        this.portal = portal;
        this.tickSpentInPortal = tickSpentInPortal;
    }

    public long getTickSpentInPortal() {
        return tickSpentInPortal;
    }

    public Entity getEntity() {
        return entity;
    }

    public BlockPosition getPosition() {
        return position;
    }

    public NetherPortal getPortal() {
        return portal;
    }
}
