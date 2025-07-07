package net.minestom.vanilla.loot;

import net.kyori.adventure.key.Key;
import net.minestom.server.ServerProcess;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.vanilla.datapack.Datapacks;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Loot {

    public static @NotNull Map<Key, LootTable> loadTables(@NotNull ServerProcess process) {
        try {
            Path jar = Datapacks.ensureCurrentJarExists();

            Path tablesPath = Path.of("/", "data", "minecraft", "loot_table");

            return Datapacks.buildRegistryFromJar(jar, tablesPath, process, ".json", LootTable.CODEC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull EventNode<InstanceEvent> createEventNode(@NotNull Map<Key, LootTable> tables) {
        return EventNode.type("loot-tables", EventFilter.INSTANCE).addListener(PlayerBlockBreakEvent.class, event -> {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

            final Block block = event.getBlock();

            var material = block.registry().material();
            if (material == null) return;

            var held = event.getPlayer().getItemInMainHand();
            var enchs = held.get(DataComponents.ENCHANTMENTS);
            var tool = held.get(DataComponents.TOOL);

            if (!block.registry().requiresTool() || (tool != null && tool.isCorrectForDrops(block))) {
                Map<LootContext.Key<?>, Object> l = new HashMap<>();
                l.put(LootContext.RANDOM, new Random());

                l.put(LootContext.TOOL, event.getPlayer().getItemInMainHand());
                l.put(LootContext.BLOCK_STATE, event.getBlock());
                l.put(LootContext.ORIGIN, event.getBlockPosition());

                if (enchs.has(Enchantment.FORTUNE)) {
                    l.put(LootContext.ENCHANTMENT_ACTIVE, true);
                    l.put(LootContext.ENCHANTMENT_LEVEL, enchs.level(Enchantment.FORTUNE));
                }

                List<ItemStack> drops = tables.get(Key.key("blocks/" + event.getBlock().key().value())).generate(LootContext.from(l));

                for (var drop : drops) {
                    blockDrop(event.getInstance(), drop, event.getBlockPosition());
                }
            }
        });
    }

    public static void blockDrop(@NotNull Instance instance, @NotNull ItemStack item, @NotNull Point block) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        Pos spawn = new Pos(
                block.blockX() + 0.5 + rng.nextDouble(-0.25, 0.25),
                block.blockY() + 0.5 + rng.nextDouble(-0.25, 0.25) - EntityType.ITEM.height() / 2,
                block.blockZ() + 0.5 + rng.nextDouble(-0.25, 0.25),
                rng.nextFloat(360),
                0
        );

        drop(instance, item, spawn);
    }

    public static void drop(@NotNull Instance instance, @NotNull ItemStack item, @NotNull Point position) {
        ItemEntity entity = new ItemEntity(item);

        ThreadLocalRandom rng = ThreadLocalRandom.current();

        Vec vel = new Vec(
                rng.nextDouble(-0.1, 0.1),
                0.2,
                rng.nextDouble(-0.1, 0.1)
        ).mul(20);

        entity.setPickupDelay(10, TimeUnit.SERVER_TICK);

        entity.setInstance(instance, position);
        entity.setVelocity(vel);
    }

}
