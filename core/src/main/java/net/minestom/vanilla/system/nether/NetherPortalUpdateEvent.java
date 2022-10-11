package net.minestom.vanilla.system.nether;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.vanilla.system.NetherPortal;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a nether portal updates the counter inside an entity
 */
public class NetherPortalUpdateEvent implements Event, EntityEvent, InstanceEvent {

    private final Entity entity;
    private final Point position;
    private final NetherPortal portal;
    private final Instance instance;
    private final long tickSpentInPortal;

    public NetherPortalUpdateEvent(Entity entity, Point position, NetherPortal portal, Instance instance, long tickSpentInPortal) {
        this.entity = entity;
        this.position = position;
        this.portal = portal;
        this.instance = instance;
        this.tickSpentInPortal = tickSpentInPortal;
    }

    /**
     * Amount of time spent inside this portal
     */
    public long getTickSpentInPortal() {
        return tickSpentInPortal;
    }

    /**
     * Position of the portal block which triggered the update
     */
    public Point getPosition() {
        return position;
    }

    /**
     * The nether portal the entity is in. Can be null if the portal was added with /setblock
     */
    public NetherPortal getPortal() {
        return portal;
    }

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }

    @Override
    public @NotNull Instance getInstance() {
        return instance;
    }
}
