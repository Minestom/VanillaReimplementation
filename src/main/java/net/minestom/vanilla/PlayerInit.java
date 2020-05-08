package net.minestom.vanilla;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.data.SerializableData;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.entity.AddEntityToInstanceEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.world.Dimension;
import net.minestom.vanilla.blocks.NetherPortalBlock;
import net.minestom.vanilla.generation.VanillaTestGenerator;

public class PlayerInit {

    private static volatile InstanceContainer overworld;
    private static volatile InstanceContainer nether;

    static {
        VanillaTestGenerator noiseTestGenerator = new VanillaTestGenerator();
        overworld = MinecraftServer.getInstanceManager().createInstanceContainer();
        overworld.enableAutoChunkLoad(true);
        overworld.setChunkGenerator(noiseTestGenerator);
        overworld.setData(new SerializableData());

        nether = MinecraftServer.getInstanceManager().createInstanceContainer(Dimension.NETHER);
        nether.enableAutoChunkLoad(true);
        nether.setChunkGenerator(noiseTestGenerator);
        nether.setData(new SerializableData());

        // Load some chunks beforehand
        int loopStart = -2;
        int loopEnd = 2;
        for (int x = loopStart; x < loopEnd; x++)
            for (int z = loopStart; z < loopEnd; z++) {
                overworld.loadChunk(x, z);
                nether.loadChunk(x, z);
            }

        EventCallback<AddEntityToInstanceEvent> callback = event -> {
            event.getEntity().setData(new SerializableData());
            Data data = event.getEntity().getData();
            if(event.getEntity() instanceof Player) {
                data.set(NetherPortalBlock.PORTAL_COOLDOWN_TIME_KEY, 5*20L, Long.class);
            }
        };
        overworld.addEventCallback(AddEntityToInstanceEvent.class, callback);
        nether.addEventCallback(AddEntityToInstanceEvent.class, callback);
    }

    public static void init() {
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();

        connectionManager.addPlayerInitialization(player -> {
            player.addEventCallback(PlayerLoginEvent.class, event -> {
                event.setSpawningInstance(overworld);
            });

            player.addEventCallback(PlayerSpawnEvent.class, event -> {
                if(event.isFirstSpawn()) {
                    player.setGameMode(GameMode.CREATIVE);
                    player.teleport(new Position(0, 75, 0));
                    player.getInventory().addItemStack(new ItemStack(Material.OBSIDIAN, (byte) 1));
                    player.getInventory().addItemStack(new ItemStack(Material.FLINT_AND_STEEL, (byte) 1));
                }
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
