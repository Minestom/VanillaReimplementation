package net.minestom.vanilla.loot;

import net.kyori.adventure.key.Key;
import net.minestom.server.ServerProcess;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.Tool;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.vanilla.datapack.Datapacks;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
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

    @SuppressWarnings("PatternValidation")
    public static @NotNull EventNode<InstanceEvent> createEventNode(@NotNull Map<Key, LootTable> tables) {
        return EventNode.type("loot-tables", EventFilter.INSTANCE).addListener(PlayerBlockBreakEvent.class, event -> {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return; // No loot in creative mode

            final Block block = event.getBlock();

            ItemStack heldItem = event.getPlayer().getItemInMainHand();
            Tool tool = heldItem.get(DataComponents.TOOL);

            // If the block doesn't require a tool, OR there is a tool and the block is explicitly allowed
            boolean canDrop = !block.registry().requiresTool() || (tool != null && tool.isCorrectForDrops(block));

            if (!canDrop) return;

            // TODO: Can be pre-converted to `LootTable[]` that turns block IDs into loot tables.
            Key key = Key.key("blocks/" + block.key().value());
            LootTable table = tables.get(key);

            if (table == null) {
                Logger.warn("Block " + block.key() + " does not have a corresponding loot table (would be at: " + key.asString() + ")");
                return;
            }

            // Build a context and drop
            LootContext context = LootContext.from(Map.of(
                    LootContext.RANDOM, new Random(), // TODO: Replace with sequence random
                    LootContext.WORLD, event.getInstance(),
                    LootContext.BLOCK_STATE, block,
                    LootContext.ORIGIN, event.getBlockPosition(),
                    LootContext.TOOL, heldItem,
                    LootContext.THIS_ENTITY, event.getPlayer()
            ));

            for (ItemStack drop : table.generate(context)) {
                blockDrop(event.getInstance(), drop, event.getBlockPosition());
            }

            int experience = BlockExperience.getExperience(block, heldItem, ThreadLocalRandom.current());
            if (experience != 0) {
                ExperienceOrb orb = new ExperienceOrb((short) experience);
                orb.setInstance(event.getInstance(), event.getBlockPosition().add(0.5, 0.5, 0.5));
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
