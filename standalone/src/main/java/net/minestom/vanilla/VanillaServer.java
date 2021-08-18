package net.minestom.vanilla;

import net.minestom.server.MinecraftServer;

public class VanillaServer {
    public static void main(String[] args) {
        var server = MinecraftServer.init();


        server.start("0.0.0.0", 25565);
    }
}
