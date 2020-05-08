package net.minestom.vanilla.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.world.Dimension;
import net.minestom.vanilla.system.NetherPortal;

/**
 * Triggered when a nether portal attempts to teleport entities between dimensions
 */
public class NetherPortalTeleportEvent extends CancellableEvent {


    private final Entity entity;
    private final BlockPosition portalBlockPosition;
    private final NetherPortal portal;
    private final long ticksSpentInPortal;
    private Dimension targetDimension;
    private Position targetPosition;
    private NetherPortal targetPortal;
    private boolean createNewPortal;

    public NetherPortalTeleportEvent(Entity entity, BlockPosition portalBlockPosition, NetherPortal portal, long ticksSpentInPortal, Dimension targetDimension, Position targetPosition, NetherPortal targetPortal, boolean createNewPortal) {
        this.entity = entity;
        this.portalBlockPosition = portalBlockPosition;
        this.portal = portal;
        this.ticksSpentInPortal = ticksSpentInPortal;
        this.targetDimension = targetDimension;
        this.targetPosition = targetPosition;
        this.targetPortal = targetPortal;
        this.createNewPortal = createNewPortal;
    }

    /**
     * Teleporting entity
     * @return
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Position of the portal block which triggered the teleportation
     * @return
     */
    public BlockPosition getPortalBlockPosition() {
        return portalBlockPosition;
    }

    /**
     * CAN BE NULL. The Nether portal trying to teleport an entity. Can be null if the portal block is not part of a nether portal frame
     * (for instance, placed with /setblock)
     * @return
     */
    public NetherPortal getPortal() {
        return portal;
    }

    /**
     * Number of ticks the entity spent in portal before this event
     * @return
     */
    public long getTicksSpentInPortal() {
        return ticksSpentInPortal;
    }

    /**
     * Dimension to teleport the entity to
     * @return
     */
    public Dimension getTargetDimension() {
        return targetDimension;
    }

    /**
     * Dimension to teleport the entity to
     */
    public void setTargetDimension(Dimension targetDimension) {
        this.targetDimension = targetDimension;
    }

    /**
     * Position to teleport the entity to. Set to the center of the linked portal, if available
     * @return
     */
    public Position getTargetPosition() {
        return targetPosition;
    }

    /**
     * Position to teleport the entity to. Set by default to the center of the linked portal, if available
     * @param targetPosition
     */
    public void setTargetPosition(Position targetPosition) {
        this.targetPosition = targetPosition;
    }

    /**
     * The linked Nether Portal to teleport to, if any
     * @return
     */
    public NetherPortal getTargetPortal() {
        return targetPortal;
    }

    /**
     * Set the portal to teleport to. Warning: the position to teleport the entity to is defined by {@link #getTargetPosition()}
     * @param targetPortal
     */
    public void setTargetPortal(NetherPortal targetPortal) {
        this.targetPortal = targetPortal;
    }

    /**
     * Should the teleportation create a new portal on the other side?
     * @return
     */
    public boolean createsNewPortal() {
        return createNewPortal;
    }

    /**
     * @see #createsNewPortal
     * @param createNewPortal
     */
    public void createsNewPortal(boolean createNewPortal) {
        this.createNewPortal = createNewPortal;
    }
}
