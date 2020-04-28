package net.minestom.vanilla.blocks;

import net.minestom.server.event.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.network.ConnectionManager;

import java.util.function.Supplier;

/**
 * All blocks available in the vanilla reimplementation
 */
public enum VanillaBlocks {

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
                    System.out.println(block);
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
