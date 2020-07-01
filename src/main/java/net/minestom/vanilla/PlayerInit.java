package net.minestom.vanilla;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.Data;
import net.minestom.server.data.SerializableData;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.instance.ExplosionSupplier;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.storage.StorageManager;
import net.minestom.server.timer.TaskRunnable;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.Dimension;
import net.minestom.vanilla.anvil.AnvilChunkLoader;
import net.minestom.vanilla.blocks.NetherPortalBlock;
import net.minestom.vanilla.generation.VanillaTestGenerator;
import net.minestom.vanilla.instance.VanillaExplosion;

import java.io.FileNotFoundException;
import java.util.List;

public class PlayerInit {

    private static volatile InstanceContainer overworld;
    private static volatile InstanceContainer nether;

    static {
        ExplosionSupplier explosionGenerator = (centerX, centerY, centerZ, strength, additionalData) -> {
            boolean isTNT = additionalData != null ? additionalData.getOrDefault(VanillaExplosion.DROP_EVERYTHING_KEY, false) : false;
            boolean noBlockDamage = additionalData != null ? additionalData.getOrDefault(VanillaExplosion.DONT_DESTROY_BLOCKS_KEY, false) : false;
            return new VanillaExplosion(centerX, centerY, centerZ, strength, false, isTNT, !noBlockDamage);
        };
        StorageManager storageManager = MinecraftServer.getStorageManager();
        VanillaTestGenerator noiseTestGenerator = new VanillaTestGenerator();
        overworld = MinecraftServer.getInstanceManager().createInstanceContainer(Dimension.OVERWORLD, storageManager.getFolder("testworld/data")); // TODO: configurable
        overworld.enableAutoChunkLoad(true);
        overworld.setChunkGenerator(noiseTestGenerator);
        overworld.setData(new SerializableData());
        overworld.setExplosionSupplier(explosionGenerator);
        overworld.setChunkLoader(new AnvilChunkLoader(storageManager.getFolder("testworld/region")));

        nether = MinecraftServer.getInstanceManager().createInstanceContainer(Dimension.NETHER, MinecraftServer.getStorageManager().getFolder("testworld/DIM-1/data"));
        nether.enableAutoChunkLoad(true);
        nether.setChunkGenerator(noiseTestGenerator);
        nether.setData(new SerializableData());
        nether.setExplosionSupplier(explosionGenerator);
        nether.setChunkLoader(new AnvilChunkLoader(storageManager.getFolder("testworld/DIM-1/region")));

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

        MinecraftServer.getSchedulerManager().addShutdownTask(new TaskRunnable() {
            @Override
            public void run() {
                try {
                    overworld.saveInstance(() -> System.out.println("Overworld saved"));
                    nether.saveInstance(() -> System.out.println("Nether saved"));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void init() {
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();

        connectionManager.addPlayerInitialization(player -> {
            player.addEventCallback(PlayerLoginEvent.class, event -> {
                event.setSpawningInstance(overworld);
            });

            // anticheat method
            // but also prevents client and server fighting for player position after a teleport due to a Nether portal
            player.addEventCallback(PlayerMoveEvent.class, moveEvent -> {
                float currentX = player.getPosition().getX();
                float currentY = player.getPosition().getY();
                float currentZ = player.getPosition().getZ();
                float velocityX = player.getVelocity().getX();
                float velocityY = player.getVelocity().getY();
                float velocityZ = player.getVelocity().getZ();

                float dx = moveEvent.getNewPosition().getX()-currentX;
                float dy = moveEvent.getNewPosition().getY()-currentY;
                float dz = moveEvent.getNewPosition().getZ()-currentZ;

                float actualDisplacement = dx*dx+dy*dy+dz*dz;
                float expectedDisplacement = velocityX*velocityX+velocityY*velocityY+velocityZ*velocityZ;

                float upperLimit = 100; // TODO: 300 if elytra deployed

                if(actualDisplacement - expectedDisplacement >= upperLimit) {
                    moveEvent.setCancelled(true);
                    player.teleport(player.getPosition()); // force teleport to previous position
                    System.out.println(player.getUsername()+" moved too fast! "+dx+" "+dy+" "+dz);
                }
            });

            player.addEventCallback(PlayerBlockBreakEvent.class, event -> {
                LootTable table = null;
                LootTableManager lootTableManager = MinecraftServer.getLootTableManager();
                if(event.getResultCustomBlockId() != 0) {
                    CustomBlock customBlock = MinecraftServer.getBlockManager().getCustomBlock(event.getResultCustomBlockId());
                    table = customBlock.getLootTable(lootTableManager);
                }
                Block block = Block.fromId(player.getInstance().getBlockId(event.getBlockPosition()));
                Data lootTableArguments = new Data();
                // TODO: tool used, silk touch, etc.
                try {
                    if(table == null) {
                        table = lootTableManager.load(NamespaceID.from("blocks/" + block.name().toLowerCase()));
                    }
                    List<ItemStack> stacks = table.generate(lootTableArguments);
                    for (ItemStack item : stacks) {
                        Position spawnPosition = new Position((float) (event.getBlockPosition().getX() + 0.2f + Math.random() * 0.6f), (float) (event.getBlockPosition().getY() + 0.5f), (float) (event.getBlockPosition().getZ() + 0.2f + Math.random() * 0.6f));
                        ItemEntity itemEntity = new ItemEntity(item, spawnPosition);

                        itemEntity.getVelocity().setX((float) (Math.random()*2f-1f));
                        itemEntity.getVelocity().setY((float) (Math.random()*2f));
                        itemEntity.getVelocity().setZ((float) (Math.random()*2f-1f));

                        itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND);
                        itemEntity.setInstance(player.getInstance());
                    }
                } catch (FileNotFoundException e) {
                    // ignore missing table
                }
            });

            player.addEventCallback(PlayerSpawnEvent.class, event -> {
                if(event.isFirstSpawn()) {
                    player.setGameMode(GameMode.CREATIVE);
                    player.teleport(new Position(185, 100, 227));
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

                ItemEntity itemEntity = new ItemEntity(droppedItem, player.getPosition().clone().add(0, 1.5f, 0));
                itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND);
                itemEntity.setInstance(player.getInstance());
                Vector velocity = player.getPosition().clone().getDirection().multiply(6);
                itemEntity.setVelocity(velocity);
            });
        });
    }
}
