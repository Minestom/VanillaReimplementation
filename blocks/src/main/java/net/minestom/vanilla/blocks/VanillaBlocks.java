package net.minestom.vanilla.blocks;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.blocks.redstone.LeverBlockHandler;
import net.minestom.vanilla.blocks.redstone.RedstoneBlockBlockHandler;
import net.minestom.vanilla.blocks.redstone.RedstoneWireBlockHandler;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * All blocks available in the vanilla reimplementation
 */
public enum VanillaBlocks {

    // Start of redstone
    LEVER(Block.LEVER, LeverBlockHandler::new),
    REDSTONE_BLOCK(Block.REDSTONE_BLOCK, RedstoneBlockBlockHandler::new),
    REDSTONE_WIRE(Block.REDSTONE_WIRE, RedstoneWireBlockHandler::new),

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
    JUKEBOX(Block.JUKEBOX, JukeboxBlockHandler::new);

    private final @NotNull Block block;
    private final @NotNull Context2Handler context2handler;

    VanillaBlocks(@NotNull Block block, @NotNull Context2Handler context2handler) {
        this.block = block;
        this.context2handler = context -> {
            if (!context.block().compare(block)) {
                throw new IllegalStateException("Block registry mismatch. Registered block: " + block +
                        " !=  Given block:" + context.block());
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
        @NotNull Block block();
        @NotNull VanillaReimplementation vri();
    }

    /**
     * Creates a block handler from the context
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
     * @param vri the vanilla reimplementation object
     */
    public static void registerAll(@NotNull VanillaReimplementation vri) {

        Map<String, BlockHandler> blockHandlers = new HashMap<>();

        for (VanillaBlocks vanillaBlock : values()) {
            BlockContext context = new BlockContext() {
                @Override
                public @NotNull Block block() {
                    return vanillaBlock.block;
                }

                @Override
                public @NotNull VanillaReimplementation vri() {
                    return vri;
                }
            };
            BlockHandler handler = vanillaBlock.context2handler.apply(context);
            blockHandlers.put(vanillaBlock.block.namespace().toString(), handler);
        }

        Map<String, BlockHandler> blockHandler2Namespace = Collections.unmodifiableMap(blockHandlers);

        // TODO: Update (& remove) once minestom has a general PlaceBlock event


        // Use reflection to inject the handlers
        injectHandlers(blockHandler2Namespace);
        vri.process().eventHandler().addListener(PlayerBlockPlaceEvent.class,
                event -> handlePlayerBlockPlaceEvent(event, blockHandler2Namespace));
    }

    private static void injectHandlers(Map<String, BlockHandler> blockHandlerByNamespace) {
        try {
            Class<Block> clazz = Block.class;

            for (Field field : clazz.getFields()) {
                field.setAccessible(true);

                Block someBlock = (Block) field.get(null);
                BlockHandler newHandler = blockHandlerByNamespace.get(someBlock.namespace().asString());
                someBlock = someBlock.withHandler(newHandler);

                // Apply default tag values if applicable
                if (newHandler instanceof VanillaBlockHandler) {
                    for (Map.Entry<Tag<?>, ?> entry : ((VanillaBlockHandler) newHandler).defaultTagValues().entrySet()) {
                        //noinspection unchecked, rawtypes
                        someBlock = someBlock.withTag((Tag) entry.getKey(), entry.getValue());
                    }
                }

                // Finally, replace default block handler
                if (newHandler != null) {
                    setFinalStatic(field, someBlock);
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to inject block handlers", e);
        }
    }

    private static void handlePlayerBlockPlaceEvent(PlayerBlockPlaceEvent event,
                                                    Map<String, BlockHandler> blockHandlerByNamespace) {
        Block oldBlock = event.getBlock();

        BlockHandler handler = blockHandlerByNamespace.get(oldBlock.namespace().asString());

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
            System.out.println("Fail!");
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
