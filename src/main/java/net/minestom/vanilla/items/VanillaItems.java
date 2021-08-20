package net.minestom.vanilla.items;

import net.minestom.server.data.Data;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * All items with special behaviour available in the vanilla reimplementation
 */
public enum VanillaItems {

    WHITE_BED(Material.WHITE_BED, BedItemHandler::new),
    BLACK_BED(Material.BLACK_BED, BedItemHandler::new),
    LIGHT_BLUE_BED(Material.LIGHT_BLUE_BED, BedItemHandler::new),
    BLUE_BED(Material.BLUE_BED, BedItemHandler::new),
    RED_BED(Material.RED_BED, BedItemHandler::new),
    GREEN_BED(Material.GREEN_BED, BedItemHandler::new),
    YELLOW_BED(Material.YELLOW_BED, BedItemHandler::new),
    PURPLE_BED(Material.PURPLE_BED, BedItemHandler::new),
    MAGENTA_BED(Material.MAGENTA_BED, BedItemHandler::new),
    CYAN_BED(Material.CYAN_BED, BedItemHandler::new),
    PINK_BED(Material.PINK_BED, BedItemHandler::new),
    GRAY_BED(Material.GRAY_BED, BedItemHandler::new),
    LIGHT_GRAY_BED(Material.LIGHT_GRAY_BED, BedItemHandler::new),
    ORANGE_BED(Material.ORANGE_BED, BedItemHandler::new),
    BROWN_BED(Material.BROWN_BED, BedItemHandler::new),
    LIME_BED(Material.LIME_BED, BedItemHandler::new),
    FLINT_AND_STEEL(Material.FLINT_AND_STEEL, FlintAndSteelHandler::new);

    private final Material material;
    private final Supplier<VanillaItemHandler> itemCreator;

    VanillaItems(@NotNull Material material, Supplier<VanillaItemHandler> itemCreator) {
        this.itemCreator = itemCreator;
        this.material = material;
    }

//    /**
//     * Register all vanilla items into the given manager
//     * @param connectionManager used to add events to new players
//     */
//    public static void generateItemManger(ConnectionManager connectionManager) {
//
//
//
//        connectionManager.addPlayerInitialization(player -> {
//            for (VanillaItems itemDescription : values()) {
//                VanillaItemHandler item = itemDescription.itemCreator.get();
//                player.addEventCallback(PlayerUseItemEvent.class, event -> {
//                    if(event.getItemStack().getMaterial() == item.getMaterial()) {
//                        item.onUseInAir(player, event.getItemStack(), event.getHand());
//                    }
//                });
//
//                player.addEventCallback(PlayerBlockInteractEvent.class, event -> {
//                    Instance instance = player.getInstance();
//                    BlockPosition blockPosition = event.getBlockPosition();
//
//                    // logic from Minestom core, allows containers to be opened even if the item has a use
//                    CustomBlock customBlock = instance.getCustomBlock(blockPosition);
//                    if (customBlock != null) {
//                        Data data = instance.getBlockData(blockPosition);
//                        boolean blocksItem = customBlock.onInteract(player, event.getHand(), blockPosition, data);
//                        if(blocksItem) {
//                            event.setBlockingItemUse(true);
//                            event.setCancelled(true);
//                        }
//                    }
//
//                    if(!event.isCancelled()) {
//                        ItemStack itemStack = player.getItemInHand(event.getHand());
//                        if(itemStack.getMaterial() == item.getMaterial()) {
//                            if(item.onUseOnBlock(player, itemStack, event.getHand(), event.getBlockPosition(), event.getBlockFace().toDirection())) {
//                                // prevent block placement
//                                event.setBlockingItemUse(true);
//                                event.setCancelled(true);
//                            }
//                        }
//                    }
//                });
//
//                player.addEventCallback(PlayerUseItemOnBlockEvent.class, event -> {
//                    if(event.getItemStack().getMaterial() == item.getMaterial()) {
//                        item.onUseOnBlock(player, event.getItemStack(), event.getHand(), event.getPosition(), event.getBlockFace());
//                    }
//                });
//            }
//        });
//    }

    public @NotNull Material getMaterial() {
        return material;
    }

    public @NotNull Supplier<VanillaItemHandler> getItemHandlerSupplier() {
        return itemCreator;
    }
}
