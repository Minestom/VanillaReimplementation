package net.minestom.vanilla.survival;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.crafting.CraftingFeature;
import net.minestom.vanilla.crafting.Recipe;
import net.minestom.vanilla.loader.EntityAnvilLoader;
import net.minestom.vanilla.logging.Logger;
import net.minestom.vanilla.loot.LootFeature;
import net.minestom.vanilla.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

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
    private final @NotNull InstanceContainer overworld;

    Survival(@NotNull ServerProcess process) {
        this.process = process;

        this.overworld = process.instance().createInstanceContainer(DimensionType.OVERWORLD);
        this.overworld.setChunkLoader(new EntityAnvilLoader(Path.of("world")));
        this.overworld.setChunkSupplier(LightingChunk::new);
        this.overworld.setWorldBorder(new WorldBorder(
                32 * 16 * 2, 0, 0, 10, 500, 560
        ));
    }

    /**
     * Initializes the server (event handlers, etc.).
     */
    public void initialize() {

        Map<Key, LootTable> tables = LootFeature.buildFromDatapack(process);
        process.eventHandler().addChild(LootFeature.createEventNode(tables));

        Map<Key, Recipe> recipes = CraftingFeature.buildFromDatapack(process);
        process.eventHandler().addChild(CraftingFeature.createEventNode(recipes, process));

        process.eventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();

            // TODO: Determine respawn coordinates and radius, and then randomly pick a valid spot
            Pos respawnPoint = new Pos(0, 64, 0, 0, 0);

            event.setSpawningInstance(this.overworld);
            player.setRespawnPoint(respawnPoint);

            var enchs = new EnchantmentList(Map.of(
                    Enchantment.EFFICIENCY, 5,
                    Enchantment.FORTUNE, 3
            ));

            player.getInventory().addItemStack(ItemStack.of(Material.DIAMOND_PICKAXE).with(DataComponents.ENCHANTMENTS, enchs));
            player.getInventory().addItemStack(ItemStack.of(Material.DIAMOND_HOE).with(DataComponents.ENCHANTMENTS, enchs));

        }).addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();

            if (event.isFirstSpawn()) {
                this.broadcast(Component.translatable("multiplayer.player.joined", NamedTextColor.YELLOW).arguments(player.getName()));
            }
        }).addListener(PlayerDisconnectEvent.class, event -> {
            final Player player = event.getPlayer();

            this.broadcast(Component.translatable("multiplayer.player.left", NamedTextColor.YELLOW).arguments(player.getName()));
        }).addListener(PickupItemEvent.class, event -> {
            if (!(event.getLivingEntity() instanceof Player player)) return;

            // Cancel event if player does not have enough inventory space
            ItemStack itemStack = event.getItemEntity().getItemStack();
            event.setCancelled(!player.getInventory().addItemStack(itemStack));
        }).addListener(ItemDropEvent.class, event -> {
            final Player player = event.getPlayer();
            ItemStack droppedItem = event.getItemStack();

            Pos playerPos = player.getPosition();
            ItemEntity itemEntity = new ItemEntity(droppedItem);
            itemEntity.setPickupDelay(Duration.of(500, TimeUnit.MILLISECOND));
            itemEntity.setInstance(player.getInstance(), playerPos.withY(y -> y + 1.5));
            Vec velocity = playerPos.direction().mul(6);
            itemEntity.setVelocity(velocity);
        });
    }

    private void broadcast(@NotNull Component message) {
        for (Instance instance : process.instance().getInstances()) {
            instance.sendMessage(message);
        }
    }

}
