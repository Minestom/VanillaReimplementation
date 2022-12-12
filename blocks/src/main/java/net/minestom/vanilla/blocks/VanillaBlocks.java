package net.minestom.vanilla.blocks;

import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.blocks.oxidisable.OxidatableHandler;
import net.minestom.vanilla.blocks.oxidisable.OxidatedHandler;
import net.minestom.vanilla.blocks.oxidisable.WaxedHandler;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * All blocks available in the vanilla reimplementation
 */
public enum VanillaBlocks {

    SAND(Block.SAND, GravityBlockHandler::new),
    RED_SAND(Block.RED_SAND, GravityBlockHandler::new),
    GRAVEL(Block.GRAVEL, GravityBlockHandler::new),

    // Start of concrete powders
    WHITE_CONCRETE_POWDER(Block.WHITE_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.WHITE_CONCRETE)),
    BLACK_CONCRETE_POWDER(Block.BLACK_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.BLACK_CONCRETE)),
    LIGHT_BLUE_CONCRETE_POWDER(Block.LIGHT_BLUE_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.LIGHT_BLUE_CONCRETE)),
    BLUE_CONCRETE_POWDER(Block.BLUE_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.BLUE_CONCRETE)),
    RED_CONCRETE_POWDER(Block.RED_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.RED_CONCRETE)),
    GREEN_CONCRETE_POWDER(Block.GREEN_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.GREEN_CONCRETE)),
    YELLOW_CONCRETE_POWDER(Block.YELLOW_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.YELLOW_CONCRETE)),
    PURPLE_CONCRETE_POWDER(Block.PURPLE_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.PURPLE_CONCRETE)),
    MAGENTA_CONCRETE_POWDER(Block.MAGENTA_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.MAGENTA_CONCRETE)),
    CYAN_CONCRETE_POWDER(Block.CYAN_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.CYAN_CONCRETE)),
    PINK_CONCRETE_POWDER(Block.PINK_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.PINK_CONCRETE)),
    GRAY_CONCRETE_POWDER(Block.GRAY_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.GRAY_CONCRETE)),
    LIGHT_GRAY_CONCRETE_POWDER(Block.LIGHT_GRAY_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.LIGHT_GRAY_CONCRETE)),
    ORANGE_CONCRETE_POWDER(Block.ORANGE_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.ORANGE_CONCRETE)),
    BROWN_CONCRETE_POWDER(Block.BROWN_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.BROWN_CONCRETE)),
    LIME_CONCRETE_POWDER(Block.LIME_CONCRETE_POWDER, (context) -> new ConcretePowderBlockHandler(context, Block.LIME_CONCRETE)),
    // End of concrete powders

    // Start of oxidisable copper
    // Blocks
    COPPER_BLOCK(Block.COPPER_BLOCK, (context) ->           new OxidatableHandler(context, Block.COPPER_BLOCK, Block.EXPOSED_COPPER, Block.WAXED_COPPER_BLOCK, 0)),
    EXPOSED_COPPER(Block.EXPOSED_COPPER, (context) ->       new OxidatableHandler(context, Block.COPPER_BLOCK, Block.WEATHERED_COPPER, Block.WAXED_EXPOSED_COPPER, 1)),
    WEATHERED_COPPER(Block.WEATHERED_COPPER, (context) ->   new OxidatableHandler(context, Block.EXPOSED_COPPER, Block.OXIDIZED_COPPER, Block.WAXED_WEATHERED_COPPER, 2)),
    OXIDIZED_COPPER(Block.OXIDIZED_COPPER, (context) ->     new OxidatableHandler(context, Block.WEATHERED_COPPER, Block.OXIDIZED_COPPER, Block.WAXED_OXIDIZED_COPPER, 3)),
    // Cut Blocks
    CUT_COPPER(Block.CUT_COPPER, (context) ->                     new OxidatableHandler(context, Block.CUT_COPPER, Block.EXPOSED_CUT_COPPER, Block.WAXED_CUT_COPPER, 0)),
    EXPOSED_CUT_COPPER(Block.EXPOSED_CUT_COPPER, (context) ->     new OxidatableHandler(context, Block.CUT_COPPER, Block.WEATHERED_CUT_COPPER, Block.WAXED_EXPOSED_CUT_COPPER, 1)),
    WEATHERED_CUT_COPPER(Block.WEATHERED_CUT_COPPER, (context) -> new OxidatableHandler(context, Block.EXPOSED_CUT_COPPER, Block.OXIDIZED_CUT_COPPER, Block.WAXED_WEATHERED_CUT_COPPER, 2)),
    OXIDIZED_CUT_COPPER(Block.OXIDIZED_CUT_COPPER, (context) ->   new OxidatableHandler(context, Block.WEATHERED_CUT_COPPER, Block.OXIDIZED_CUT_COPPER, Block.WAXED_OXIDIZED_CUT_COPPER, 3)),
    // Stairs
    CUT_COPPER_STAIRS(Block.CUT_COPPER_STAIRS, (context) ->                     new OxidatableHandler(context, Block.CUT_COPPER_STAIRS, Block.EXPOSED_CUT_COPPER_STAIRS, Block.WAXED_CUT_COPPER_STAIRS, 0)),
    EXPOSED_CUT_COPPER_STAIRS(Block.EXPOSED_CUT_COPPER_STAIRS, (context) ->     new OxidatableHandler(context, Block.CUT_COPPER_STAIRS, Block.WEATHERED_CUT_COPPER_STAIRS, Block.WAXED_EXPOSED_CUT_COPPER_STAIRS, 1)),
    WEATHERED_CUT_COPPER_STAIRS(Block.WEATHERED_CUT_COPPER_STAIRS, (context) -> new OxidatableHandler(context, Block.EXPOSED_CUT_COPPER_STAIRS, Block.OXIDIZED_CUT_COPPER_STAIRS, Block.WAXED_WEATHERED_CUT_COPPER_STAIRS, 2)),
    OXIDIZED_CUT_COPPER_STAIRS(Block.OXIDIZED_CUT_COPPER_STAIRS, (context) ->   new OxidatableHandler(context, Block.WEATHERED_CUT_COPPER_STAIRS, Block.OXIDIZED_CUT_COPPER_STAIRS, Block.WAXED_OXIDIZED_CUT_COPPER_STAIRS, 3)),
    // Slabs
    CUT_COPPER_SLAB(Block.CUT_COPPER_SLAB, (context) ->                     new OxidatableHandler(context, Block.CUT_COPPER_SLAB, Block.EXPOSED_CUT_COPPER_SLAB, Block.WAXED_CUT_COPPER_SLAB, 0)),
    EXPOSED_CUT_COPPER_SLAB(Block.EXPOSED_CUT_COPPER_SLAB, (context) ->     new OxidatableHandler(context, Block.CUT_COPPER_SLAB, Block.WEATHERED_CUT_COPPER_SLAB, Block.WAXED_EXPOSED_CUT_COPPER_SLAB, 1)),
    WEATHERED_CUT_COPPER_SLAB(Block.WEATHERED_CUT_COPPER_SLAB, (context) -> new OxidatableHandler(context, Block.EXPOSED_CUT_COPPER_SLAB, Block.OXIDIZED_CUT_COPPER_SLAB, Block.WAXED_WEATHERED_CUT_COPPER_SLAB, 2)),
    OXIDIZED_CUT_COPPER_SLAB(Block.OXIDIZED_CUT_COPPER_SLAB, (context) ->   new OxidatableHandler(context, Block.WEATHERED_CUT_COPPER_SLAB, Block.OXIDIZED_CUT_COPPER_SLAB, Block.WAXED_OXIDIZED_CUT_COPPER_SLAB, 3)),
    // End of copper

    // Start of waxed copper
    // Blocks
    WAXED_COPPER_BLOCK(Block.WAXED_COPPER_BLOCK, (context) ->         new WaxedHandler(context, Block.COPPER_BLOCK, 0)),
    WAXED_EXPOSED_COPPER(Block.WAXED_EXPOSED_COPPER, (context) ->     new WaxedHandler(context, Block.EXPOSED_COPPER, 1)),
    WAXED_WEATHERED_COPPER(Block.WAXED_WEATHERED_COPPER, (context) -> new WaxedHandler(context, Block.WEATHERED_COPPER, 2)),
    WAXED_OXIDIZED_COPPER(Block.WAXED_OXIDIZED_COPPER, (context) ->   new WaxedHandler(context, Block.OXIDIZED_COPPER, 3)),
    // Cut Blocks
    WAXED_CUT_COPPER(Block.WAXED_CUT_COPPER, (context) ->                     new WaxedHandler(context, Block.CUT_COPPER, 0)),
    WAXED_EXPOSED_CUT_COPPER(Block.WAXED_EXPOSED_CUT_COPPER, (context) ->     new WaxedHandler(context, Block.EXPOSED_CUT_COPPER, 1)),
    WAXED_WEATHERED_CUT_COPPER(Block.WAXED_WEATHERED_CUT_COPPER, (context) -> new WaxedHandler(context, Block.WEATHERED_CUT_COPPER, 2)),
    WAXED_OXIDIZED_CUT_COPPER(Block.WAXED_OXIDIZED_CUT_COPPER, (context) ->   new WaxedHandler(context, Block.OXIDIZED_CUT_COPPER, 3)),
    // Stairs
    WAXED_CUT_COPPER_STAIRS(Block.WAXED_CUT_COPPER_STAIRS, (context) ->                     new WaxedHandler(context, Block.CUT_COPPER_STAIRS, 0)),
    WAXED_EXPOSED_CUT_COPPER_STAIRS(Block.WAXED_EXPOSED_CUT_COPPER_STAIRS, (context) ->     new WaxedHandler(context, Block.EXPOSED_CUT_COPPER_STAIRS, 1)),
    WAXED_WEATHERED_CUT_COPPER_STAIRS(Block.WAXED_WEATHERED_CUT_COPPER_STAIRS, (context) -> new WaxedHandler(context, Block.WEATHERED_CUT_COPPER_STAIRS, 2)),
    WAXED_OXIDIZED_CUT_COPPER_STAIRS(Block.WAXED_OXIDIZED_CUT_COPPER_STAIRS, (context) ->   new WaxedHandler(context, Block.OXIDIZED_CUT_COPPER_STAIRS, 3)),
    // Slabs
    WAXED_CUT_COPPER_SLAB(Block.WAXED_CUT_COPPER_SLAB, (context) ->                     new WaxedHandler(context, Block.CUT_COPPER_SLAB, 0)),
    WAXED_EXPOSED_CUT_COPPER_SLAB(Block.WAXED_EXPOSED_CUT_COPPER_SLAB, (context) ->     new WaxedHandler(context, Block.EXPOSED_CUT_COPPER_SLAB, 1)),
    WAXED_WEATHERED_CUT_COPPER_SLAB(Block.WAXED_WEATHERED_CUT_COPPER_SLAB, (context) -> new WaxedHandler(context, Block.WEATHERED_CUT_COPPER_SLAB, 2)),
    WAXED_OXIDIZED_CUT_COPPER_SLAB(Block.WAXED_OXIDIZED_CUT_COPPER_SLAB, (context) ->   new WaxedHandler(context, Block.OXIDIZED_CUT_COPPER_SLAB, 3)),
    // End of waxed copper

    // Start of beds
    WHITE_BED(Block.WHITE_BED, BedBlockHandler::new),
    BLACK_BED(Block.BLACK_BED, BedBlockHandler::new),
    LIGHT_BLUE_BED(Block.LIGHT_BLUE_BED, BedBlockHandler::new),
    BLUE_BED(Block.BLUE_BED, BedBlockHandler::new),
    RED_BED(Block.RED_BED, BedBlockHandler::new),
    GREEN_BED(Block.GREEN_BED, BedBlockHandler::new),
    YELLOW_BED(Block.YELLOW_BED, BedBlockHandler::new),
    PURPLE_BED(Block.PURPLE_BED, BedBlockHandler::new),
    MAGENTA_BED(Block.MAGENTA_BED, BedBlockHandler::new),
    CYAN_BED(Block.CYAN_BED, BedBlockHandler::new),
    PINK_BED(Block.PINK_BED, BedBlockHandler::new),
    GRAY_BED(Block.GRAY_BED, BedBlockHandler::new),
    LIGHT_GRAY_BED(Block.LIGHT_GRAY_BED, BedBlockHandler::new),
    ORANGE_BED(Block.ORANGE_BED, BedBlockHandler::new),
    BROWN_BED(Block.BROWN_BED, BedBlockHandler::new),
    LIME_BED(Block.LIME_BED, BedBlockHandler::new),
    // End of beds

    FIRE(Block.FIRE, FireBlockHandler::new),
    NETHER_PORTAL(Block.NETHER_PORTAL, NetherPortalBlockHandler::new),
    END_PORTAL(Block.END_PORTAL, EndPortalBlockHandler::new),

    TNT(Block.TNT, TNTBlockHandler::new),

    CHEST(Block.CHEST, ChestBlockHandler::new),
    TRAPPED_CHEST(Block.TRAPPED_CHEST, TrappedChestBlockHandler::new),
    ENDER_CHEST(Block.ENDER_CHEST, EnderChestBlockHandler::new),
    JUKEBOX(Block.JUKEBOX, JukeboxBlockHandler::new),

    // Start of cakes
    CAKE(Block.CAKE, CakeBlockHandler::new),
    CANDLE_CAKE(Block.CANDLE_CAKE, CakeBlockHandler::new),
    WHITE_CANDLE_CAKE(Block.WHITE_CANDLE_CAKE, CakeBlockHandler::new),
    ORANGE_CANDLE_CAKE(Block.ORANGE_CANDLE_CAKE, CakeBlockHandler::new),
    MAGENTA_CANDLE_CAKE(Block.MAGENTA_CANDLE_CAKE, CakeBlockHandler::new),
    LIGHT_BLUE_CANDLE_CAKE(Block.LIGHT_BLUE_CANDLE_CAKE, CakeBlockHandler::new),
    YELLOW_CANDLE_CAKE(Block.YELLOW_CANDLE_CAKE, CakeBlockHandler::new),
    LIME_CANDLE_CAKE(Block.LIME_CANDLE_CAKE, CakeBlockHandler::new),
    PINK_CANDLE_CAKE(Block.PINK_CANDLE_CAKE, CakeBlockHandler::new),
    GRAY_CANDLE_CAKE(Block.GRAY_CANDLE_CAKE, CakeBlockHandler::new),
    LIGHT_GRAY_CANDLE_CAKE(Block.LIGHT_GRAY_CANDLE_CAKE, CakeBlockHandler::new),
    CYAN_CANDLE_CAKE(Block.CYAN_CANDLE_CAKE, CakeBlockHandler::new),
    PURPLE_CANDLE_CAKE(Block.PURPLE_CANDLE_CAKE, CakeBlockHandler::new),
    BLUE_CANDLE_CAKE(Block.BLUE_CANDLE_CAKE, CakeBlockHandler::new),
    BROWN_CANDLE_CAKE(Block.BROWN_CANDLE_CAKE, CakeBlockHandler::new),
    GREEN_CANDLE_CAKE(Block.GREEN_CANDLE_CAKE, CakeBlockHandler::new),
    BLACK_CANDLE_CAKE(Block.BLACK_CANDLE_CAKE, CakeBlockHandler::new)
    // End of cakes

    ;
    private final @NotNull Block minestomBlock;
    private final @NotNull Context2Handler context2handler;

    VanillaBlocks(@NotNull Block minestomBlock, @NotNull Context2Handler context2handler) {
        this.minestomBlock = minestomBlock;
        this.context2handler = context -> {
            if (context.stateId() != minestomBlock.stateId()) {
                throw new IllegalStateException("Block registry mismatch. Registered block: " + minestomBlock.stateId() +
                        " !=  Given block:" + context.stateId());
            }
            return context2handler.apply(context);
        };
    }

    interface Context2Handler {
        @NotNull BlockHandler apply(@NotNull BlockContext context);
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
    public @NotNull BlockHandler create(@NotNull BlockContext context) {
        return context2handler.apply(context);
    }

    /**
     * Register all vanilla commands into the given blockManager. ConnectionManager will handle replacing the basic
     * block with its custom variant.
     *
     * @param vri      the vanilla reimplementation object
     * @param registry the block registry
     */
    public static void registerAll(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {

        Map<String, Block> newBlocks = new HashMap<>();
        for (VanillaBlocks vb : values()) {
            Block minestomBlock = vb.minestomBlock;
            BlockContext context = new BlockContext() {
                @Override
                public short stateId() {
                    return minestomBlock.stateId();
                }

                @Override
                public @NotNull VanillaReimplementation vri() {
                    return vri;
                }
            };
            BlockHandler handler = vb.context2handler.apply(context);
            newBlocks.put(minestomBlock.namespace().toString(), minestomBlock.withHandler(handler));
        }

        // Use reflection to inject the handlers
        injectHandlers(newBlocks);

        Map<String, Block> newBlocksCopy = Map.copyOf(newBlocks);
        newBlocksCopy.values().forEach(registry::register);

        // TODO: Update (& remove) once minestom has a general PlaceBlock event
        vri.process().eventHandler().addListener(PlayerBlockPlaceEvent.class,
                event -> handlePlayerBlockPlaceEvent(event, newBlocksCopy));
    }

    private static void injectHandlers(Map<String, Block> blockByNamespace) {
        try {
            Class<Block> clazz = Block.class;

            for (Field field : clazz.getFields()) {
                field.setAccessible(true);

                Block someBlock = (Block) field.get(null);
                someBlock = blockByNamespace.get(someBlock.namespace().asString());
                if (someBlock == null) continue;

                // Apply default tag values if applicable
                if (someBlock.handler() instanceof VanillaBlockHandler) {
                    for (Map.Entry<Tag<?>, ?> entry : ((VanillaBlockHandler) someBlock.handler()).defaultTagValues().entrySet()) {
                        //noinspection unchecked, rawtypes
                        someBlock = someBlock.withTag((Tag) entry.getKey(), entry.getValue());
                    }
                }
                blockByNamespace.put(someBlock.namespace().asString(), someBlock);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to inject block handlers", e);
        }
    }

    private static void handlePlayerBlockPlaceEvent(PlayerBlockPlaceEvent event,
                                                    Map<String, Block> blockByNamespace) {
        Block oldBlock = event.getBlock();

        BlockHandler handler = blockByNamespace.get(oldBlock.namespace().asString()).handler();

        if (handler == null) {
            return;
        }

        event.setBlock(oldBlock.withHandler(handler));
    }

    private static void setFinalStatic(final Field ourField, Object newValue) {
        try {
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            final Unsafe unsafe = (Unsafe) unsafeField.get(null);
            final Object staticFieldBase = unsafe.staticFieldBase(ourField);
            final long staticFieldOffset = unsafe.staticFieldOffset(ourField);
            unsafe.putObject(staticFieldBase, staticFieldOffset, newValue);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    public static void dropOnBreak(Instance instance, BlockPosition position) {
//        LootTable table = null;
//        LootTableManager lootTableManager = MinecraftServer.getLootTableManager();
//        CustomBlock customBlock = instance.getCustomBlock(position);
//        if (customBlock != null) {
//            table = customBlock.getLootTable(lootTableManager);
//        }
//        Block block = Block.fromStateId(instance.getBlockStateId(position));
//        Data lootTableArguments = new DataImpl();
//        // TODO: tool used, silk touch, etc.
//        try {
//            if (table == null) {
//                table = lootTableManager.load(NamespaceID.from("blocks/" + block.name().toLowerCase()));
//            }
//            List<ItemStack> stacks = table.generate(lootTableArguments);
//            for (ItemStack item : stacks) {
//                Position spawnPosition = new Position((float) (position.getX() + 0.2f + Math.random() * 0.6f), (float) (position.getY() + 0.5f), (float) (position.getZ() + 0.2f + Math.random() * 0.6f));
//                ItemEntity itemEntity = new ItemEntity(item, spawnPosition);
//
//                itemEntity.getVelocity().setX((float) (Math.random() * 2f - 1f));
//                itemEntity.getVelocity().setY((float) (Math.random() * 2f));
//                itemEntity.getVelocity().setZ((float) (Math.random() * 2f - 1f));
//
//                itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND);
//                itemEntity.setInstance(instance);
//            }
//        } catch (FileNotFoundException e) {
//            // ignore missing table
//        }
//    }
}
