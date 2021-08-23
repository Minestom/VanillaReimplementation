package net.minestom.vanilla.blocks;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * All blocks available in the vanilla reimplementation
 */
public enum VanillaBlocks {

    SAND(Block.SAND, () -> new GravityBlockHandler(Block.SAND)),
    RED_SAND(Block.RED_SAND, () -> new GravityBlockHandler(Block.RED_SAND)),
    GRAVEL(Block.GRAVEL, () -> new GravityBlockHandler(Block.GRAVEL)),

    // Start of concrete powders
    WHITE_CONCRETE_POWDER(Block.WHITE_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.WHITE_CONCRETE_POWDER, Block.WHITE_CONCRETE)),
    BLACK_CONCRETE_POWDER(Block.BLACK_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.BLACK_CONCRETE_POWDER, Block.BLACK_CONCRETE)),
    LIGHT_BLUE_CONCRETE_POWDER(Block.LIGHT_BLUE_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.LIGHT_BLUE_CONCRETE_POWDER, Block.LIGHT_BLUE_CONCRETE)),
    BLUE_CONCRETE_POWDER(Block.BLUE_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.BLUE_CONCRETE_POWDER, Block.BLUE_CONCRETE)),
    RED_CONCRETE_POWDER(Block.RED_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.RED_CONCRETE_POWDER, Block.RED_CONCRETE)),
    GREEN_CONCRETE_POWDER(Block.GREEN_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.GREEN_CONCRETE_POWDER, Block.GREEN_CONCRETE)),
    YELLOW_CONCRETE_POWDER(Block.YELLOW_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.YELLOW_CONCRETE_POWDER, Block.YELLOW_CONCRETE)),
    PURPLE_CONCRETE_POWDER(Block.PURPLE_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.PURPLE_CONCRETE_POWDER, Block.PURPLE_CONCRETE)),
    MAGENTA_CONCRETE_POWDER(Block.MAGENTA_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.MAGENTA_CONCRETE_POWDER, Block.MAGENTA_CONCRETE)),
    CYAN_CONCRETE_POWDER(Block.CYAN_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.CYAN_CONCRETE_POWDER, Block.CYAN_CONCRETE)),
    PINK_CONCRETE_POWDER(Block.PINK_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.PINK_CONCRETE_POWDER, Block.PINK_CONCRETE)),
    GRAY_CONCRETE_POWDER(Block.GRAY_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.GRAY_CONCRETE_POWDER, Block.GRAY_CONCRETE)),
    LIGHT_GRAY_CONCRETE_POWDER(Block.LIGHT_GRAY_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.LIGHT_GRAY_CONCRETE_POWDER, Block.LIGHT_GRAY_CONCRETE)),
    ORANGE_CONCRETE_POWDER(Block.ORANGE_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.ORANGE_CONCRETE_POWDER, Block.ORANGE_CONCRETE)),
    BROWN_CONCRETE_POWDER(Block.BROWN_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.BROWN_CONCRETE_POWDER, Block.BROWN_CONCRETE)),
    LIME_CONCRETE_POWDER(Block.LIME_CONCRETE_POWDER, () -> new ConcretePowderBlockHandler(Block.LIME_CONCRETE_POWDER, Block.LIME_CONCRETE)),
    // End of concrete powders

    // Start of beds
    WHITE_BED(Block.WHITE_BED, () -> new BedBlockHandler(Block.WHITE_BED)),
    BLACK_BED(Block.BLACK_BED, () -> new BedBlockHandler(Block.BLACK_BED)),
    LIGHT_BLUE_BED(Block.LIGHT_BLUE_BED, () -> new BedBlockHandler(Block.LIGHT_BLUE_BED)),
    BLUE_BED(Block.BLUE_BED, () -> new BedBlockHandler(Block.BLUE_BED)),
    RED_BED(Block.RED_BED, () -> new BedBlockHandler(Block.RED_BED)),
    GREEN_BED(Block.GREEN_BED, () -> new BedBlockHandler(Block.GREEN_BED)),
    YELLOW_BED(Block.YELLOW_BED, () -> new BedBlockHandler(Block.YELLOW_BED)),
    PURPLE_BED(Block.PURPLE_BED, () -> new BedBlockHandler(Block.PURPLE_BED)),
    MAGENTA_BED(Block.MAGENTA_BED, () -> new BedBlockHandler(Block.MAGENTA_BED)),
    CYAN_BED(Block.CYAN_BED, () -> new BedBlockHandler(Block.CYAN_BED)),
    PINK_BED(Block.PINK_BED, () -> new BedBlockHandler(Block.PINK_BED)),
    GRAY_BED(Block.GRAY_BED, () -> new BedBlockHandler(Block.GRAY_BED)),
    LIGHT_GRAY_BED(Block.LIGHT_GRAY_BED, () -> new BedBlockHandler(Block.LIGHT_GRAY_BED)),
    ORANGE_BED(Block.ORANGE_BED, () -> new BedBlockHandler(Block.ORANGE_BED)),
    BROWN_BED(Block.BROWN_BED, () -> new BedBlockHandler(Block.BROWN_BED)),
    LIME_BED(Block.LIME_BED, () -> new BedBlockHandler(Block.LIME_BED)),
    // End of beds

    FIRE(Block.FIRE, FireBlockHandler::new),
    NETHER_PORTAL(Block.NETHER_PORTAL, NetherPortalBlockHandler::new),
    END_PORTAL(Block.END_PORTAL, EndPortalBlockHandler::new),

    TNT(Block.TNT, TNTBlockHandler::new),

    CHEST(Block.CHEST, ChestBlockHandler::new),
    TRAPPED_CHEST(Block.TRAPPED_CHEST, TrappedChestBlockHandler::new),
    ENDER_CHEST(Block.ENDER_CHEST, EnderChestBlockHandler::new),
    JUKEBOX(Block.JUKEBOX, JukeboxBlockHandler::new);

    private static final Map<Block, BlockHandler> blockHandlerByOldBlocks = new HashMap<>();

    private final @NotNull Block oldBlock;
    private final @NotNull BlockHandler blockHandler;

    /**
     * @param block the block to register the handler on
     * @param blockHandlerSupplier the handler supplier to register
     */
    VanillaBlocks(@NotNull Block block, @NotNull Supplier<BlockHandler> blockHandlerSupplier) {
        this.oldBlock = block;
        this.blockHandler = blockHandlerSupplier.get();
    }

    /**
     * Register all vanilla commands into the given blockManager. ConnectionManager will handle replacing the basic
     * block with its custom variant.
     *
     * @param eventHandler the event handler to register events on
     */
    public static void registerAll(EventNode<Event> eventHandler) {
        for (VanillaBlocks vanillaBlock : values()) {
            blockHandlerByOldBlocks.put(vanillaBlock.oldBlock, vanillaBlock.blockHandler);
        }

        eventHandler.addListener(
                EventListener.of(
                        PlayerBlockPlaceEvent.class,
                        VanillaBlocks::handlePlayerBlockPlaceEvent
                )
        );
    }

    private static void handlePlayerBlockPlaceEvent(PlayerBlockPlaceEvent event) {
        Block oldBlock = event.getBlock();

        BlockHandler handler = blockHandlerByOldBlocks.get(oldBlock);

        if (handler == null) {
            return;
        }

        if (oldBlock.handler() == handler) {
            return;
        }

        event.setBlock(oldBlock.withHandler(handler));
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
