package net.minestom.vanilla.event.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.vanilla.system.NetherPortal;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a entity starts colliding with a nether portal
 */
public class EntityEnterNetherPortalEvent implements Event, InstanceEvent, EntityEvent {

    private final Entity entity;
    private final Point position;
    private final NetherPortal portal;

    public EntityEnterNetherPortalEvent(Entity entity, Point position, NetherPortal portal) {
        this.entity = entity;
        this.position = position;
        this.portal = portal;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    /**
     * Position of the portal block which triggered the update
     * @return
     */
    public Point getPosition() {
        return position;
    }

    /**
     * The nether portal the entity is in. Can be null if the portal was added with /setblock
     * @return
     */
    public NetherPortal getPortal() {
        return portal;
    }

    @Override
    public @NotNull Instance getInstance() {
        return null;
    }
}
