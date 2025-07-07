package net.minestom.vanilla.loot;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * Utilities for calculating how much experience ought to be dropped when a block is broken.
 */
public class BlockExperience {

    /**
     * Gets the amount of experience the given block should drop when mined.
     */
    public static int getExperience(@NotNull Block minedBlock, @NotNull ItemStack tool, @NotNull RandomGenerator random) {
        // TODO: Will have to take into account EffectComponent#BLOCK_EXPERIENCE on Enchantments if it is ever used.

        if (tool.get(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY).has(Enchantment.SILK_TOUCH)) return 0;

        return switch (EXPERIENCE.get(minedBlock.id())) {
            case Amount.Constant(int points) -> points;
            case Amount.Uniform(int min, int max) -> random.nextInt(min, max + 1);
            case null -> 0;
        };
    }

    /**
     * An amount of experience - either a constant or uniform.
     */
    public sealed interface Amount {

        record Constant(int amount) implements Amount {}
        record Uniform(int min, int max) implements Amount {}

    }

    /**
     * Unfortunately, this cannot be datagenned. This is not a consistent property in the code, and cannot be reliably
     * checked. Naive checks will detect most of these blocks (but not all), and will label some other blocks as
     * constant 0. Implementing this correctly requires code introspection abilities that are not reasonable to
     * implement, especially given the small number (16) of blocks that actually drop experience.
     */
    private static final @NotNull Int2ObjectMap<Amount> EXPERIENCE = new Int2ObjectArrayMap<>(Map.ofEntries(
            // Ores
            entry(Block.COAL_ORE, new Amount.Uniform(0, 2)),
            entry(Block.DEEPSLATE_COAL_ORE, new Amount.Uniform(0, 2)),
            entry(Block.LAPIS_ORE, new Amount.Uniform(2, 5)),
            entry(Block.DEEPSLATE_LAPIS_ORE, new Amount.Uniform(2, 5)),
            entry(Block.REDSTONE_ORE, new Amount.Uniform(1, 5)),
            entry(Block.DEEPSLATE_REDSTONE_ORE, new Amount.Uniform(1, 5)),
            entry(Block.DIAMOND_ORE, new Amount.Uniform(3, 7)),
            entry(Block.DEEPSLATE_DIAMOND_ORE, new Amount.Uniform(3, 7)),
            entry(Block.EMERALD_ORE, new Amount.Uniform(3, 7)),
            entry(Block.DEEPSLATE_EMERALD_ORE, new Amount.Uniform(3, 7)),
            entry(Block.NETHER_GOLD_ORE, new Amount.Uniform(0, 1)),
            entry(Block.NETHER_QUARTZ_ORE, new Amount.Uniform(2, 5)),

            // Sculk
            entry(Block.SCULK, new Amount.Constant(1)),
            entry(Block.SCULK_SHRIEKER, new Amount.Constant(5)),
            entry(Block.SCULK_SENSOR, new Amount.Constant(5)),
            entry(Block.SCULK_CATALYST, new Amount.Constant(5))
    ));

    private static Map.Entry<Integer, Amount> entry(Block block, Amount amount) {
        return Map.entry(block.id(), amount);
    }

}
