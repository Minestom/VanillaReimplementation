package net.minestom.vanilla.blocks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.world.Dimension;
import net.minestom.vanilla.blockentity.BlockEntity;
import net.minestom.vanilla.blockentity.NetherPortalBlockEntity;
import net.minestom.vanilla.data.NetherPortalList;
import net.minestom.vanilla.event.entity.EntityEnterNetherPortalEvent;
import net.minestom.vanilla.event.entity.NetherPortalTeleportEvent;
import net.minestom.vanilla.event.entity.NetherPortalUpdateEvent;
import net.minestom.vanilla.system.NetherPortal;

import java.util.Optional;

public class NetherPortalBlock extends VanillaBlock {

    public static final String TICKS_SPENT_IN_PORTAL_KEY = "minestom:time_spent_in_nether_portal";
    public static final String LAST_PORTAL_UPDATE_KEY = "minestom:last_nether_portal_update_time";
    public static final String LAST_PORTAL_KEY = "minestom:last_nether_portal";

    /**
     * (Data key) Time before teleporting an entity
     */
    public static final String PORTAL_COOLDOWN_TIME_KEY = "minestom:nether_portal_cooldown_time";

    public NetherPortalBlock() {
        super(Block.NETHER_PORTAL);
    }

    @Override
    public void handleContact(Instance instance, BlockPosition position, Entity touching) {
        super.handleContact(instance, position, touching);
        if(touching.getData() != null) {
            Data data = touching.getData();

            long lastPortalUpdate = data.getOrDefault(LAST_PORTAL_UPDATE_KEY, 0L);
            if(lastPortalUpdate < touching.getAliveTicks()-2) { // if a tick happened with no portal update, that means the entity left the portal at some point
                data.set(LAST_PORTAL_UPDATE_KEY, 0L, Long.class);
                data.set(LAST_PORTAL_KEY, NetherPortal.NONE, NetherPortal.class);
                data.set(TICKS_SPENT_IN_PORTAL_KEY, 0L, Long.class);
            }
            if(lastPortalUpdate != touching.getAliveTicks()) {
                data.set(LAST_PORTAL_UPDATE_KEY, touching.getAliveTicks(), Long.class);
                long ticksSpentInPortal = data.getOrDefault(TICKS_SPENT_IN_PORTAL_KEY, 0L);
                NetherPortal portal = getPortal(instance.getBlockData(position));

                NetherPortal portalEntityWasIn = data.getOrDefault(LAST_PORTAL_KEY, null);
                if(portal != portalEntityWasIn) {
                    ticksSpentInPortal = 0; // reset counter
                }
                data.set(LAST_PORTAL_KEY, portal, NetherPortal.class);

                if(ticksSpentInPortal == 0) {
                    touching.callEvent(EntityEnterNetherPortalEvent.class, new EntityEnterNetherPortalEvent(touching, position, portal));
                }
                ticksSpentInPortal++;
                data.set(TICKS_SPENT_IN_PORTAL_KEY, ticksSpentInPortal, Long.class);
                touching.callEvent(NetherPortalUpdateEvent.class, new NetherPortalUpdateEvent(touching, position, portal, ticksSpentInPortal));

                if(ticksSpentInPortal >= data.getOrDefault(PORTAL_COOLDOWN_TIME_KEY, 0L)) {
                    Dimension targetDimension = Dimension.NETHER;
                    Position targetPosition = new Position(position.getX()/8, position.getY(), position.getZ()/8);
                    if(instance.getDimension() == Dimension.NETHER) {
                        targetDimension = Dimension.OVERWORLD;
                        targetPosition.setX(position.getX()*8);
                        targetPosition.setZ(position.getZ()*8);
                    }

                    // TODO: event to change portal linking

                    final Dimension finalTargetDimension = targetDimension;
                    Optional<Instance> potentialTargetInstance = MinecraftServer.getInstanceManager().getInstances().stream()
                            .filter(in -> in.getDimension() == finalTargetDimension)
                            .findFirst();
                    if(!potentialTargetInstance.isPresent())
                        return;
                    Instance targetInstance = potentialTargetInstance.get();

                    NetherPortalList availablePortals = targetInstance.getData().get(NetherPortal.LIST_KEY);

                    NetherPortal targetPortal = null;
                    if(availablePortals != null) {
                        targetPortal = availablePortals.findClosest(targetPosition);
                    }

                    boolean createNewPortal = false;
                    if(targetPortal == null) { // no existing portal, will create one
                        BlockPosition bottomRight = targetPosition.toBlockPosition().add(-portal.getAxis().xMultiplier, -1, -portal.getAxis().zMultiplier);
                        BlockPosition topLeft = targetPosition.toBlockPosition().add(2*portal.getAxis().xMultiplier, 3, 2*portal.getAxis().zMultiplier);
                        targetPortal = new NetherPortal(portal.getAxis(), bottomRight, topLeft);
                        createNewPortal = true;
                    } else {
                        // TODO: compute relative position to this portal center and copy (with respect to frame size)
                        targetPosition.setX(targetPortal.getCenter().getX()+0.5f);
                        targetPosition.setZ(targetPortal.getCenter().getZ()+0.5f);
                        targetPosition.setY(targetPortal.getCenter().getY());
                    }

                    NetherPortalTeleportEvent event = new NetherPortalTeleportEvent(touching, position, portal, ticksSpentInPortal, targetDimension, targetPosition, targetPortal, createNewPortal);
                    touching.callCancellableEvent(NetherPortalTeleportEvent.class, event, () -> {
                        data.set(LAST_PORTAL_UPDATE_KEY, 0L, Long.class);
                        data.set(LAST_PORTAL_KEY, NetherPortal.NONE, NetherPortal.class);
                        data.set(TICKS_SPENT_IN_PORTAL_KEY, 0L, Long.class);
                        teleport(instance, touching, event);
                    });
                }
            }
        }
    }

    private void teleport(Instance instance, Entity touching, NetherPortalTeleportEvent event) {
        Dimension eventTargetDimension = event.getTargetDimension();
        Optional<Instance> potentialTargetInstance = MinecraftServer.getInstanceManager().getInstances().stream()
                .filter(in -> in.getDimension() == eventTargetDimension)
                .findFirst();
        if(!potentialTargetInstance.isPresent()) {
            return; // fail teleportation
        }
        Instance targetInstance = potentialTargetInstance.get();
        if(event.createsNewPortal()) {
            event.getTargetPortal().generate(targetInstance);
            System.out.println("CREATING NEW PORTAL ON OTHER SIDE "+event.getTargetPortal());
        }

        if(targetInstance != instance) {
            touching.setInstance(targetInstance);
        }
        Position targetTeleportationPosition = event.getTargetPosition();
        touching.teleport(targetTeleportationPosition, () -> {
            if(touching instanceof Player) {
                ((Player) touching).refreshAfterTeleport();
            }
        });
        System.out.println("teleporting to: "+targetTeleportationPosition);
    }

    @Override
    public Data createData(Instance instance, BlockPosition blockPosition, Data data) {
        if(data instanceof NetherPortalBlockEntity && ((BlockEntity) data).getPosition().equals(blockPosition))
            return data;
        return new NetherPortalBlockEntity(blockPosition);
    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {
        NetherPortal netherPortal = getPortal(data);
        if(netherPortal != null) {
            netherPortal.breakFrame(instance);
            netherPortal.unregister(instance);
        }
    }

    private NetherPortal getPortal(Data data) {
        if(data instanceof NetherPortalBlockEntity) {
            NetherPortalBlockEntity blockEntity = (NetherPortalBlockEntity)data;
            return blockEntity.getRelatedPortal();
        }
        return null;
    }

    @Override
    public void updateFromNeighbor(Instance instance, BlockPosition thisPosition, BlockPosition neighborPosition, boolean directNeighbor) {
        breakPortalIfNoLongerValid(instance, thisPosition);
    }

    private void breakPortalIfNoLongerValid(Instance instance, BlockPosition blockPosition) {
        NetherPortal netherPortal = getPortal(instance.getBlockData(blockPosition));
        if(netherPortal != null) {
            if(!netherPortal.isStillValid(instance)) {
                netherPortal.breakFrame(instance);
            }
        }
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList().property("axis", "x", "z");
    }
}
