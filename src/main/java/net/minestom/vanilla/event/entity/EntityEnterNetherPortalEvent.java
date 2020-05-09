package net.minestom.vanilla.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.utils.BlockPosition;
import net.minestom.vanilla.system.NetherPortal;

/**
 * Called when a entity starts colliding with a nether portal
 */
public class EntityEnterNetherPortalEvent extends Event {

    private final Entity entity;
    private final BlockPosition position;
    private final NetherPortal portal;

    public EntityEnterNetherPortalEvent(Entity entity, BlockPosition position, NetherPortal portal) {
        this.entity = entity;
        this.position = position;
        this.portal = portal;
    }

    public Entity getEntity() {
        return entity;
    }

    /**
     * Position of the portal block which triggered the update
     * @return
     */
    public BlockPosition getPosition() {
        return position;
    }

    /**
     * The nether portal the entity is in. Can be null if the portal was added with /setblock
     * @return
     */
    public NetherPortal getPortal() {
        return portal;
    }
}
