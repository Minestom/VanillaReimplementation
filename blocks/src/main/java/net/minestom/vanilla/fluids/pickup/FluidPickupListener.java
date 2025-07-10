package net.minestom.vanilla.fluids.pickup;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.Event;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler.PlayerPlacement;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.coordinate.Point;
import net.minestom.vanilla.common.utils.EntityUtils;
import net.minestom.vanilla.fluids.FluidUtils;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class FluidPickupListener {

    public static EventNode<Event> getFluidPickupEventNode() {
        return EventNode.all("fluid-pickup")
            .addListener(PlayerUseItemEvent.class, (event) -> {
                if (!event.getPlayer().getItemInMainHand().equals(ItemStack.of(Material.BUCKET))) {
                    return;
                }

                var instance = event.getPlayer().getInstance();

                Point eyePosition = EntityUtils.eyePosition(event.getPlayer());
                Point liquidBlock = FluidUtils.raycastForFluid(
                    event.getPlayer(),
                    eyePosition,
                    event.getPlayer().getPosition().direction(),
                    event.getPlayer().getAttributeValue(Attribute.BLOCK_INTERACTION_RANGE)
                );

                if (liquidBlock == null) {
                    return;
                }

                var blockFace = FluidUtils.findBlockFace(event.getPlayer(), liquidBlock);
                if (blockFace == null) {
                    return;
                }

                FluidPickupEvent pickupEvent = new FluidPickupEvent(
                    event.getInstance(),
                    event.getPlayer(),
                    event.getInstance().getBlock(liquidBlock),
                    new BlockVec(liquidBlock.x(), liquidBlock.y(), liquidBlock.z()),
                    Block.AIR
                );

                MinecraftServer.getGlobalEventHandler().callCancellable(pickupEvent, () -> {
                    instance.placeBlock(
                        new PlayerPlacement(
                            pickupEvent.getBlockToPlace(),
                            instance,
                            liquidBlock,
                            event.getPlayer(),
                            event.getHand(),
                            blockFace,
                            (float) liquidBlock.x(),
                            (float) liquidBlock.y(),
                            (float) liquidBlock.z()
                        )
                    );
                });
            });
    }
}
