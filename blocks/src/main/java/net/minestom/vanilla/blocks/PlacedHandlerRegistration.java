package net.minestom.vanilla.blocks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class PlacedHandlerRegistration {

    public static void registerDefault() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockPlaceEvent.class, event -> {
            var handler = MinecraftServer.getBlockManager().getHandler(event.getBlock().key().asString());
            if (event.getBlock().handler() != handler) {
                event.setBlock(event.getBlock().withHandler(handler));
            }
        });
    }
}
