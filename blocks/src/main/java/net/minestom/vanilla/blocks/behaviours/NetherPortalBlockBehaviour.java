package net.minestom.vanilla.blocks.behaviours;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.blocks.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.blockupdatesystem.BlockUpdatable;
import net.minestom.vanilla.blockupdatesystem.BlockUpdateInfo;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;
import net.minestom.vanilla.system.nether.EntityEnterNetherPortalEvent;
import net.minestom.vanilla.system.nether.NetherPortalTeleportEvent;
import net.minestom.vanilla.system.nether.NetherPortalUpdateEvent;
import net.minestom.vanilla.system.NetherPortal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class NetherPortalBlockBehaviour extends VanillaBlockBehaviour implements BlockUpdatable {

    /**
     * Time the entity has spent inside a portal. Reset when entering a different portal or by
     * reentering a portal after leaving one
     */
    public static final Tag<Long> TICKS_SPENT_IN_PORTAL_KEY = Tag.Long("minestom:time_spent_in_nether_portal").defaultValue(0L);

    /**
     * Prevents multiple updates from different portal blocks
     */
    public static final Tag<Long> LAST_PORTAL_UPDATE_KEY = Tag.Long("minestom:last_nether_portal_update_time").defaultValue(Long.MAX_VALUE);

    /**
     * Used to check whether the last portal entered is corresponding to this portal block or not
     */
    public static final Tag<Long> LAST_PORTAL_KEY = Tag.Long("minestom:last_nether_portal");

    /**
     * Time before teleporting an entity
     */
    public static final Tag<Long> PORTAL_COOLDOWN_TIME_KEY = Tag.Long("minestom:nether_portal_cooldown_time").defaultValue(0L);

    /**
     * The portal related to this block
     */
    public static final Tag<Long> RELATED_PORTAL_KEY = Tag.Long("minestom:related_portal");

    public NetherPortalBlockBehaviour(@NotNull VanillaBlocks.BlockContext context) {
        super(context);
    }

    @Override
    public void onTouch(@NotNull Touch touch) {
        Block block = touch.getBlock();
        Instance instance = touch.getInstance();
        Point pos = touch.getBlockPosition();
        Entity touching = touch.getTouching();

        Long lastPortalUpdate = block.getTag(LAST_PORTAL_UPDATE_KEY);

        if (lastPortalUpdate == null) {
            return;
        }

        if (lastPortalUpdate < touching.getAliveTicks() - 2) { // if a tick happened with no portal update, that means the entity left the portal at some point
            Block newBlock = block
                    .withTag(LAST_PORTAL_UPDATE_KEY, 0L)
                    .withTag(TICKS_SPENT_IN_PORTAL_KEY, 0L);

            instance.setBlock(pos, newBlock);
            return;
        }

        if (lastPortalUpdate == touching.getAliveTicks()) {
            return;
        }

        NetherPortal portal = getPortal(block);
        long ticksSpentInPortal = updateTimeInPortal(instance, pos, touching, block, portal);

        Long portalCooldownTime = block.getTag(PORTAL_COOLDOWN_TIME_KEY);

        if (portalCooldownTime == null) {
            portalCooldownTime = 0L;
        }

        if (ticksSpentInPortal >= portalCooldownTime) {
            attemptTeleport(instance, touching, block, ticksSpentInPortal, portal);
        }
    }

    private long updateTimeInPortal(Instance instance, Point position, Entity touching, Block block, NetherPortal portal) {
        Block newBlock = block;

        newBlock = newBlock.withTag(LAST_PORTAL_UPDATE_KEY, touching.getAliveTicks());
        Long ticksSpentInPortal = block.getTag(TICKS_SPENT_IN_PORTAL_KEY);

        if (ticksSpentInPortal == null) {
            ticksSpentInPortal = 0L;
        }

        NetherPortal portalEntityWasIn = NetherPortal.fromId(newBlock.getTag(LAST_PORTAL_KEY));

        if (portal != portalEntityWasIn) {
            ticksSpentInPortal = 0L; // reset counter
        }

        newBlock = newBlock.withTag(LAST_PORTAL_KEY, portal.id()); // data.set(, portal, NetherPortal.class);

        if (ticksSpentInPortal == 0) {
            Event event = new EntityEnterNetherPortalEvent(touching, position, portal);

            MinecraftServer.getGlobalEventHandler().call(event);
        }

        ticksSpentInPortal++;

        newBlock = newBlock.withTag(TICKS_SPENT_IN_PORTAL_KEY, ticksSpentInPortal);

        instance.setBlock(position, newBlock);

        Event event = new NetherPortalUpdateEvent(touching, position, portal, instance, ticksSpentInPortal);

        MinecraftServer.getGlobalEventHandler().call(event);
        return ticksSpentInPortal;
    }

    private void attemptTeleport(Instance instance, Entity touching, Block block, long ticksSpentInPortal, NetherPortal portal) {
        DimensionType targetDimension;
        Point position = touching.getPosition();

        double targetX = position.x() / 8;
        double targetY = position.y();
        double targetZ = position.z() / 8;

        var key = instance.getDimensionType();
        DimensionType dimension = MinecraftServer.getDimensionTypeRegistry().get(key);
        if (dimension.effects().equals("nether")) {
            targetDimension = MinecraftServer.getDimensionTypeRegistry().get(DimensionType.OVERWORLD);
            targetX = position.x() * 8;
            targetZ = position.z() * 8;
        } else {
            targetDimension = VanillaDimensionTypes.OVERWORLD;
        }

        // TODO: event to change portal linking
        final DimensionType finalTargetDimension = targetDimension;
        Optional<Instance> potentialTargetInstance = MinecraftServer.getInstanceManager().getInstances().stream()
                .filter(in -> {
                    var key1 = in.getDimensionType();
                    return MinecraftServer.getDimensionTypeRegistry().get(key1) == targetDimension;
                })
                .findFirst();

        if (potentialTargetInstance.isEmpty()) {
            return;
        }

        Instance targetInstance = potentialTargetInstance.get();
        Pos targetPosition = new Pos(targetX, targetY, targetZ);
        NetherPortal targetPortal = getCorrespondingNetherPortal(targetInstance, targetPosition);

        boolean generatePortal = false;
        if (targetPortal == null) { // no existing portal, will create one

            NetherPortal.Axis axis = portal.getAxis();
            Pos bottomRight = new Pos(
                    targetX - axis.xMultiplier,
                    targetY - 1,
                    targetZ - axis.zMultiplier
            );

            Pos topLeft = new Pos(
                    targetX + 2 * axis.xMultiplier,
                    targetY + 3,
                    targetZ + 2 * axis.zMultiplier
            );

            targetPortal = new NetherPortal(portal.getAxis(), bottomRight, topLeft);
            generatePortal = true;
        }

        targetPosition = calculateTargetPosition(touching, portal, targetPortal);

        NetherPortalTeleportEvent event = new NetherPortalTeleportEvent(touching, position, portal, ticksSpentInPortal, targetInstance, targetPosition, targetPortal, generatePortal);
        MinecraftServer.getGlobalEventHandler().call(event);

        if (!event.isCancelled()) {
            Block newBlock = block
                    .withTag(LAST_PORTAL_UPDATE_KEY, 0L)
                    .withTag(LAST_PORTAL_KEY, portal.id())
                    .withTag(TICKS_SPENT_IN_PORTAL_KEY, 0L);
            instance.setBlock(position, newBlock);
            teleport(instance, touching, event);
        }
    }

    private @Nullable NetherPortal getCorrespondingNetherPortal(Instance targetInstance, Point targetPosition) {
        Block block = targetInstance.getBlock(targetPosition);
        return NetherPortal.fromId(block.getTag(RELATED_PORTAL_KEY));
    }

    private Pos calculateTargetPosition(Entity touching, NetherPortal portal, NetherPortal targetPortal) {
        Point targetCenter = targetPortal.getCenter();

        if (portal == null) { // if this block is not isolated
            return new Pos(
                    targetCenter.x() + 0.5,
                    targetCenter.y(),
                    targetCenter.z() + 0.5
            );
        }

        Pos touchingPos = touching.getPosition();
        double touchingX = touchingPos.x();
        double touchingY = touchingPos.y();
        double touchingZ = touchingPos.z();

        Vec portalCenter = portal.getCenter();
        double portalCenterX = portalCenter.x();
        double portalCenterY = portalCenter.y();
        double portalCenterZ = portalCenter.z();

        NetherPortal.Axis portalAxis = portal.getAxis();
        double portalAxisXMultiplier = portalAxis.xMultiplier;
        double portalAxisZMultiplier = portalAxis.zMultiplier;

        NetherPortal.Axis targetAxis = targetPortal.getAxis();
        double targetAxisXMultiplier = targetAxis.xMultiplier;
        double targetAxisZMultiplier = targetAxis.zMultiplier;

        double relativeX = (touchingX - portalCenterX) / (portal.computeWidth() * portalAxisXMultiplier + portalAxisZMultiplier);
        double relativeY = (touchingY - portalCenterY) / portal.computeHeight();
        double relativeZ = (touchingZ - portalCenterZ) / (portal.computeWidth() * portalAxisZMultiplier + portalAxisXMultiplier);

        double targetMultiplierX = (targetPortal.computeWidth() * targetAxisXMultiplier + targetAxisZMultiplier);
        double targetMultiplierY = targetPortal.computeHeight();
        double targetMultiplierZ = (targetPortal.computeWidth() * targetAxisZMultiplier + targetAxisXMultiplier);

        return new Pos(
                targetCenter.x() + relativeX * targetMultiplierX,
                targetCenter.y() + relativeY * targetMultiplierY,
                targetCenter.z() + relativeZ * targetMultiplierZ
        );
    }

    private void teleport(Instance instance, Entity touching, NetherPortalTeleportEvent event) {
        Instance targetInstance = event.getTargetInstance();
        if (event.createsNewPortal()) {
            event.getTargetPortal().generate(targetInstance);
        }

        if (targetInstance != instance) {
            touching.setInstance(targetInstance);
        }

        Pos targetTeleportationPosition = new Pos(event.getTargetPosition());

        touching.teleport(targetTeleportationPosition).thenRun(() -> {
            Vec velocity = touching.getVelocity();

            if (
                    event.getPortal() != null &&
                            event.getPortal().getAxis()
                                    != event.getTargetPortal().getAxis()
            ) {
                double swapTmp = velocity.x();

                touching.setVelocity(new Vec(
                        swapTmp,
                        velocity.z(),
                        swapTmp
                ));
            }
        });
    }

    @Override
    public void onDestroy(@NotNull Destroy destroy) {
        Block block = destroy.getBlock();
        Instance instance = destroy.getInstance();

        NetherPortal netherPortal = getPortal(block);
        if (netherPortal != null) {
            netherPortal.breakFrame(instance);
            netherPortal.unregister(instance);
        }
    }

    private NetherPortal getPortal(Block block) {
        return NetherPortal.fromId(block.getTag(RELATED_PORTAL_KEY));
    }

//    @Override
//    public void updateFromNeighbor(Instance instance, Point thisPosition, Point neighborPosition, boolean directNeighbor) {
//
//    }

    private void breakPortalIfNoLongerValid(Instance instance, Point blockPosition) {
        NetherPortal netherPortal = getPortal(instance.getBlock(blockPosition));

        if (netherPortal == null) {
            return;
        }

        if (netherPortal.isStillValid(instance)) {
            return;
        }

        netherPortal.breakFrame(instance);
    }

    public void setRelatedPortal(Instance instance, Point blockPosition, Block block, NetherPortal portal) {
        instance.setBlock(blockPosition, block.withTag(RELATED_PORTAL_KEY, portal.id()));
    }

    @Override
    public void blockUpdate(@NotNull Instance instance, @NotNull Point pos, @NotNull BlockUpdateInfo info) {
        breakPortalIfNoLongerValid(instance, pos);
    }
}
