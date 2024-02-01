package net.minestom.vanilla.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.ExplosionSupplier;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.vanilla.generation.VanillaTestGenerator;
import net.minestom.vanilla.instance.VanillaExplosion;
import net.minestom.vanilla.logging.Logger;
import net.minestom.vanilla.system.ServerProperties;

public class VanillaEvents {

    public static void register(VanillaServer server, ServerProperties properties, EventNode<Event> eventNode) {
        String worldName = properties.get("level-name");

        ExplosionSupplier explosionGenerator = (centerX, centerY, centerZ, strength, additionalData) -> {

            boolean isTNT = additionalData != null && Boolean.TRUE.equals(additionalData.getBoolean(VanillaExplosion.DROP_EVERYTHING_KEY));
            boolean noBlockDamage = additionalData != null && Boolean.TRUE.equals(additionalData.getBoolean(VanillaExplosion.DONT_DESTROY_BLOCKS_KEY));

            return VanillaExplosion.builder(new Pos(centerX, centerY, centerZ), strength)
                    .destroyBlocks(!noBlockDamage)
                    .isFlaming(false)
                    .dropEverything(isTNT)
                    .build();
        };
        // TODO: World storage

        VanillaTestGenerator noiseTestGenerator = new VanillaTestGenerator();
        Instance overworld = server.overworld();
//        overworld.enableAutoChunkLoad(true);
//        overworld.setGenerator(noiseTestGenerator);
//        overworld.setExplosionSupplier(explosionGenerator);
//        overworld.setChunkLoader(new AnvilChunkLoader(storageManager.getLocation(worldName + "/region")));
//
//        nether = MinecraftServer.getInstanceManager().createInstanceContainer(VanillaDimensionTypes.NETHER);
//        nether.enableAutoChunkLoad(true);
//        nether.setGenerator(noiseTestGenerator);
//        nether.setExplosionSupplier(explosionGenerator);
//        nether.setChunkLoader(new AnvilChunkLoader(storageManager.getLocation(worldName + "/DIM-1/region")));
//
//        end = MinecraftServer.getInstanceManager().createInstanceContainer(VanillaDimensionTypes.END);
//        end.enableAutoChunkLoad(true);
//        end.setGenerator(noiseTestGenerator);
//        end.setExplosionSupplier(explosionGenerator);
//        end.setChunkLoader(new AnvilChunkLoader(storageManager.getLocation(worldName + "/DIM1/region")));

        // Load some chunks beforehand
        int loopStart = -2;
        int loopEnd = 2;
        for (int x = loopStart; x < loopEnd; x++)
            for (int z = loopStart; z < loopEnd; z++) {
                overworld.loadChunk(x, z);
            }

        eventNode.addListener(AddEntityToInstanceEvent.class, event -> {
            Entity entity = event.getEntity();

            if (entity instanceof Player) {
//                entity.setTag(NetherPortalBlockHandler.PORTAL_COOLDOWN_TIME_KEY, 5 * 20L);
            }
        });

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            try {
                overworld.saveInstance();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

        if (Boolean.parseBoolean(properties.get("online-mode"))) {
            MojangAuth.init();
        }

        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();

        eventNode.addListener(
                EventListener.of(AsyncPlayerConfigurationEvent.class, event -> {
                    event.setSpawningInstance(overworld);
                    Logger.info(event.getPlayer().getUsername() + " joined the server");
                })
        );

        eventNode.addListener(
                EventListener.of(PlayerDisconnectEvent.class, event -> Logger.info(event.getPlayer().getUsername() + " left the server"))
        );

        eventNode.addListener(
                PlayerPacketEvent.class,
                event -> {
                    ClientPacket packet = event.getPacket();
                    // moving around and keepalive packets aren't usually required
                    if (packet instanceof ClientPlayerPositionPacket) return;
                    if (packet instanceof ClientPlayerPositionAndRotationPacket) return;
                    if (packet instanceof ClientPlayerRotationPacket) return;
                    Logger.debug("Packet received " + event.getPacket());
                }
        );
        eventNode.addListener(
                PlayerPacketOutEvent.class,
                playerPacketOutEvent -> {
                    Logger.debug("Packet sent " + playerPacketOutEvent.getPacket());
                }
        );

        eventNode.addListener(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();
            Point pos = player.getPosition();
            Vec vel = player.getVelocity();

            double currentX = pos.x();
            double currentY = pos.y();
            double currentZ = pos.z();
            double velocityX = vel.x();
            double velocityY = vel.y();
            double velocityZ = vel.z();

            Point newPos = event.getNewPosition();

            double dx = newPos.x() - currentX;
            double dy = newPos.y() - currentY;
            double dz = newPos.z() - currentZ;

            double actualDisplacement = dx * dx + dy * dy + dz * dz;
            double expectedDisplacement = velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ;

            float upperLimit = 100; // TODO: 300 if elytra deployed

            if (actualDisplacement - expectedDisplacement >= upperLimit) {
                event.setCancelled(true);
                player.teleport(player.getPosition()); // force teleport to previous position
                Logger.warn(player.getUsername() + " moved too fast! " + dx + " " + dy + " " + dz);
            }
        });

        eventNode.addListener(
                EventListener.builder(PlayerSpawnEvent.class)
                        .filter(PlayerSpawnEvent::isFirstSpawn)
                        .handler(event -> {
                            Player player = event.getPlayer();

                            player.setPermissionLevel(4);
                            player.setGameMode(GameMode.CREATIVE);

                            player.getInstance().loadChunk(player.getPosition()).join();

                            int y = player.getInstance().getDimensionType().getMaxY();
                            while (player.getInstance().getBlock(1, y, 1).isAir()) {
                                y--;
                            }

                            player.teleport(new Pos(1, y + 1, 1));
                            PlayerInventory inventory = player.getInventory();

                            inventory.addItemStack(ItemStack.of(Material.OBSIDIAN, 1));
                            inventory.addItemStack(ItemStack.of(Material.FLINT_AND_STEEL, 1));
                            inventory.addItemStack(ItemStack.of(Material.RED_BED, 1));
                            inventory.addItemStack(ItemStack.of(Material.CHEST, 1));
                            inventory.addItemStack(ItemStack.of(Material.CRAFTING_TABLE, 1));
                            inventory.addItemStack(ItemStack.of(Material.OAK_LOG, 32));
                            inventory.addItemStack(ItemStack.of(Material.STONECUTTER, 1));
                            inventory.addItemStack(ItemStack.of(Material.STONE, 64));

                        })
                        .build()
        );

        eventNode.addListener(
                EventListener.builder(PickupItemEvent.class)
                        .filter(event -> event.getEntity() instanceof Player)
                        .handler(event -> {
                            Player player = (Player) event.getEntity();
                            boolean couldAdd = player.getInventory().addItemStack(event.getItemStack());
                            event.setCancelled(!couldAdd); // Cancel event if player does not have enough inventory space
                        })
                        .build()
        );

        eventNode.addListener(ItemDropEvent.class, event -> {
            Player player = event.getPlayer();
            ItemStack droppedItem = event.getItemStack();

            ItemEntity itemEntity = new ItemEntity(droppedItem);
            itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND);
            itemEntity.setInstance(player.getInstance());
            itemEntity.teleport(player.getPosition().add(0, 1.5f, 0));

            Vec velocity = player.getPosition().direction().mul(6);
            itemEntity.setVelocity(velocity);
        });
    }
}
