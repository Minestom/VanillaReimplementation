package io.github.togar2.fluids;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.blockupdatesystem.BlockUpdateInfo;
import net.minestom.vanilla.blockupdatesystem.BlockUpdateManager;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MinestomFluids {
    public static final Fluid LAVA = new LavaFluid();
    public static final Fluid WATER = new WaterFluid();
    public static final Fluid EMPTY = new EmptyFluid();
    private static final Short2ObjectMap<Fluid> fluidsByStateId = Short2ObjectMaps.synchronize(new Short2ObjectOpenHashMap<>());
    static {
        fluidsByStateId.put(LAVA.defaultBlock().stateId(), LAVA);
        fluidsByStateId.put(WATER.defaultBlock().stateId(), WATER);
        fluidsByStateId.put(EMPTY.defaultBlock().stateId(), EMPTY);
    }

    public static @NotNull Fluid fluidByBlock(Block block) {
        return fluidsByStateId.getOrDefault(block.stateId(), EMPTY);
    }

    public static void scheduleTick(Instance instance, Point point, Block block) {
        int tickDelay = MinestomFluids.fluidByBlock(block).nextTickDelay(instance, point, block);
        if (tickDelay == -1) return;
        BlockUpdateManager.from(instance).scheduleUpdate(point, BlockUpdateInfo.LIQUID_FLOW(), tickDelay);
    }

    public static void init(ServerProcess process) {
        process.block().registerBlockPlacementRule(new FluidPlacementRule(Block.WATER));
        process.block().registerBlockPlacementRule(new FluidPlacementRule(Block.LAVA));
        for (Fluid fluid : fluidsByStateId.values()) {
            BlockUpdateManager.registerUpdatable(fluid.defaultBlock().stateId(),
                    (instance, pos, info) -> {
                        if (info instanceof BlockUpdateInfo.LiquidFlow) {
                            fluid.onTick(instance, pos, instance.getBlock(pos));
                            scheduleTick(instance, pos, instance.getBlock(pos));
                        }
                    });
        }
    }

    public static EventNode<Event> events() {
        EventNode<Event> node = EventNode.all("fluid-events");
        return node;
    }
}
