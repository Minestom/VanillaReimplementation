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
                ItemStack stack = event.getItemStack();
                ItemEntity itemEntity = new ItemEntity(stack);
                itemEntity.getPosition().setX(player.getPosition().getX());
                itemEntity.getPosition().setY(player.getPosition().getY()+1f/* TODO: Custom height? */);
                itemEntity.getPosition().setZ(player.getPosition().getZ());

                Vector throwDirection = itemEntity.getVelocity();
                // x = -cos(pitch) * sin(yaw)
                // y = -sin(pitch)
                // z =  cos(pitch) * cos(yaw)
                float pitch = (float) Math.toRadians(player.getPosition().getPitch());
                float yaw = (float)Math.toRadians(player.getPosition().getYaw());
                throwDirection.setX((float) (-Math.cos(pitch) * Math.sin(yaw)));
                throwDirection.setY((float) -Math.sin(pitch));
                throwDirection.setZ((float) (Math.cos(pitch) * Math.cos(yaw)));

                float throwSpeed = 3.5f;
                throwDirection.multiply(throwSpeed);
                throwDirection.setY(throwDirection.getY() + 5f);

                itemEntity.setPickupDelay(1000L*2);

                itemEntity.setInstance(player.getInstance());
            });
        });
    }
}
