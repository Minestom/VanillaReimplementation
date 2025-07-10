package net.minestom.vanilla.fluids.listener;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler.PlayerPlacement;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.vanilla.fluids.MinestomFluids;

public class FluidPlacementEvent {

    public static void setupFluidPlacementEvent() {
        MinecraftServer.getGlobalEventHandler()
            .addListener(PlayerBlockInteractEvent.class, event -> {
                if (event.isCancelled()) return;
                var instance = event.getPlayer().getInstance();
                var itemInMainHand = event.getPlayer().getItemInMainHand();

                if (itemInMainHand.equals(ItemStack.of(Material.LAVA_BUCKET))) {
                    var blockFace = event.getBlockFace();
                    var block = Block.LAVA;
                    var updated = instance.getBlock(event.getBlockPosition());
                    var placePosition = event.getBlockPosition().relative(blockFace).asVec().asPosition();

                    instance.placeBlock(
                        new PlayerPlacement(
                            block,
                            instance,
                            placePosition,
                            event.getPlayer(),
                            PlayerHand.MAIN,
                            blockFace,
                            (float) placePosition.x(),
                            (float) placePosition.y(),
                            (float) placePosition.z()
                        )
                    );

                    if (block != Block.LAVA) {
                        MinestomFluids.scheduleTick(instance, placePosition, Block.LAVA);
                        // Schedule updates for adjacent blocks (ensures fluid spreads properly)
                        for (BlockFace face : BlockFace.values()) {
                            var neighbor = placePosition.relative(face);
                            var neighborBlock = instance.getBlock(neighbor);
                            if (MinestomFluids.getFluidOnBlock(neighborBlock) != MinestomFluids.EMPTY) {
                                MinestomFluids.scheduleTick(instance, neighbor, neighborBlock);
                            }
                        }
                    }
                    event.getPlayer().refreshPosition(placePosition);
                }

                if (itemInMainHand.equals(ItemStack.of(Material.WATER_BUCKET))) {
                    var blockFace = event.getBlockFace();
                    Block block;
                    var updated = instance.getBlock(event.getBlockPosition());
                    Pos placePosition = event.getBlockPosition().relative(blockFace).asVec().asPosition();

                    if (isWaterloggable(updated)) {
                        block = updated.withProperty("waterlogged", "true");
                        placePosition = event.getBlockPosition().asVec().asPosition();
                    } else {
                        block = Block.WATER;
                    }

                    instance.placeBlock(
                        new PlayerPlacement(
                            block,
                            instance,
                            placePosition,
                            event.getPlayer(),
                            PlayerHand.MAIN,
                            blockFace,
                            (float) placePosition.x(),
                            (float) placePosition.y(),
                            (float) placePosition.z()
                        )
                    );

                    if (block != Block.WATER) {
                        // Schedule update for the current block
                        MinestomFluids.scheduleTick(instance, placePosition, Block.WATER);

                        // Schedule updates for adjacent blocks (ensures fluid spreads properly)
                        for (BlockFace face : BlockFace.values()) {
                            var neighbor = placePosition.relative(face);
                            var neighborBlock = instance.getBlock(neighbor);
                            if (MinestomFluids.getFluidOnBlock(neighborBlock) != MinestomFluids.EMPTY) {
                                MinestomFluids.scheduleTick(instance, neighbor, neighborBlock);
                            }
                        }
                    }
                    event.getPlayer().refreshPosition(placePosition, false, true);
                }
            });
    }

    public static boolean isWaterloggable(Block block) {
        var tags = Block.staticRegistry();
        if (tags.getTag(Key.key("minecraft:stairs")).contains(block)
            || tags.getTag(Key.key("minecraft:slabs")).contains(block)
            || tags.getTag(Key.key("minecraft:fences")).contains(block)
            || tags.getTag(Key.key("minecraft:trapdoors")).contains(block)
            // Commented out checks from original
            // if (block.compare(Block.LADDER)
            // || block.compare(Block.SUGAR_CANE)
            // || block.compare(Block.BUBBLE_COLUMN)
            // || block.compare(Block.NETHER_PORTAL)
            // || block.compare(Block.END_PORTAL)
            // || block.compare(Block.END_GATEWAY)
            // || block.compare(Block.KELP)
            // || block.compare(Block.KELP_PLANT)
            // || block.compare(Block.SEAGRASS)
            // || block.compare(Block.TALL_SEAGRASS)
            // || block.compare(Block.SEA_PICKLE)
            // || tags.getTag(Tag.BasicType.BLOCKS, "minecraft:signs")!!.contains(block.namespace())
            // || block.name().contains("door")
            // || block.name().contains("coral")
        ) {
            return true;
        }
        return false;
        // Additional commented out code from original
        // if (tags.getTag(Key.key("minecraft:stairs"))!!.contains(block)) {
        //     return true;
        // }
        // return !block.isSolid || !block.isAir
    }
}
