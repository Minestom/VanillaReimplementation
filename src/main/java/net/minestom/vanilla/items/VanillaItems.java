package net.minestom.vanilla.items;

import net.minestom.server.data.Data;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.BlockPosition;
import net.minestom.vanilla.blocks.VanillaBlocks;

import java.util.function.Supplier;

/**
 * All items with special behaviour available in the vanilla reimplementation
 */
public enum VanillaItems {

    WHITE_BED(() -> new BedItem(Material.WHITE_BED, VanillaBlocks.WHITE_BED)),
    BLACK_BED(() -> new BedItem(Material.BLACK_BED, VanillaBlocks.BLACK_BED)),
    LIGHT_BLUE_BED(() -> new BedItem(Material.LIGHT_BLUE_BED, VanillaBlocks.LIGHT_BLUE_BED)),
    BLUE_BED(() -> new BedItem(Material.BLUE_BED, VanillaBlocks.BLUE_BED)),
    RED_BED(() -> new BedItem(Material.RED_BED, VanillaBlocks.RED_BED)),
    GREEN_BED(() -> new BedItem(Material.GREEN_BED, VanillaBlocks.GREEN_BED)),
    YELLOW_BED(() -> new BedItem(Material.YELLOW_BED, VanillaBlocks.YELLOW_BED)),
    PURPLE_BED(() -> new BedItem(Material.PURPLE_BED, VanillaBlocks.PURPLE_BED)),
    MAGENTA_BED(() -> new BedItem(Material.MAGENTA_BED, VanillaBlocks.MAGENTA_BED)),
    CYAN_BED(() -> new BedItem(Material.CYAN_BED, VanillaBlocks.CYAN_BED)),
    PINK_BED(() -> new BedItem(Material.PINK_BED, VanillaBlocks.PINK_BED)),
    GRAY_BED(() -> new BedItem(Material.GRAY_BED, VanillaBlocks.GRAY_BED)),
    LIGHT_GRAY_BED(() -> new BedItem(Material.LIGHT_GRAY_BED, VanillaBlocks.LIGHT_GRAY_BED)),
    ORANGE_GRAY_BED(() -> new BedItem(Material.ORANGE_BED, VanillaBlocks.ORANGE_BED)),
    BROWN_BED(() -> new BedItem(Material.BROWN_BED, VanillaBlocks.BROWN_BED)),
    LIME_BED(() -> new BedItem(Material.LIME_BED, VanillaBlocks.LIME_BED)),
    FLINT_AND_STEEL(FlintAndSteel::new);

    private final Supplier<VanillaItem> itemCreator;

    private VanillaItems(Supplier<VanillaItem> itemCreator) {
        this.itemCreator = itemCreator;
    }

    /**
     * Register all vanilla items into the given manager
     * @param connectionManager used to add events to new players
     */
    public static void registerAll(ConnectionManager connectionManager) {
        connectionManager.addPlayerInitialization(player -> {
            for (VanillaItems itemDescription : values()) {
                VanillaItem item = itemDescription.itemCreator.get();
                player.addEventCallback(PlayerUseItemEvent.class, event -> {
                    if(event.getItemStack().getMaterial() == item.getMaterial()) {
                        item.onUseInAir(player, event.getItemStack(), event.getHand());
                    }
                });

                player.addEventCallback(PlayerBlockInteractEvent.class, event -> {
                    Instance instance = player.getInstance();
                    BlockPosition blockPosition = event.getBlockPosition();

                    // logic from Minestom core, allows containers to be opened even if the item has a use
                    CustomBlock customBlock = instance.getCustomBlock(blockPosition);
                    if (customBlock != null) {
                        Data data = instance.getBlockData(blockPosition);
                        boolean blocksItem = customBlock.onInteract(player, event.getHand(), blockPosition, data);
                        if(blocksItem) {
                            event.setBlockingItemUse(true);
                            event.setCancelled(true);
                        }
                    }

                    if(!event.isCancelled()) {
                        ItemStack itemStack = player.getItemInHand(event.getHand());
                        if(itemStack.getMaterial() == item.getMaterial()) {
                            if(item.onUseOnBlock(player, itemStack, event.getHand(), event.getBlockPosition(), event.getBlockFace().toDirection())) {
                                // prevent block placement
                                event.setBlockingItemUse(true);
                                event.setCancelled(true);
                            }
                        }
                    }
                });

                player.addEventCallback(PlayerUseItemOnBlockEvent.class, event -> {
                    if(event.getItemStack().getMaterial() == item.getMaterial()) {
                        item.onUseOnBlock(player, event.getItemStack(), event.getHand(), event.getPosition(), event.getBlockFace());
                    }
                });
            }
        });
    }
}
