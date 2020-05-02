package net.minestom.vanilla;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.ItemDropEvent;
import net.minestom.server.event.PickupItemEvent;
import net.minestom.server.event.PlayerLoginEvent;
import net.minestom.server.event.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.vanilla.generation.VanillaTestGenerator;

public class PlayerInit {

    private static volatile InstanceContainer instanceContainer;

    static {
        VanillaTestGenerator noiseTestGenerator = new VanillaTestGenerator();
        instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer();
        instanceContainer.enableAutoChunkLoad(true);
        instanceContainer.setChunkGenerator(noiseTestGenerator);

        // Load some chunks beforehand
        int loopStart = -2;
        int loopEnd = 2;
        for (int x = loopStart; x < loopEnd; x++)
            for (int z = loopStart; z < loopEnd; z++) {
                instanceContainer.loadChunk(x, z);
            }
    }

    public static void init() {
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();

        connectionManager.addPlayerInitialization(player -> {
            player.addEventCallback(PlayerLoginEvent.class, event -> {
                event.setSpawningInstance(instanceContainer);
            });

            player.addEventCallback(PlayerSpawnEvent.class, event -> {
                player.setGameMode(GameMode.CREATIVE);
                player.teleport(new Position(0, 75, 0));

                ItemStack item = new ItemStack((short) 1, (byte) 43);
                item.setDisplayName("Item name");
                item.getLore().add("a lore line");
                player.getInventory().addItemStack(item);
                player.getInventory().addItemStack(new ItemStack((short) 1, (byte) 100));
                player.getInventory().addItemStack(new ItemStack(Material.DIAMOND_CHESTPLATE, (byte) 1));

            });

            player.addEventCallback(PickupItemEvent.class, event -> {
                boolean couldAdd = player.getInventory().addItemStack(event.getItemStack());
                event.setCancelled(!couldAdd); // Cancel event if player does not have enough inventory space
            });

            player.addEventCallback(ItemDropEvent.class, event -> {
                ItemStack droppedItem = event.getItemStack();

                ItemEntity itemEntity = new ItemEntity(droppedItem);
                itemEntity.setPickupDelay(500);
                itemEntity.refreshPosition(player.getPosition().clone().add(0, 1.5f, 0));
                itemEntity.setInstance(player.getInstance());
                Vector velocity = player.getPosition().clone().getDirection().multiply(6);
                itemEntity.setVelocity(velocity);
            });
        });
    }
}
