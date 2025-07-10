package net.minestom.vanilla.fluids;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryKey;
import net.minestom.vanilla.fluids.impl.EmptyFluid;
import net.minestom.vanilla.fluids.impl.Fluid;
import net.minestom.vanilla.fluids.impl.LavaFluid;
import net.minestom.vanilla.fluids.impl.WaterFluid;
import net.minestom.vanilla.fluids.listener.FluidPlacementEvent;
import net.minestom.vanilla.fluids.pickup.FluidPickupListener;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MinestomFluids {
    private static boolean enabled = false;
    public static final Map<Instance, Map<Long, Set<Point>>> UPDATES = new ConcurrentHashMap<>();

    private static final DynamicRegistry<Fluid> registry = DynamicRegistry.create(Key.key("vri:fluids"));

    public static DynamicRegistry<Fluid> getRegistry() {
        return registry;
    }

    public static final RegistryKey<Fluid> EMPTY = registry.register("minecraft:empty", new EmptyFluid());

    public static RegistryKey<Fluid> getFluidOnBlock(Block block) {
        for (Fluid fluid : registry.values()) {
            if (fluid.isInTile(block)) {
                return registry.getKey(fluid);
            }
        }
        return EMPTY;
    }

    public static Fluid getFluidInstanceOnBlock(Block block) {
        for (Fluid fluid : registry.values()) {
            if (fluid.isInTile(block)) {
                return fluid;
            }
        }
        return registry.get(EMPTY);
    }

    public static void onTick(InstanceTickEvent event) {
        Map<Long, Set<Point>> instanceUpdates = UPDATES.computeIfAbsent(event.getInstance(), k -> new ConcurrentHashMap<>());
        Set<Point> currentUpdate = instanceUpdates.get(event.getInstance().getWorldAge());

        if (currentUpdate == null) return;

        for (Point point : currentUpdate) {
            processFluidTick(event.getInstance(), point);
        }

        UPDATES.get(event.getInstance()).remove(event.getInstance().getWorldAge());
    }

    public static void processFluidTick(Instance instance, Point point) {
        Block block = instance.getBlock(point);
        Fluid fluid = registry.get(getFluidOnBlock(block));

        fluid.onTick(instance, point, block);
        scheduleTick(instance, point, block);
    }

    public static void scheduleTick(Instance instance, Point point, Block block) {
        int tickDelay = registry.get(getFluidOnBlock(block))
            .getNextTickDelay(instance, point, block);

        if (tickDelay == -1) return;

        long newAge = instance.getWorldAge() + tickDelay;
        UPDATES.computeIfAbsent(instance, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(newAge, k -> new HashSet<>())
            .add(point);
    }

    private static EventNode<Event> events() {
        EventNode<Event> node = EventNode.all("fluid-events");
        node.addListener(InstanceTickEvent.class, MinestomFluids::onTick);
        node.addChild(FluidPickupListener.getFluidPickupEventNode());

        // Setup fluid placement event
        FluidPlacementEvent.setupFluidPlacementEvent();

        return node;
    }

    // Breaking water logging comment preserved
    /*
    private static void registerWaterloggedPlacementRules() {
        Block.values().forEach(block -> {
            if (MinecraftServer.getTagManager().getTag(Tag.BasicType.BLOCKS, "minecraft:stairs")
                    .contains(block.key())) {
                block.possibleStates().forEach(state -> {
                    String property = state.getProperty("waterlogged");
                    if (property != null && property.equals("true")) {
                        System.out.println("registered " + block.name());
                        MinecraftServer.getBlockManager().registerBlockPlacementRule(new FluidPlacementRule(block));
                    } else {
                        System.out.println("property is null");
                    }
                });
            }
        });
    }
    */

    public static void enableFluids() {
        if (enabled) return;

        enabled = true;
        MinecraftServer.getBlockManager().registerBlockPlacementRule(new FluidPlacementRule(Block.WATER));
        MinecraftServer.getBlockManager().registerBlockPlacementRule(new FluidPlacementRule(Block.LAVA));
        MinecraftServer.getBlockManager().registerBlockPlacementRule(new FluidPlacementRule(Block.AIR));
        MinecraftServer.getGlobalEventHandler().addChild(events());
    }

    public static void enableVanillaFluids() {
        if (!enabled) enableFluids();
        registry.register("minecraft:water", new WaterFluid(Block.WATER, Material.WATER_BUCKET));
        registry.register("minecraft:lava", new LavaFluid(Block.LAVA, Material.LAVA_BUCKET));
    }
}
