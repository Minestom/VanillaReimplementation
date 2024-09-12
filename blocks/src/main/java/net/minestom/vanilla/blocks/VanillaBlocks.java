package net.minestom.vanilla.blocks;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.blocks.behaviours.*;
import net.minestom.vanilla.blocks.behaviours.oxidisable.OxidatableBlockBehaviour;
import net.minestom.vanilla.blocks.behaviours.oxidisable.WaxedBlockBehaviour;
import net.minestom.vanilla.blocks.behaviours.recipe.CampfireBehaviour;
import net.minestom.vanilla.blocks.behaviours.recipe.*;
import net.minestom.vanilla.blockupdatesystem.BlockUpdatable;
import net.minestom.vanilla.blockupdatesystem.BlockUpdateManager;
import net.minestom.vanilla.datapack.DatapackLoadingFeature;
import net.minestom.vanilla.randomticksystem.RandomTickManager;
import net.minestom.vanilla.randomticksystem.RandomTickable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * All blocks available in the vanilla reimplementation
 */
public enum VanillaBlocks {

    SAND(Block.SAND, GravityBlockBehaviour::new),
    RED_SAND(Block.RED_SAND, GravityBlockBehaviour::new),
    GRAVEL(Block.GRAVEL, GravityBlockBehaviour::new),

    // Start of concrete powders
    WHITE_CONCRETE_POWDER(Block.WHITE_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.WHITE_CONCRETE)),
    BLACK_CONCRETE_POWDER(Block.BLACK_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.BLACK_CONCRETE)),
    LIGHT_BLUE_CONCRETE_POWDER(Block.LIGHT_BLUE_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.LIGHT_BLUE_CONCRETE)),
    BLUE_CONCRETE_POWDER(Block.BLUE_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.BLUE_CONCRETE)),
    RED_CONCRETE_POWDER(Block.RED_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.RED_CONCRETE)),
    GREEN_CONCRETE_POWDER(Block.GREEN_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.GREEN_CONCRETE)),
    YELLOW_CONCRETE_POWDER(Block.YELLOW_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.YELLOW_CONCRETE)),
    PURPLE_CONCRETE_POWDER(Block.PURPLE_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.PURPLE_CONCRETE)),
    MAGENTA_CONCRETE_POWDER(Block.MAGENTA_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.MAGENTA_CONCRETE)),
    CYAN_CONCRETE_POWDER(Block.CYAN_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.CYAN_CONCRETE)),
    PINK_CONCRETE_POWDER(Block.PINK_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.PINK_CONCRETE)),
    GRAY_CONCRETE_POWDER(Block.GRAY_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.GRAY_CONCRETE)),
    LIGHT_GRAY_CONCRETE_POWDER(Block.LIGHT_GRAY_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.LIGHT_GRAY_CONCRETE)),
    ORANGE_CONCRETE_POWDER(Block.ORANGE_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.ORANGE_CONCRETE)),
    BROWN_CONCRETE_POWDER(Block.BROWN_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.BROWN_CONCRETE)),
    LIME_CONCRETE_POWDER(Block.LIME_CONCRETE_POWDER, (context) -> new ConcretePowderBlockBehaviour(context, Block.LIME_CONCRETE)),
    // End of concrete powders

    // Start of oxidisable copper
    // Blocks
    COPPER_BLOCK(Block.COPPER_BLOCK, (context) ->           new OxidatableBlockBehaviour(context, Block.COPPER_BLOCK, Block.EXPOSED_COPPER, Block.WAXED_COPPER_BLOCK, 0)),
    EXPOSED_COPPER(Block.EXPOSED_COPPER, (context) ->       new OxidatableBlockBehaviour(context, Block.COPPER_BLOCK, Block.WEATHERED_COPPER, Block.WAXED_EXPOSED_COPPER, 1)),
    WEATHERED_COPPER(Block.WEATHERED_COPPER, (context) ->   new OxidatableBlockBehaviour(context, Block.EXPOSED_COPPER, Block.OXIDIZED_COPPER, Block.WAXED_WEATHERED_COPPER, 2)),
    OXIDIZED_COPPER(Block.OXIDIZED_COPPER, (context) ->     new OxidatableBlockBehaviour(context, Block.WEATHERED_COPPER, Block.OXIDIZED_COPPER, Block.WAXED_OXIDIZED_COPPER, 3)),
    // Cut Blocks
    CUT_COPPER(Block.CUT_COPPER, (context) ->                     new OxidatableBlockBehaviour(context, Block.CUT_COPPER, Block.EXPOSED_CUT_COPPER, Block.WAXED_CUT_COPPER, 0)),
    EXPOSED_CUT_COPPER(Block.EXPOSED_CUT_COPPER, (context) ->     new OxidatableBlockBehaviour(context, Block.CUT_COPPER, Block.WEATHERED_CUT_COPPER, Block.WAXED_EXPOSED_CUT_COPPER, 1)),
    WEATHERED_CUT_COPPER(Block.WEATHERED_CUT_COPPER, (context) -> new OxidatableBlockBehaviour(context, Block.EXPOSED_CUT_COPPER, Block.OXIDIZED_CUT_COPPER, Block.WAXED_WEATHERED_CUT_COPPER, 2)),
    OXIDIZED_CUT_COPPER(Block.OXIDIZED_CUT_COPPER, (context) ->   new OxidatableBlockBehaviour(context, Block.WEATHERED_CUT_COPPER, Block.OXIDIZED_CUT_COPPER, Block.WAXED_OXIDIZED_CUT_COPPER, 3)),
    // Stairs
    CUT_COPPER_STAIRS(Block.CUT_COPPER_STAIRS, (context) ->                     new OxidatableBlockBehaviour(context, Block.CUT_COPPER_STAIRS, Block.EXPOSED_CUT_COPPER_STAIRS, Block.WAXED_CUT_COPPER_STAIRS, 0)),
    EXPOSED_CUT_COPPER_STAIRS(Block.EXPOSED_CUT_COPPER_STAIRS, (context) ->     new OxidatableBlockBehaviour(context, Block.CUT_COPPER_STAIRS, Block.WEATHERED_CUT_COPPER_STAIRS, Block.WAXED_EXPOSED_CUT_COPPER_STAIRS, 1)),
    WEATHERED_CUT_COPPER_STAIRS(Block.WEATHERED_CUT_COPPER_STAIRS, (context) -> new OxidatableBlockBehaviour(context, Block.EXPOSED_CUT_COPPER_STAIRS, Block.OXIDIZED_CUT_COPPER_STAIRS, Block.WAXED_WEATHERED_CUT_COPPER_STAIRS, 2)),
    OXIDIZED_CUT_COPPER_STAIRS(Block.OXIDIZED_CUT_COPPER_STAIRS, (context) ->   new OxidatableBlockBehaviour(context, Block.WEATHERED_CUT_COPPER_STAIRS, Block.OXIDIZED_CUT_COPPER_STAIRS, Block.WAXED_OXIDIZED_CUT_COPPER_STAIRS, 3)),
    // Slabs
    CUT_COPPER_SLAB(Block.CUT_COPPER_SLAB, (context) ->                     new OxidatableBlockBehaviour(context, Block.CUT_COPPER_SLAB, Block.EXPOSED_CUT_COPPER_SLAB, Block.WAXED_CUT_COPPER_SLAB, 0)),
    EXPOSED_CUT_COPPER_SLAB(Block.EXPOSED_CUT_COPPER_SLAB, (context) ->     new OxidatableBlockBehaviour(context, Block.CUT_COPPER_SLAB, Block.WEATHERED_CUT_COPPER_SLAB, Block.WAXED_EXPOSED_CUT_COPPER_SLAB, 1)),
    WEATHERED_CUT_COPPER_SLAB(Block.WEATHERED_CUT_COPPER_SLAB, (context) -> new OxidatableBlockBehaviour(context, Block.EXPOSED_CUT_COPPER_SLAB, Block.OXIDIZED_CUT_COPPER_SLAB, Block.WAXED_WEATHERED_CUT_COPPER_SLAB, 2)),
    OXIDIZED_CUT_COPPER_SLAB(Block.OXIDIZED_CUT_COPPER_SLAB, (context) ->   new OxidatableBlockBehaviour(context, Block.WEATHERED_CUT_COPPER_SLAB, Block.OXIDIZED_CUT_COPPER_SLAB, Block.WAXED_OXIDIZED_CUT_COPPER_SLAB, 3)),
    // End of copper

    // Start of waxed copper
    // Blocks
    WAXED_COPPER_BLOCK(Block.WAXED_COPPER_BLOCK, (context) ->         new WaxedBlockBehaviour(context, Block.COPPER_BLOCK, 0)),
    WAXED_EXPOSED_COPPER(Block.WAXED_EXPOSED_COPPER, (context) ->     new WaxedBlockBehaviour(context, Block.EXPOSED_COPPER, 1)),
    WAXED_WEATHERED_COPPER(Block.WAXED_WEATHERED_COPPER, (context) -> new WaxedBlockBehaviour(context, Block.WEATHERED_COPPER, 2)),
    WAXED_OXIDIZED_COPPER(Block.WAXED_OXIDIZED_COPPER, (context) ->   new WaxedBlockBehaviour(context, Block.OXIDIZED_COPPER, 3)),
    // Cut Blocks
    WAXED_CUT_COPPER(Block.WAXED_CUT_COPPER, (context) ->                     new WaxedBlockBehaviour(context, Block.CUT_COPPER, 0)),
    WAXED_EXPOSED_CUT_COPPER(Block.WAXED_EXPOSED_CUT_COPPER, (context) ->     new WaxedBlockBehaviour(context, Block.EXPOSED_CUT_COPPER, 1)),
    WAXED_WEATHERED_CUT_COPPER(Block.WAXED_WEATHERED_CUT_COPPER, (context) -> new WaxedBlockBehaviour(context, Block.WEATHERED_CUT_COPPER, 2)),
    WAXED_OXIDIZED_CUT_COPPER(Block.WAXED_OXIDIZED_CUT_COPPER, (context) ->   new WaxedBlockBehaviour(context, Block.OXIDIZED_CUT_COPPER, 3)),
    // Stairs
    WAXED_CUT_COPPER_STAIRS(Block.WAXED_CUT_COPPER_STAIRS, (context) ->                     new WaxedBlockBehaviour(context, Block.CUT_COPPER_STAIRS, 0)),
    WAXED_EXPOSED_CUT_COPPER_STAIRS(Block.WAXED_EXPOSED_CUT_COPPER_STAIRS, (context) ->     new WaxedBlockBehaviour(context, Block.EXPOSED_CUT_COPPER_STAIRS, 1)),
    WAXED_WEATHERED_CUT_COPPER_STAIRS(Block.WAXED_WEATHERED_CUT_COPPER_STAIRS, (context) -> new WaxedBlockBehaviour(context, Block.WEATHERED_CUT_COPPER_STAIRS, 2)),
    WAXED_OXIDIZED_CUT_COPPER_STAIRS(Block.WAXED_OXIDIZED_CUT_COPPER_STAIRS, (context) ->   new WaxedBlockBehaviour(context, Block.OXIDIZED_CUT_COPPER_STAIRS, 3)),
    // Slabs
    WAXED_CUT_COPPER_SLAB(Block.WAXED_CUT_COPPER_SLAB, (context) ->                     new WaxedBlockBehaviour(context, Block.CUT_COPPER_SLAB, 0)),
    WAXED_EXPOSED_CUT_COPPER_SLAB(Block.WAXED_EXPOSED_CUT_COPPER_SLAB, (context) ->     new WaxedBlockBehaviour(context, Block.EXPOSED_CUT_COPPER_SLAB, 1)),
    WAXED_WEATHERED_CUT_COPPER_SLAB(Block.WAXED_WEATHERED_CUT_COPPER_SLAB, (context) -> new WaxedBlockBehaviour(context, Block.WEATHERED_CUT_COPPER_SLAB, 2)),
    WAXED_OXIDIZED_CUT_COPPER_SLAB(Block.WAXED_OXIDIZED_CUT_COPPER_SLAB, (context) ->   new WaxedBlockBehaviour(context, Block.OXIDIZED_CUT_COPPER_SLAB, 3)),
    // End of waxed copper

    // Start of beds
    WHITE_BED(Block.WHITE_BED, BedBlockBehaviour::new),
    BLACK_BED(Block.BLACK_BED, BedBlockBehaviour::new),
    LIGHT_BLUE_BED(Block.LIGHT_BLUE_BED, BedBlockBehaviour::new),
    BLUE_BED(Block.BLUE_BED, BedBlockBehaviour::new),
    RED_BED(Block.RED_BED, BedBlockBehaviour::new),
    GREEN_BED(Block.GREEN_BED, BedBlockBehaviour::new),
    YELLOW_BED(Block.YELLOW_BED, BedBlockBehaviour::new),
    PURPLE_BED(Block.PURPLE_BED, BedBlockBehaviour::new),
    MAGENTA_BED(Block.MAGENTA_BED, BedBlockBehaviour::new),
    CYAN_BED(Block.CYAN_BED, BedBlockBehaviour::new),
    PINK_BED(Block.PINK_BED, BedBlockBehaviour::new),
    GRAY_BED(Block.GRAY_BED, BedBlockBehaviour::new),
    LIGHT_GRAY_BED(Block.LIGHT_GRAY_BED, BedBlockBehaviour::new),
    ORANGE_BED(Block.ORANGE_BED, BedBlockBehaviour::new),
    BROWN_BED(Block.BROWN_BED, BedBlockBehaviour::new),
    LIME_BED(Block.LIME_BED, BedBlockBehaviour::new),
    // End of beds

    FIRE(Block.FIRE, FireBlockBehaviour::new),
    NETHER_PORTAL(Block.NETHER_PORTAL, NetherPortalBlockBehaviour::new),
    END_PORTAL(Block.END_PORTAL, EndPortalBlockBehaviour::new),

    TNT(Block.TNT, TNTBlockBehaviour::new),

    CHEST(Block.CHEST, ChestBlockBehaviour::new),
    TRAPPED_CHEST(Block.TRAPPED_CHEST, TrappedChestBlockBehaviour::new),
    ENDER_CHEST(Block.ENDER_CHEST, EnderChestBlockBehaviour::new),
    JUKEBOX(Block.JUKEBOX, JukeboxBlockBehaviour::new),

    // recipes
    CRAFTING_TABLE(Block.CRAFTING_TABLE, CraftingTableBehaviour::new),
    FURNACE(Block.FURNACE, FurnaceBehaviour::new),
    SMOKER(Block.SMOKER, SmokerBehaviour::new),
    BLAST_FURNACE(Block.BLAST_FURNACE, BlastingFurnaceBehaviour::new),

    STONE_CUTTER(Block.STONECUTTER, StonecutterBehaviour::new),
    CAMPFIRE(Block.CAMPFIRE, CampfireBehaviour::new),
    SOUL_CAMPFIRE(Block.SOUL_CAMPFIRE, CampfireBehaviour::new),
    SMITHING_TABLE(Block.SMITHING_TABLE, SmithingTableBehaviour::new),

    // Start of cakes
    CAKE(Block.CAKE, CakeBlockBehaviour::new),
    CANDLE_CAKE(Block.CANDLE_CAKE, CakeBlockBehaviour::new),
    WHITE_CANDLE_CAKE(Block.WHITE_CANDLE_CAKE, CakeBlockBehaviour::new),
    ORANGE_CANDLE_CAKE(Block.ORANGE_CANDLE_CAKE, CakeBlockBehaviour::new),
    MAGENTA_CANDLE_CAKE(Block.MAGENTA_CANDLE_CAKE, CakeBlockBehaviour::new),
    LIGHT_BLUE_CANDLE_CAKE(Block.LIGHT_BLUE_CANDLE_CAKE, CakeBlockBehaviour::new),
    YELLOW_CANDLE_CAKE(Block.YELLOW_CANDLE_CAKE, CakeBlockBehaviour::new),
    LIME_CANDLE_CAKE(Block.LIME_CANDLE_CAKE, CakeBlockBehaviour::new),
    PINK_CANDLE_CAKE(Block.PINK_CANDLE_CAKE, CakeBlockBehaviour::new),
    GRAY_CANDLE_CAKE(Block.GRAY_CANDLE_CAKE, CakeBlockBehaviour::new),
    LIGHT_GRAY_CANDLE_CAKE(Block.LIGHT_GRAY_CANDLE_CAKE, CakeBlockBehaviour::new),
    CYAN_CANDLE_CAKE(Block.CYAN_CANDLE_CAKE, CakeBlockBehaviour::new),
    PURPLE_CANDLE_CAKE(Block.PURPLE_CANDLE_CAKE, CakeBlockBehaviour::new),
    BLUE_CANDLE_CAKE(Block.BLUE_CANDLE_CAKE, CakeBlockBehaviour::new),
    BROWN_CANDLE_CAKE(Block.BROWN_CANDLE_CAKE, CakeBlockBehaviour::new),
    GREEN_CANDLE_CAKE(Block.GREEN_CANDLE_CAKE, CakeBlockBehaviour::new),
    BLACK_CANDLE_CAKE(Block.BLACK_CANDLE_CAKE, CakeBlockBehaviour::new)
    // End of cakes

    ;
    private final short stateId;
    private final @NotNull Context2Handler context2handler;

    VanillaBlocks(@NotNull Block minestomBlock, @NotNull Context2Handler context2handler) {
        this.stateId = (short) minestomBlock.stateId();
        this.context2handler = context -> {
            if (context.stateId() != minestomBlock.stateId()) {
                throw new IllegalStateException("Block registry mismatch. Registered block: " + minestomBlock.stateId() +
                        " !=  Given block:" + context.stateId());
            }
            return context2handler.apply(context);
        };
    }

    interface Context2Handler {
        @NotNull VanillaBlockBehaviour apply(@NotNull BlockContext context);
    }

    /**
     * Used to provide context for creating block handlers
     */
    public interface BlockContext {
        short stateId();

        @NotNull VanillaReimplementation vri();
    }

    /**
     * Creates a block handler from the context
     *
     * @param context the context
     * @return the block handler
     */
    public @NotNull VanillaBlockBehaviour create(@NotNull BlockContext context) {
        return context2handler.apply(context);
    }

    /**
     * Register all vanilla blocks. ConnectionManager will handle replacing the basic
     * block with its custom variant.
     *
     * @param vri the vanilla reimplementation object
     */
    public static void registerAll(@NotNull VanillaReimplementation vri) {

        EventNode<Event> events = EventNode.all("vanilla-blocks");

        // block loot
        VanillaBlockLoot loot = new VanillaBlockLoot(vri, vri.feature(DatapackLoadingFeature.class).current());
        events.addListener(EventListener.builder(PlayerBlockBreakEvent.class)
                .filter(event -> !event.isCancelled())
                .filter(event -> event.getPlayer().getGameMode() != GameMode.CREATIVE)
                .handler(loot::spawnLoot)
                .build());

        Short2ObjectMap<VanillaBlockBehaviour> stateId2behaviour = new Short2ObjectOpenHashMap<>();

        for (VanillaBlocks vb : values()) {
            BlockContext context = new BlockContext() {
                @Override
                public short stateId() {
                    return vb.stateId;
                }

                @Override
                public @NotNull VanillaReimplementation vri() {
                    return vri;
                }
            };
            VanillaBlockBehaviour behaviour = vb.context2handler.apply(context);

            for (Block possibleState : Objects.requireNonNull(Block.fromStateId(vb.stateId)).possibleStates()) {
                short possibleStateId = (short) possibleState.stateId();
                stateId2behaviour.put(possibleStateId, behaviour);
            }

            if (behaviour instanceof BlockUpdatable updatable)
                BlockUpdateManager.registerUpdatable(vb.stateId, updatable);

            if (behaviour instanceof RandomTickable randomTickable)
                RandomTickManager.registerRandomTickable(vb.stateId, randomTickable);
        }

        registerEvents(events, stateId2behaviour);

        vri.process().eventHandler().addChild(events);
    }

    private static void registerEvents(EventNode<Event> node, Short2ObjectMap<VanillaBlockBehaviour> behaviours) {
        node.addListener(EventListener.builder(PlayerBlockPlaceEvent.class)
                .filter(event -> behaviours.containsKey((short) event.getBlock().stateId()))
                .handler(event -> {
                    short stateId = (short) event.getBlock().stateId();
                    Block block = Objects.requireNonNull(Block.fromStateId(stateId));
                    var behaviour = behaviours.get(stateId);

                    behaviour.onPlace(new PlayerPlacement(event));
                    Block blockToPlace = event.getBlock();
                    if (blockToPlace.compare(block)) {
                        event.setBlock(blockToPlace.withHandler(behaviour));
                    }
                })
                .build());
    }

    private record PlayerPlacement(PlayerBlockPlaceEvent event) implements VanillaBlockBehaviour.VanillaPlacement,
            VanillaBlockBehaviour.VanillaPlacement.HasPlayer {

        @Override
        public @NotNull Block blockToPlace() {
            return event.getBlock();
        }

        @Override
        public @NotNull Instance instance() {
            return event.getInstance();
        }

        @Override
        public @NotNull Point position() {
            return event.getBlockPosition();
        }

        @Override
        public void blockToPlace(@NotNull Block newBlock) {
            event.setBlock(newBlock);
        }

        @Override
        public @NotNull Player player() {
            return event.getPlayer();
        }
    }
}
