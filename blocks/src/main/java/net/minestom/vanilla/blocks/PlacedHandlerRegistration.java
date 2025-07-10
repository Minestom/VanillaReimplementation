package net.minestom.vanilla.blocks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;

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
