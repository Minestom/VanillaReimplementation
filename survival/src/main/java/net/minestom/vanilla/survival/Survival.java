package net.minestom.vanilla.survival;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class Survival {
    public static void main(String[] args) {
        // Initialize the server
        MinecraftServer minecraftServer = MinecraftServer.init();

        // Pass it to survival and initialize
        new Survival(MinecraftServer.process()).initialize();
        Logger.info("Initialized vanilla.");

        minecraftServer.start("0.0.0.0", 25565);
    }

    private final @NotNull ServerProcess process;
    private final @NotNull Instance overworld;

    Survival(@NotNull ServerProcess process) {
        this.process = process;

        this.overworld = process.instance().createInstanceContainer(
                DimensionType.OVERWORLD,
                new AnvilLoader(Path.of("world"))
        );
    }

    /**
     * Initializes the server (event handlers, etc).
     */
    public void initialize() {

        process.eventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();

            // TODO: Determine respawn coordinates and radius, and then randomly pick a valid spot
            Pos respawnPoint = new Pos(0, 64, 0, 0, 0);

            event.setSpawningInstance(this.overworld);
            player.setRespawnPoint(respawnPoint);
        }).addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();

            if (event.isFirstSpawn()) {
                this.broadcast(Component.translatable("multiplayer.player.joined", NamedTextColor.YELLOW).arguments(player.getName()));
            }
        }).addListener(PlayerDisconnectEvent.class, event -> {
            final Player player = event.getPlayer();

            this.broadcast(Component.translatable("multiplayer.player.left", NamedTextColor.YELLOW).arguments(player.getName()));
        });
    }

    private void broadcast(@NotNull Component message) {
        for (Instance instance : process.instance().getInstances()) {
            instance.sendMessage(message);
        }
    }

}
