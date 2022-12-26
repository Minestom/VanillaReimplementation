package io.github.togar2.fluids;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.blockupdatesystem.BlockUpdateInfo;
import net.minestom.vanilla.blockupdatesystem.BlockUpdateManager;
import org.jetbrains.annotations.NotNull;

public class MinestomFluids {
    public static final Fluid LAVA = new LavaFluid();
    public static final Fluid WATER = new WaterFluid();
    public static final Fluid EMPTY = new EmptyFluid();
    private static final Short2ObjectMap<Fluid> fluidsByBlock = Short2ObjectMaps.synchronize(new Short2ObjectOpenHashMap<>());
    static {
        fluidsByBlock.put(LAVA.defaultBlock().stateId(), LAVA);
        fluidsByBlock.put(WATER.defaultBlock().stateId(), WATER);
        fluidsByBlock.put(EMPTY.defaultBlock().stateId(), EMPTY);
    }

    public static @NotNull Fluid fluidByBlock(Block block) {
        return fluidsByBlock.getOrDefault(block.stateId(), EMPTY);
    }

    public static void scheduleTick(Instance instance, Point point, Block block) {
        int tickDelay = MinestomFluids.fluidByBlock(block).nextTickDelay(instance, point, block);
        if (tickDelay == -1) return;
        BlockUpdateManager.from(instance).scheduleUpdate(point, BlockUpdateInfo.LIQUID_FLOW(), tickDelay);
    }

    public static void init(ServerProcess process) {
        process.block().registerBlockPlacementRule(new FluidPlacementRule(Block.WATER));
        process.block().registerBlockPlacementRule(new FluidPlacementRule(Block.LAVA));
        for (Fluid fluid : fluidsByBlock.values()) {
            BlockUpdateManager.registerUpdatable(fluid.defaultBlock,
                    (instance, pos, info) -> {
                        if (info instanceof BlockUpdateInfo.LiquidFlow) {
                            fluid.onTick(instance, pos, instance.getBlock(pos));
                            scheduleTick(instance, pos, instance.getBlock(pos));
                        }
                    });
        }
    }
}
