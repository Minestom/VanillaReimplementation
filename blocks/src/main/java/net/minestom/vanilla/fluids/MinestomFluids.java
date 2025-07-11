package net.minestom.vanilla.fluids;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.vanilla.fluids.common.Fluid;
import net.minestom.vanilla.fluids.common.FluidState;
import net.minestom.vanilla.fluids.common.WaterlogHandler;
import net.minestom.vanilla.fluids.impl.EmptyFluid;
import net.minestom.vanilla.fluids.impl.LavaFluid;
import net.minestom.vanilla.fluids.impl.WaterFluid;
import net.minestom.vanilla.fluids.placement.FluidPlacementRule;
import net.minestom.vanilla.fluids.placement.LavaPlacementRule;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MinestomFluids {
	public static final Fluid WATER = new WaterFluid();
	public static final Fluid LAVA = new LavaFluid();
	public static final Fluid EMPTY = new EmptyFluid();
	
	public static final FluidState AIR_STATE = new FluidState(Block.AIR, EMPTY);
	
	private static final Map<Integer, WaterlogHandler> WATERLOG_HANDLERS = new ConcurrentHashMap<>();
	
	private static final Tag<Map<Long, Set<BlockVec>>> TICK_UPDATES = Tag.Transient("fluid-tick-updates");
	
	public static Fluid get(Block block) {
		if (block.compare(Block.WATER) || FluidState.isWaterlogged(block)) {
			return WATER;
		} else if (block.compare(Block.LAVA)) {
			return LAVA;
		} else {
			return EMPTY;
		}
	}
	
	public static void tick(InstanceTickEvent event) {
		Instance instance = event.getInstance();
		long age = instance.getWorldAge();
		
		var updates = instance.getTag(TICK_UPDATES);
		if (updates == null) {
			updates = new ConcurrentHashMap<>();
			instance.setTag(TICK_UPDATES, updates);
		}
		
		Set<BlockVec> currentUpdate = updates.remove(age);
		if (currentUpdate == null) return;
		
		for (BlockVec point : currentUpdate) {
			tick(event.getInstance(), point);
		}
	}
	
	public static void tick(Instance instance, BlockVec point) {
		FluidState state = FluidState.of(instance.getBlock(point));
		state.fluid().onTick(instance, point, state);
	}
	
	public static void scheduleTick(Instance instance, BlockVec point, FluidState state) {
		scheduleTick(instance, point, state.fluid().getNextTickDelay(instance, point));
	}
	
	public static void scheduleTick(Instance instance, BlockVec point, int tickDelay) {
		if (tickDelay == -1) return;
		
		var updates = instance.getTag(TICK_UPDATES);
		if (updates == null) {
			updates = new ConcurrentHashMap<>();
			instance.setTag(TICK_UPDATES, updates);
		}
		
		long newAge = instance.getWorldAge() + tickDelay;
		updates.computeIfAbsent(newAge, l -> new HashSet<>()).add(point);
	}
	
	public static void registerWaterlog(Block block, WaterlogHandler handler) {
		WATERLOG_HANDLERS.put(block.id(), handler);
	}
	
	public static WaterlogHandler getWaterlog(Block block) {
		return WATERLOG_HANDLERS.get(block.id());
	}
	
	public static void init() {
		MinecraftServer.getBlockManager().registerBlockPlacementRule(new FluidPlacementRule(Block.WATER));
		MinecraftServer.getBlockManager().registerBlockPlacementRule(new LavaPlacementRule(Block.LAVA));

		MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockInteractEvent.class, event -> {
		    Material itemMaterial = event.getPlayer().getItemInHand(event.getHand()).material();
		    Block block = event.getBlock();
		    Instance instance = event.getInstance();
		    BlockVec position = event.getBlockPosition();

		    if (itemMaterial == Material.WATER_BUCKET) {
		        WaterlogHandler handler = MinestomFluids.getWaterlog(block);
		        if (handler != null) {
		            handler.placeFluid(instance, position, MinestomFluids.WATER.getDefaultState());
		        } else {
		            instance.placeBlock(new BlockHandler.Placement(Block.WATER, instance, position.relative(event.getBlockFace())));
		        }
		    } else if (itemMaterial == Material.BUCKET) {
		        WaterlogHandler handler = MinestomFluids.getWaterlog(block);
		        FluidState state = FluidState.of(block);
		        if (handler != null && handler.canRemoveFluid(instance, position, state)) {
		            instance.setBlock(position, FluidState.setWaterlogged(block, false));
		        } else if (block.isLiquid()) {
		            event.getPlayer().setItemInHand(event.getHand(), state.fluid().getBucket());
		            instance.setBlock(position, Block.AIR);
		        }
		    } else if (itemMaterial == Material.LAVA_BUCKET) {
		        instance.placeBlock(new BlockHandler.Placement(Block.LAVA, instance, position.relative(event.getBlockFace())));
		    }
		});

		MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockBreakEvent.class, event -> {
		    if (FluidState.isWaterlogged(event.getBlock())) {
		        event.setResultBlock(Block.WATER);
		    }
		});

		MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockPlaceEvent.class, event -> {
		    Block originalBlock = event.getInstance().getBlock(event.getBlockPosition());
		    Fluid fluid = MinestomFluids.get(originalBlock);
		    if (fluid != MinestomFluids.EMPTY && FluidState.isSource(originalBlock) && FluidState.canBeWaterlogged(event.getBlock())) {
		        event.setBlock(FluidState.setWaterlogged(event.getBlock(), true));
		    }
		});

		for (Block block : Block.values()) {
			if (FluidState.canBeWaterlogged(block)) {
				registerWaterlog(block, WaterlogHandler.DEFAULT);
			}
		}
	}
	
	public static EventNode<Event> events() {
		EventNode<Event> node = EventNode.all("fluid-events");
		node.addListener(InstanceTickEvent.class, MinestomFluids::tick);
		return node;
	}
}
