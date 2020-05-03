package net.minestom.vanilla.blocks;

import net.minestom.server.event.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.network.ConnectionManager;

import java.util.function.Supplier;

/**
 * All blocks available in the vanilla reimplementation
 */
public enum VanillaBlocks {

    SAND(() -> new GravityBlock(Block.SAND)),
    RED_SAND(() -> new GravityBlock(Block.RED_SAND)),
    GRAVEL(() -> new GravityBlock(Block.GRAVEL)),

    // Start of concrete powders
    WHITE_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.WHITE_CONCRETE_POWDER, Block.WHITE_CONCRETE)),
    BLACK_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.BLACK_CONCRETE_POWDER, Block.BLACK_CONCRETE)),
    LIGHT_BLUE_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.LIGHT_BLUE_CONCRETE_POWDER, Block.LIGHT_BLUE_CONCRETE)),
    BLUE_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.BLUE_CONCRETE_POWDER, Block.BLUE_CONCRETE)),
    RED_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.RED_CONCRETE_POWDER, Block.RED_CONCRETE)),
    GREEN_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.GREEN_CONCRETE_POWDER, Block.GREEN_CONCRETE)),
    YELLOW_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.YELLOW_CONCRETE_POWDER, Block.YELLOW_CONCRETE)),
    PURPLE_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.PURPLE_CONCRETE_POWDER, Block.PURPLE_CONCRETE)),
    MAGENTA_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.MAGENTA_CONCRETE_POWDER, Block.MAGENTA_CONCRETE)),
    CYAN_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.CYAN_CONCRETE_POWDER, Block.CYAN_CONCRETE)),
    PINK_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.PINK_CONCRETE_POWDER, Block.PINK_CONCRETE)),
    GRAY_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.GRAY_CONCRETE_POWDER, Block.GRAY_CONCRETE)),
    LIGHT_GRAY_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.LIGHT_GRAY_CONCRETE_POWDER, Block.LIGHT_GRAY_CONCRETE)),
    ORANGE_GRAY_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.ORANGE_CONCRETE_POWDER, Block.ORANGE_CONCRETE)),
    BROWN_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.BROWN_CONCRETE_POWDER, Block.BROWN_CONCRETE)),
    LIME_CONCRETE_POWDER(() -> new ConcretePowderBlock(Block.LIME_CONCRETE_POWDER, Block.LIME_CONCRETE)),
    // End of concrete powders

    CHEST(ChestBlock::new),
    TRAPPED_CHEST(TrappedChestBlock::new),
    ENDER_CHEST(EnderChestBlock::new),
    JUKEBOX(JukeboxBlock::new);

    private final VanillaBlockSupplier blockSupplier;
    private final BlockPlacementRule placementRule;

    private VanillaBlocks(VanillaBlockSupplier blockSupplier) {
        this(blockSupplier, null);
    }

    private VanillaBlocks(VanillaBlockSupplier blockSupplier, BlockPlacementRule placementRule) {
        this.blockSupplier = blockSupplier;
        this.placementRule = placementRule;
    }

    /**
     * Register this vanilla block to the given BlockManager, ConnectionManager is used to replace the basic block with its custom variant
     * @param connectionManager
     * @param blockManager
     */
    public void register(short customBlockID, ConnectionManager connectionManager, BlockManager blockManager) {
        VanillaBlock block = this.blockSupplier.create();
        connectionManager.addPlayerInitialization(player -> {
            player.addEventCallback(PlayerBlockPlaceEvent.class, event -> {
                if(event.getBlockId() == block.getBaseBlockId()) {
                    short blockID = block.getVisualBlockForPlacement(event.getPlayer(), event.getHand(), event.getBlockPosition());
                    event.setBlockId(blockID);
                    event.setCustomBlockId(block.getCustomBlockId());
                }
            });
        });
        blockManager.registerCustomBlock(block);
        if(placementRule != null) {
            blockManager.registerBlockPlacementRule(placementRule);
        }
    }

    /**
     * Register all vanilla commands into the given blockManager. ConnectionManager is used to replace the basic block with its custom counterpart
     * @param blockManager
     */
    public static void registerAll(ConnectionManager connectionManager, BlockManager blockManager) {
        for(VanillaBlocks vanillaBlock : values()) {
            vanillaBlock.register((short) vanillaBlock.ordinal(), connectionManager, blockManager);
        }
    }

    @FunctionalInterface
    private interface VanillaBlockSupplier {
        VanillaBlock create();
    }
}
