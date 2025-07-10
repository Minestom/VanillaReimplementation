package net.minestom.vanilla.blocks.behavior;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class StrippingBehaviorRule implements BlockHandler {

    public StrippingBehaviorRule(Block block) {
        // Block parameter kept for consistency with other handlers
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("vri:stripping_behavior");
    }

    @Override
    public boolean onInteract(Interaction interaction) {
        Player player = interaction.getPlayer();
        ItemStack itemInHand = player.getItemInMainHand();

        if (!isAxe(itemInHand.material())) {
            return true;
        }

        Block currentBlock = interaction.getInstance().getBlock(interaction.getBlockPosition());
        Block strippedBaseBlock = getStrippedVariant(currentBlock);
        if (strippedBaseBlock == null) {
            return false;
        }

        Block strippedBlockWithProperties = preserveBlockProperties(currentBlock, strippedBaseBlock);
        interaction.getInstance().setBlock(interaction.getBlockPosition(), strippedBlockWithProperties);

        if (player.getGameMode() != GameMode.CREATIVE) {
            damageAxe(player, itemInHand);
        }

        return false;
    }

    private Block preserveBlockProperties(Block originalBlock, Block strippedBlock) {
        Block resultBlock = strippedBlock;
        String axis = originalBlock.getProperty("axis");
        if (axis != null) {
            resultBlock = resultBlock.withProperty("axis", axis);
        }
        return resultBlock;
    }

    private boolean isAxe(Material material) {
        return material.name().endsWith("axe");
    }

    private Block getStrippedVariant(Block originalBlock) {
        String baseBlockName = originalBlock.name();
      return switch (baseBlockName) {
        case "minecraft:oak_log" -> Block.STRIPPED_OAK_LOG;
        case "minecraft:spruce_log" -> Block.STRIPPED_SPRUCE_LOG;
        case "minecraft:birch_log" -> Block.STRIPPED_BIRCH_LOG;
        case "minecraft:jungle_log" -> Block.STRIPPED_JUNGLE_LOG;
        case "minecraft:acacia_log" -> Block.STRIPPED_ACACIA_LOG;
        case "minecraft:dark_oak_log" -> Block.STRIPPED_DARK_OAK_LOG;
        case "minecraft:mangrove_log" -> Block.STRIPPED_MANGROVE_LOG;
        case "minecraft:cherry_log" -> Block.STRIPPED_CHERRY_LOG;
        case "minecraft:bamboo_block" -> Block.STRIPPED_BAMBOO_BLOCK;
        case "minecraft:oak_wood" -> Block.STRIPPED_OAK_WOOD;
        case "minecraft:spruce_wood" -> Block.STRIPPED_SPRUCE_WOOD;
        case "minecraft:birch_wood" -> Block.STRIPPED_BIRCH_WOOD;
        case "minecraft:jungle_wood" -> Block.STRIPPED_JUNGLE_WOOD;
        case "minecraft:acacia_wood" -> Block.STRIPPED_ACACIA_WOOD;
        case "minecraft:dark_oak_wood" -> Block.STRIPPED_DARK_OAK_WOOD;
        case "minecraft:mangrove_wood" -> Block.STRIPPED_MANGROVE_WOOD;
        case "minecraft:cherry_wood" -> Block.STRIPPED_CHERRY_WOOD;
        case "minecraft:crimson_stem" -> Block.STRIPPED_CRIMSON_STEM;
        case "minecraft:warped_stem" -> Block.STRIPPED_WARPED_STEM;
        case "minecraft:crimson_hyphae" -> Block.STRIPPED_CRIMSON_HYPHAE;
        case "minecraft:warped_hyphae" -> Block.STRIPPED_WARPED_HYPHAE;
        default -> null;
      };
    }

    private void damageAxe(Player player, ItemStack axe) {
        Integer currentDamage = axe.getTag(Tag.Integer("Damage"));
        if (currentDamage == null) {
            currentDamage = 0;
        }

        if (axe.has(DataComponents.MAX_DAMAGE)) {
            Integer maxDamage = axe.get(DataComponents.MAX_DAMAGE);
            if (maxDamage != null) {
                if (currentDamage + 1 >= maxDamage) {
                    player.setItemInMainHand(ItemStack.AIR);
                    Sound breakSound = Sound.sound(SoundEvent.ENTITY_ITEM_BREAK, Sound.Source.PLAYER, 1.0f, 1.0f);
                    if (player.getInstance() != null) {
                        player.getInstance().playSound(breakSound, player.getPosition());
                    }
                } else {
                    ItemStack damagedAxe = axe.withTag(Tag.Integer("Damage"), currentDamage + 1);
                    player.setItemInMainHand(damagedAxe);
                }
            }
        }
    }

    public static List<Block> getStrippableBlocks() {
        return Arrays.asList(
            Block.OAK_LOG,
            Block.SPRUCE_LOG,
            Block.BIRCH_LOG,
            Block.JUNGLE_LOG,
            Block.ACACIA_LOG,
            Block.DARK_OAK_LOG,
            Block.MANGROVE_LOG,
            Block.CHERRY_LOG,
            Block.BAMBOO_BLOCK,

            Block.OAK_WOOD,
            Block.SPRUCE_WOOD,
            Block.BIRCH_WOOD,
            Block.JUNGLE_WOOD,
            Block.ACACIA_WOOD,
            Block.DARK_OAK_WOOD,
            Block.MANGROVE_WOOD,
            Block.CHERRY_WOOD,

            Block.CRIMSON_STEM,
            Block.WARPED_STEM,
            Block.CRIMSON_HYPHAE,
            Block.WARPED_HYPHAE
        );
    }
}
