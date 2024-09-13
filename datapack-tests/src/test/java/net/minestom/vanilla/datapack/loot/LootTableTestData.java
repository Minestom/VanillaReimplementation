package net.minestom.vanilla.datapack.loot;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.vanilla.datapack.loot.context.LootContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootTableTestData {

    public static final Map<String, List<ItemStack>> EXPECTED_RESULTS;

    static {
        Map<String, List<ItemStack>> expected = new HashMap<>();

        // TODO: Go through these and make sure they are correct
        expected.put("acacia_button", List.of(ItemStack.of(Material.ACACIA_BUTTON, 1)));
        expected.put("acacia_door", List.of());
        expected.put("acacia_fence", List.of(ItemStack.of(Material.ACACIA_FENCE, 1)));
        expected.put("acacia_fence_gate", List.of(ItemStack.of(Material.ACACIA_FENCE_GATE, 1)));
        expected.put("acacia_hanging_sign", List.of(ItemStack.of(Material.ACACIA_HANGING_SIGN, 1)));
        expected.put("acacia_leaves", List.of());
        expected.put("acacia_log", List.of(ItemStack.of(Material.ACACIA_LOG, 1)));
        expected.put("acacia_planks", List.of(ItemStack.of(Material.ACACIA_PLANKS, 1)));
        expected.put("acacia_pressure_plate", List.of(ItemStack.of(Material.ACACIA_PRESSURE_PLATE, 1)));
        expected.put("acacia_sapling", List.of(ItemStack.of(Material.ACACIA_SAPLING, 1)));
        expected.put("acacia_sign", List.of(ItemStack.of(Material.ACACIA_SIGN, 1)));
        expected.put("acacia_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("acacia_stairs", List.of(ItemStack.of(Material.ACACIA_STAIRS, 1)));
        expected.put("acacia_trapdoor", List.of(ItemStack.of(Material.ACACIA_TRAPDOOR, 1)));
        expected.put("acacia_wood", List.of(ItemStack.of(Material.ACACIA_WOOD, 1)));
        expected.put("activator_rail", List.of(ItemStack.of(Material.ACTIVATOR_RAIL, 1)));
        expected.put("allium", List.of(ItemStack.of(Material.ALLIUM, 1)));
        expected.put("amethyst_block", List.of(ItemStack.of(Material.AMETHYST_BLOCK, 1)));
        expected.put("amethyst_cluster", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("ancient_debris", List.of(ItemStack.of(Material.ANCIENT_DEBRIS, 1)));
        expected.put("andesite", List.of(ItemStack.of(Material.ANDESITE, 1)));
        expected.put("andesite_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("andesite_stairs", List.of(ItemStack.of(Material.ANDESITE_STAIRS, 1)));
        expected.put("andesite_wall", List.of(ItemStack.of(Material.ANDESITE_WALL, 1)));
        expected.put("anvil", List.of(ItemStack.of(Material.ANVIL, 1)));
        expected.put("attached_melon_stem", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("attached_pumpkin_stem", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("azalea", List.of(ItemStack.of(Material.AZALEA, 1)));
        expected.put("azalea_leaves", List.of());
        expected.put("azure_bluet", List.of(ItemStack.of(Material.AZURE_BLUET, 1)));
        expected.put("bamboo", List.of(ItemStack.of(Material.BAMBOO, 1)));
        expected.put("bamboo_block", List.of(ItemStack.of(Material.BAMBOO_BLOCK, 1)));
        expected.put("bamboo_button", List.of(ItemStack.of(Material.BAMBOO_BUTTON, 1)));
        expected.put("bamboo_door", List.of());
        expected.put("bamboo_fence", List.of(ItemStack.of(Material.BAMBOO_FENCE, 1)));
        expected.put("bamboo_fence_gate", List.of(ItemStack.of(Material.BAMBOO_FENCE_GATE, 1)));
        expected.put("bamboo_hanging_sign", List.of(ItemStack.of(Material.BAMBOO_HANGING_SIGN, 1)));
        expected.put("bamboo_mosaic", List.of(ItemStack.of(Material.BAMBOO_MOSAIC, 1)));
        expected.put("bamboo_mosaic_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("bamboo_mosaic_stairs", List.of(ItemStack.of(Material.BAMBOO_MOSAIC_STAIRS, 1)));
        expected.put("bamboo_planks", List.of(ItemStack.of(Material.BAMBOO_PLANKS, 1)));
        expected.put("bamboo_pressure_plate", List.of(ItemStack.of(Material.BAMBOO_PRESSURE_PLATE, 1)));
        expected.put("bamboo_sapling", List.of(ItemStack.of(Material.BAMBOO, 1)));
        expected.put("bamboo_sign", List.of(ItemStack.of(Material.BAMBOO_SIGN, 1)));
        expected.put("bamboo_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("bamboo_stairs", List.of(ItemStack.of(Material.BAMBOO_STAIRS, 1)));
        expected.put("bamboo_trapdoor", List.of(ItemStack.of(Material.BAMBOO_TRAPDOOR, 1)));
        expected.put("barrel", List.of(ItemStack.of(Material.BARREL, 1)));
        expected.put("basalt", List.of(ItemStack.of(Material.BASALT, 1)));
        expected.put("beacon", List.of(ItemStack.of(Material.BEACON, 1)));
        expected.put("bee_nest", List.of());
        expected.put("beehive", List.of(ItemStack.of(Material.BEEHIVE, 1)));
        expected.put("beetroots", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("bell", List.of(ItemStack.of(Material.BELL, 1)));
        expected.put("big_dripleaf", List.of(ItemStack.of(Material.BIG_DRIPLEAF, 1)));
        expected.put("big_dripleaf_stem", List.of(ItemStack.of(Material.BIG_DRIPLEAF, 1)));
        expected.put("birch_button", List.of(ItemStack.of(Material.BIRCH_BUTTON, 1)));
        expected.put("birch_door", List.of());
        expected.put("birch_fence", List.of(ItemStack.of(Material.BIRCH_FENCE, 1)));
        expected.put("birch_fence_gate", List.of(ItemStack.of(Material.BIRCH_FENCE_GATE, 1)));
        expected.put("birch_hanging_sign", List.of(ItemStack.of(Material.BIRCH_HANGING_SIGN, 1)));
        expected.put("birch_leaves", List.of());
        expected.put("birch_log", List.of(ItemStack.of(Material.BIRCH_LOG, 1)));
        expected.put("birch_planks", List.of(ItemStack.of(Material.BIRCH_PLANKS, 1)));
        expected.put("birch_pressure_plate", List.of(ItemStack.of(Material.BIRCH_PRESSURE_PLATE, 1)));
        expected.put("birch_sapling", List.of(ItemStack.of(Material.BIRCH_SAPLING, 1)));
        expected.put("birch_sign", List.of(ItemStack.of(Material.BIRCH_SIGN, 1)));
        expected.put("birch_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("birch_stairs", List.of(ItemStack.of(Material.BIRCH_STAIRS, 1)));
        expected.put("birch_trapdoor", List.of(ItemStack.of(Material.BIRCH_TRAPDOOR, 1)));
        expected.put("birch_wood", List.of(ItemStack.of(Material.BIRCH_WOOD, 1)));
        expected.put("black_banner", List.of(ItemStack.of(Material.BLACK_BANNER, 1)));
        expected.put("black_bed", List.of());
        expected.put("black_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("black_candle_cake", List.of(ItemStack.of(Material.BLACK_CANDLE, 1)));
        expected.put("black_carpet", List.of(ItemStack.of(Material.BLACK_CARPET, 1)));
        expected.put("black_concrete", List.of(ItemStack.of(Material.BLACK_CONCRETE, 1)));
        expected.put("black_concrete_powder", List.of(ItemStack.of(Material.BLACK_CONCRETE_POWDER, 1)));
        expected.put("black_glazed_terracotta", List.of(ItemStack.of(Material.BLACK_GLAZED_TERRACOTTA, 1)));
        expected.put("black_shulker_box", List.of(ItemStack.of(Material.BLACK_SHULKER_BOX, 1)));
        expected.put("black_stained_glass", List.of());
        expected.put("black_stained_glass_pane", List.of());
        expected.put("black_terracotta", List.of(ItemStack.of(Material.BLACK_TERRACOTTA, 1)));
        expected.put("black_wool", List.of(ItemStack.of(Material.BLACK_WOOL, 1)));
        expected.put("blackstone", List.of(ItemStack.of(Material.BLACKSTONE, 1)));
        expected.put("blackstone_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("blackstone_stairs", List.of(ItemStack.of(Material.BLACKSTONE_STAIRS, 1)));
        expected.put("blackstone_wall", List.of(ItemStack.of(Material.BLACKSTONE_WALL, 1)));
        expected.put("blast_furnace", List.of(ItemStack.of(Material.BLAST_FURNACE, 1)));
        expected.put("blue_banner", List.of(ItemStack.of(Material.BLUE_BANNER, 1)));
        expected.put("blue_bed", List.of());
        expected.put("blue_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("blue_candle_cake", List.of(ItemStack.of(Material.BLUE_CANDLE, 1)));
        expected.put("blue_carpet", List.of(ItemStack.of(Material.BLUE_CARPET, 1)));
        expected.put("blue_concrete", List.of(ItemStack.of(Material.BLUE_CONCRETE, 1)));
        expected.put("blue_concrete_powder", List.of(ItemStack.of(Material.BLUE_CONCRETE_POWDER, 1)));
        expected.put("blue_glazed_terracotta", List.of(ItemStack.of(Material.BLUE_GLAZED_TERRACOTTA, 1)));
        expected.put("blue_ice", List.of());
        expected.put("blue_orchid", List.of(ItemStack.of(Material.BLUE_ORCHID, 1)));
        expected.put("blue_shulker_box", List.of(ItemStack.of(Material.BLUE_SHULKER_BOX, 1)));
        expected.put("blue_stained_glass", List.of());
        expected.put("blue_stained_glass_pane", List.of());
        expected.put("blue_terracotta", List.of(ItemStack.of(Material.BLUE_TERRACOTTA, 1)));
        expected.put("blue_wool", List.of(ItemStack.of(Material.BLUE_WOOL, 1)));
        expected.put("bone_block", List.of(ItemStack.of(Material.BONE_BLOCK, 1)));
        expected.put("bookshelf", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("brain_coral", List.of());
        expected.put("brain_coral_block", List.of(ItemStack.of(Material.DEAD_BRAIN_CORAL_BLOCK, 1)));
        expected.put("brain_coral_fan", List.of());
        expected.put("brewing_stand", List.of(ItemStack.of(Material.BREWING_STAND, 1)));
        expected.put("brick_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("brick_stairs", List.of(ItemStack.of(Material.BRICK_STAIRS, 1)));
        expected.put("brick_wall", List.of(ItemStack.of(Material.BRICK_WALL, 1)));
        expected.put("bricks", List.of(ItemStack.of(Material.BRICKS, 1)));
        expected.put("brown_banner", List.of(ItemStack.of(Material.BROWN_BANNER, 1)));
        expected.put("brown_bed", List.of());
        expected.put("brown_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("brown_candle_cake", List.of(ItemStack.of(Material.BROWN_CANDLE, 1)));
        expected.put("brown_carpet", List.of(ItemStack.of(Material.BROWN_CARPET, 1)));
        expected.put("brown_concrete", List.of(ItemStack.of(Material.BROWN_CONCRETE, 1)));
        expected.put("brown_concrete_powder", List.of(ItemStack.of(Material.BROWN_CONCRETE_POWDER, 1)));
        expected.put("brown_glazed_terracotta", List.of(ItemStack.of(Material.BROWN_GLAZED_TERRACOTTA, 1)));
        expected.put("brown_mushroom", List.of(ItemStack.of(Material.BROWN_MUSHROOM, 1)));
        expected.put("brown_mushroom_block", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("brown_shulker_box", List.of(ItemStack.of(Material.BROWN_SHULKER_BOX, 1)));
        expected.put("brown_stained_glass", List.of());
        expected.put("brown_stained_glass_pane", List.of());
        expected.put("brown_terracotta", List.of(ItemStack.of(Material.BROWN_TERRACOTTA, 1)));
        expected.put("brown_wool", List.of(ItemStack.of(Material.BROWN_WOOL, 1)));
        expected.put("bubble_coral", List.of());
        expected.put("bubble_coral_block", List.of(ItemStack.of(Material.DEAD_BUBBLE_CORAL_BLOCK, 1)));
        expected.put("bubble_coral_fan", List.of());
        expected.put("budding_amethyst", List.of());
        expected.put("cactus", List.of(ItemStack.of(Material.CACTUS, 1)));
        expected.put("cake", List.of());
        expected.put("calcite", List.of(ItemStack.of(Material.CALCITE, 1)));
        expected.put("calibrated_sculk_sensor", List.of());
        expected.put("campfire", List.of(ItemStack.of(Material.CHARCOAL, 2)));
        expected.put("candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("candle_cake", List.of(ItemStack.of(Material.CANDLE, 1)));
        expected.put("carrots", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("cartography_table", List.of(ItemStack.of(Material.CARTOGRAPHY_TABLE, 1)));
        expected.put("carved_pumpkin", List.of(ItemStack.of(Material.CARVED_PUMPKIN, 1)));
        expected.put("cauldron", List.of(ItemStack.of(Material.CAULDRON, 1)));
        expected.put("cave_vines", List.of());
        expected.put("cave_vines_plant", List.of());
        expected.put("chain", List.of(ItemStack.of(Material.CHAIN, 1)));
        expected.put("cherry_button", List.of(ItemStack.of(Material.CHERRY_BUTTON, 1)));
        expected.put("cherry_door", List.of());
        expected.put("cherry_fence", List.of(ItemStack.of(Material.CHERRY_FENCE, 1)));
        expected.put("cherry_fence_gate", List.of(ItemStack.of(Material.CHERRY_FENCE_GATE, 1)));
        expected.put("cherry_hanging_sign", List.of(ItemStack.of(Material.CHERRY_HANGING_SIGN, 1)));
        expected.put("cherry_leaves", List.of());
        expected.put("cherry_log", List.of(ItemStack.of(Material.CHERRY_LOG, 1)));
        expected.put("cherry_planks", List.of(ItemStack.of(Material.CHERRY_PLANKS, 1)));
        expected.put("cherry_pressure_plate", List.of(ItemStack.of(Material.CHERRY_PRESSURE_PLATE, 1)));
        expected.put("cherry_sapling", List.of(ItemStack.of(Material.CHERRY_SAPLING, 1)));
        expected.put("cherry_sign", List.of(ItemStack.of(Material.CHERRY_SIGN, 1)));
        expected.put("cherry_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("cherry_stairs", List.of(ItemStack.of(Material.CHERRY_STAIRS, 1)));
        expected.put("cherry_trapdoor", List.of(ItemStack.of(Material.CHERRY_TRAPDOOR, 1)));
        expected.put("cherry_wood", List.of(ItemStack.of(Material.CHERRY_WOOD, 1)));
        expected.put("chest", List.of(ItemStack.of(Material.CHEST, 1)));
        expected.put("chipped_anvil", List.of(ItemStack.of(Material.CHIPPED_ANVIL, 1)));
        expected.put("chiseled_bookshelf", List.of());
        expected.put("chiseled_copper", List.of());
        expected.put("chiseled_deepslate", List.of(ItemStack.of(Material.CHISELED_DEEPSLATE, 1)));
        expected.put("chiseled_nether_bricks", List.of(ItemStack.of(Material.CHISELED_NETHER_BRICKS, 1)));
        expected.put("chiseled_polished_blackstone", List.of(ItemStack.of(Material.CHISELED_POLISHED_BLACKSTONE, 1)));
        expected.put("chiseled_quartz_block", List.of(ItemStack.of(Material.CHISELED_QUARTZ_BLOCK, 1)));
        expected.put("chiseled_red_sandstone", List.of(ItemStack.of(Material.CHISELED_RED_SANDSTONE, 1)));
        expected.put("chiseled_sandstone", List.of(ItemStack.of(Material.CHISELED_SANDSTONE, 1)));
        expected.put("chiseled_stone_bricks", List.of(ItemStack.of(Material.CHISELED_STONE_BRICKS, 1)));
        expected.put("chiseled_tuff", List.of());
        expected.put("chiseled_tuff_bricks", List.of());
        expected.put("chorus_flower", List.of());
        expected.put("chorus_plant", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("clay", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("coal_block", List.of(ItemStack.of(Material.COAL_BLOCK, 1)));
        expected.put("coal_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("coarse_dirt", List.of(ItemStack.of(Material.COARSE_DIRT, 1)));
        expected.put("cobbled_deepslate", List.of(ItemStack.of(Material.COBBLED_DEEPSLATE, 1)));
        expected.put("cobbled_deepslate_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("cobbled_deepslate_stairs", List.of(ItemStack.of(Material.COBBLED_DEEPSLATE_STAIRS, 1)));
        expected.put("cobbled_deepslate_wall", List.of(ItemStack.of(Material.COBBLED_DEEPSLATE_WALL, 1)));
        expected.put("cobblestone", List.of(ItemStack.of(Material.COBBLESTONE, 1)));
        expected.put("cobblestone_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("cobblestone_stairs", List.of(ItemStack.of(Material.COBBLESTONE_STAIRS, 1)));
        expected.put("cobblestone_wall", List.of(ItemStack.of(Material.COBBLESTONE_WALL, 1)));
        expected.put("cobweb", List.of(ItemStack.of(Material.STRING, 1)));
        expected.put("cocoa", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("comparator", List.of(ItemStack.of(Material.COMPARATOR, 1)));
        expected.put("composter", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("conduit", List.of(ItemStack.of(Material.CONDUIT, 1)));
        expected.put("copper_block", List.of(ItemStack.of(Material.COPPER_BLOCK, 1)));
        expected.put("copper_bulb", List.of());
        expected.put("copper_door", List.of());
        expected.put("copper_grate", List.of());
        expected.put("copper_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("copper_trapdoor", List.of());
        expected.put("cornflower", List.of(ItemStack.of(Material.CORNFLOWER, 1)));
        expected.put("cracked_deepslate_bricks", List.of(ItemStack.of(Material.CRACKED_DEEPSLATE_BRICKS, 1)));
        expected.put("cracked_deepslate_tiles", List.of(ItemStack.of(Material.CRACKED_DEEPSLATE_TILES, 1)));
        expected.put("cracked_nether_bricks", List.of(ItemStack.of(Material.CRACKED_NETHER_BRICKS, 1)));
        expected.put("cracked_polished_blackstone_bricks", List.of(ItemStack.of(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS, 1)));
        expected.put("cracked_stone_bricks", List.of(ItemStack.of(Material.CRACKED_STONE_BRICKS, 1)));
        expected.put("crafter", List.of());
        expected.put("crafting_table", List.of(ItemStack.of(Material.CRAFTING_TABLE, 1)));
        expected.put("creeper_head", List.of(ItemStack.of(Material.CREEPER_HEAD, 1)));
        expected.put("crimson_button", List.of(ItemStack.of(Material.CRIMSON_BUTTON, 1)));
        expected.put("crimson_door", List.of());
        expected.put("crimson_fence", List.of(ItemStack.of(Material.CRIMSON_FENCE, 1)));
        expected.put("crimson_fence_gate", List.of(ItemStack.of(Material.CRIMSON_FENCE_GATE, 1)));
        expected.put("crimson_fungus", List.of(ItemStack.of(Material.CRIMSON_FUNGUS, 1)));
        expected.put("crimson_hanging_sign", List.of(ItemStack.of(Material.CRIMSON_HANGING_SIGN, 1)));
        expected.put("crimson_hyphae", List.of(ItemStack.of(Material.CRIMSON_HYPHAE, 1)));
        expected.put("crimson_nylium", List.of(ItemStack.of(Material.NETHERRACK, 1)));
        expected.put("crimson_planks", List.of(ItemStack.of(Material.CRIMSON_PLANKS, 1)));
        expected.put("crimson_pressure_plate", List.of(ItemStack.of(Material.CRIMSON_PRESSURE_PLATE, 1)));
        expected.put("crimson_roots", List.of(ItemStack.of(Material.CRIMSON_ROOTS, 1)));
        expected.put("crimson_sign", List.of(ItemStack.of(Material.CRIMSON_SIGN, 1)));
        expected.put("crimson_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("crimson_stairs", List.of(ItemStack.of(Material.CRIMSON_STAIRS, 1)));
        expected.put("crimson_stem", List.of(ItemStack.of(Material.CRIMSON_STEM, 1)));
        expected.put("crimson_trapdoor", List.of(ItemStack.of(Material.CRIMSON_TRAPDOOR, 1)));
        expected.put("crying_obsidian", List.of(ItemStack.of(Material.CRYING_OBSIDIAN, 1)));
        expected.put("cut_copper", List.of(ItemStack.of(Material.CUT_COPPER, 1)));
        expected.put("cut_copper_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("cut_copper_stairs", List.of(ItemStack.of(Material.CUT_COPPER_STAIRS, 1)));
        expected.put("cut_red_sandstone", List.of(ItemStack.of(Material.CUT_RED_SANDSTONE, 1)));
        expected.put("cut_red_sandstone_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("cut_sandstone", List.of(ItemStack.of(Material.CUT_SANDSTONE, 1)));
        expected.put("cut_sandstone_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("cyan_banner", List.of(ItemStack.of(Material.CYAN_BANNER, 1)));
        expected.put("cyan_bed", List.of());
        expected.put("cyan_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("cyan_candle_cake", List.of(ItemStack.of(Material.CYAN_CANDLE, 1)));
        expected.put("cyan_carpet", List.of(ItemStack.of(Material.CYAN_CARPET, 1)));
        expected.put("cyan_concrete", List.of(ItemStack.of(Material.CYAN_CONCRETE, 1)));
        expected.put("cyan_concrete_powder", List.of(ItemStack.of(Material.CYAN_CONCRETE_POWDER, 1)));
        expected.put("cyan_glazed_terracotta", List.of(ItemStack.of(Material.CYAN_GLAZED_TERRACOTTA, 1)));
        expected.put("cyan_shulker_box", List.of(ItemStack.of(Material.CYAN_SHULKER_BOX, 1)));
        expected.put("cyan_stained_glass", List.of());
        expected.put("cyan_stained_glass_pane", List.of());
        expected.put("cyan_terracotta", List.of(ItemStack.of(Material.CYAN_TERRACOTTA, 1)));
        expected.put("cyan_wool", List.of(ItemStack.of(Material.CYAN_WOOL, 1)));
        expected.put("damaged_anvil", List.of(ItemStack.of(Material.DAMAGED_ANVIL, 1)));
        expected.put("dandelion", List.of(ItemStack.of(Material.DANDELION, 1)));
        expected.put("dark_oak_button", List.of(ItemStack.of(Material.DARK_OAK_BUTTON, 1)));
        expected.put("dark_oak_door", List.of());
        expected.put("dark_oak_fence", List.of(ItemStack.of(Material.DARK_OAK_FENCE, 1)));
        expected.put("dark_oak_fence_gate", List.of(ItemStack.of(Material.DARK_OAK_FENCE_GATE, 1)));
        expected.put("dark_oak_hanging_sign", List.of(ItemStack.of(Material.DARK_OAK_HANGING_SIGN, 1)));
        expected.put("dark_oak_leaves", List.of());
        expected.put("dark_oak_log", List.of(ItemStack.of(Material.DARK_OAK_LOG, 1)));
        expected.put("dark_oak_planks", List.of(ItemStack.of(Material.DARK_OAK_PLANKS, 1)));
        expected.put("dark_oak_pressure_plate", List.of(ItemStack.of(Material.DARK_OAK_PRESSURE_PLATE, 1)));
        expected.put("dark_oak_sapling", List.of(ItemStack.of(Material.DARK_OAK_SAPLING, 1)));
        expected.put("dark_oak_sign", List.of(ItemStack.of(Material.DARK_OAK_SIGN, 1)));
        expected.put("dark_oak_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("dark_oak_stairs", List.of(ItemStack.of(Material.DARK_OAK_STAIRS, 1)));
        expected.put("dark_oak_trapdoor", List.of(ItemStack.of(Material.DARK_OAK_TRAPDOOR, 1)));
        expected.put("dark_oak_wood", List.of(ItemStack.of(Material.DARK_OAK_WOOD, 1)));
        expected.put("dark_prismarine", List.of(ItemStack.of(Material.DARK_PRISMARINE, 1)));
        expected.put("dark_prismarine_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("dark_prismarine_stairs", List.of(ItemStack.of(Material.DARK_PRISMARINE_STAIRS, 1)));
        expected.put("daylight_detector", List.of(ItemStack.of(Material.DAYLIGHT_DETECTOR, 1)));
        expected.put("dead_brain_coral", List.of());
        expected.put("dead_brain_coral_block", List.of(ItemStack.of(Material.DEAD_BRAIN_CORAL_BLOCK, 1)));
        expected.put("dead_brain_coral_fan", List.of());
        expected.put("dead_bubble_coral", List.of());
        expected.put("dead_bubble_coral_block", List.of(ItemStack.of(Material.DEAD_BUBBLE_CORAL_BLOCK, 1)));
        expected.put("dead_bubble_coral_fan", List.of());
        expected.put("dead_bush", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("dead_fire_coral", List.of());
        expected.put("dead_fire_coral_block", List.of(ItemStack.of(Material.DEAD_FIRE_CORAL_BLOCK, 1)));
        expected.put("dead_fire_coral_fan", List.of());
        expected.put("dead_horn_coral", List.of());
        expected.put("dead_horn_coral_block", List.of(ItemStack.of(Material.DEAD_HORN_CORAL_BLOCK, 1)));
        expected.put("dead_horn_coral_fan", List.of());
        expected.put("dead_tube_coral", List.of());
        expected.put("dead_tube_coral_block", List.of(ItemStack.of(Material.DEAD_TUBE_CORAL_BLOCK, 1)));
        expected.put("dead_tube_coral_fan", List.of());
        expected.put("decorated_pot", List.of(ItemStack.of(Material.DECORATED_POT, 1)));
        expected.put("deepslate", List.of(ItemStack.of(Material.COBBLED_DEEPSLATE, 1)));
        expected.put("deepslate_brick_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("deepslate_brick_stairs", List.of(ItemStack.of(Material.DEEPSLATE_BRICK_STAIRS, 1)));
        expected.put("deepslate_brick_wall", List.of(ItemStack.of(Material.DEEPSLATE_BRICK_WALL, 1)));
        expected.put("deepslate_bricks", List.of(ItemStack.of(Material.DEEPSLATE_BRICKS, 1)));
        expected.put("deepslate_coal_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("deepslate_copper_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("deepslate_diamond_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("deepslate_emerald_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("deepslate_gold_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("deepslate_iron_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("deepslate_lapis_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("deepslate_redstone_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("deepslate_tile_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("deepslate_tile_stairs", List.of(ItemStack.of(Material.DEEPSLATE_TILE_STAIRS, 1)));
        expected.put("deepslate_tile_wall", List.of(ItemStack.of(Material.DEEPSLATE_TILE_WALL, 1)));
        expected.put("deepslate_tiles", List.of(ItemStack.of(Material.DEEPSLATE_TILES, 1)));
        expected.put("detector_rail", List.of(ItemStack.of(Material.DETECTOR_RAIL, 1)));
        expected.put("diamond_block", List.of(ItemStack.of(Material.DIAMOND_BLOCK, 1)));
        expected.put("diamond_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("diorite", List.of(ItemStack.of(Material.DIORITE, 1)));
        expected.put("diorite_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("diorite_stairs", List.of(ItemStack.of(Material.DIORITE_STAIRS, 1)));
        expected.put("diorite_wall", List.of(ItemStack.of(Material.DIORITE_WALL, 1)));
        expected.put("dirt", List.of(ItemStack.of(Material.DIRT, 1)));
        expected.put("dirt_path", List.of(ItemStack.of(Material.DIRT, 1)));
        expected.put("dispenser", List.of(ItemStack.of(Material.DISPENSER, 1)));
        expected.put("dragon_egg", List.of(ItemStack.of(Material.DRAGON_EGG, 1)));
        expected.put("dragon_head", List.of(ItemStack.of(Material.DRAGON_HEAD, 1)));
        expected.put("dried_kelp_block", List.of(ItemStack.of(Material.DRIED_KELP_BLOCK, 1)));
        expected.put("dripstone_block", List.of(ItemStack.of(Material.DRIPSTONE_BLOCK, 1)));
        expected.put("dropper", List.of(ItemStack.of(Material.DROPPER, 1)));
        expected.put("emerald_block", List.of(ItemStack.of(Material.EMERALD_BLOCK, 1)));
        expected.put("emerald_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("enchanting_table", List.of(ItemStack.of(Material.ENCHANTING_TABLE, 1)));
        expected.put("end_rod", List.of(ItemStack.of(Material.END_ROD, 1)));
        expected.put("end_stone", List.of(ItemStack.of(Material.END_STONE, 1)));
        expected.put("end_stone_brick_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("end_stone_brick_stairs", List.of(ItemStack.of(Material.END_STONE_BRICK_STAIRS, 1)));
        expected.put("end_stone_brick_wall", List.of(ItemStack.of(Material.END_STONE_BRICK_WALL, 1)));
        expected.put("end_stone_bricks", List.of(ItemStack.of(Material.END_STONE_BRICKS, 1)));
        expected.put("ender_chest", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("exposed_chiseled_copper", List.of());
        expected.put("exposed_copper", List.of(ItemStack.of(Material.EXPOSED_COPPER, 1)));
        expected.put("exposed_copper_bulb", List.of());
        expected.put("exposed_copper_door", List.of());
        expected.put("exposed_copper_grate", List.of());
        expected.put("exposed_copper_trapdoor", List.of());
        expected.put("exposed_cut_copper", List.of(ItemStack.of(Material.EXPOSED_CUT_COPPER, 1)));
        expected.put("exposed_cut_copper_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("exposed_cut_copper_stairs", List.of(ItemStack.of(Material.EXPOSED_CUT_COPPER_STAIRS, 1)));
        expected.put("farmland", List.of(ItemStack.of(Material.DIRT, 1)));
        expected.put("fern", List.of());
        expected.put("fire", List.of());
        expected.put("fire_coral", List.of());
        expected.put("fire_coral_block", List.of(ItemStack.of(Material.DEAD_FIRE_CORAL_BLOCK, 1)));
        expected.put("fire_coral_fan", List.of());
        expected.put("fletching_table", List.of(ItemStack.of(Material.FLETCHING_TABLE, 1)));
        expected.put("flower_pot", List.of(ItemStack.of(Material.FLOWER_POT, 1)));
        expected.put("flowering_azalea", List.of(ItemStack.of(Material.FLOWERING_AZALEA, 1)));
        expected.put("flowering_azalea_leaves", List.of());
        expected.put("frogspawn", List.of());
        expected.put("frosted_ice", List.of());
        expected.put("furnace", List.of(ItemStack.of(Material.FURNACE, 1)));
        expected.put("gilded_blackstone", List.of(ItemStack.of(Material.GILDED_BLACKSTONE, 1)));
        expected.put("glass", List.of());
        expected.put("glass_pane", List.of());
        expected.put("glow_lichen", List.of());
        expected.put("glowstone", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("gold_block", List.of(ItemStack.of(Material.GOLD_BLOCK, 1)));
        expected.put("gold_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("granite", List.of(ItemStack.of(Material.GRANITE, 1)));
        expected.put("granite_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("granite_stairs", List.of(ItemStack.of(Material.GRANITE_STAIRS, 1)));
        expected.put("granite_wall", List.of(ItemStack.of(Material.GRANITE_WALL, 1)));
        expected.put("grass_block", List.of(ItemStack.of(Material.DIRT, 1)));
        expected.put("gravel", List.of(ItemStack.of(Material.GRAVEL, 1)));
        expected.put("gray_banner", List.of(ItemStack.of(Material.GRAY_BANNER, 1)));
        expected.put("gray_bed", List.of());
        expected.put("gray_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("gray_candle_cake", List.of(ItemStack.of(Material.GRAY_CANDLE, 1)));
        expected.put("gray_carpet", List.of(ItemStack.of(Material.GRAY_CARPET, 1)));
        expected.put("gray_concrete", List.of(ItemStack.of(Material.GRAY_CONCRETE, 1)));
        expected.put("gray_concrete_powder", List.of(ItemStack.of(Material.GRAY_CONCRETE_POWDER, 1)));
        expected.put("gray_glazed_terracotta", List.of(ItemStack.of(Material.GRAY_GLAZED_TERRACOTTA, 1)));
        expected.put("gray_shulker_box", List.of(ItemStack.of(Material.GRAY_SHULKER_BOX, 1)));
        expected.put("gray_stained_glass", List.of());
        expected.put("gray_stained_glass_pane", List.of());
        expected.put("gray_terracotta", List.of(ItemStack.of(Material.GRAY_TERRACOTTA, 1)));
        expected.put("gray_wool", List.of(ItemStack.of(Material.GRAY_WOOL, 1)));
        expected.put("green_banner", List.of(ItemStack.of(Material.GREEN_BANNER, 1)));
        expected.put("green_bed", List.of());
        expected.put("green_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("green_candle_cake", List.of(ItemStack.of(Material.GREEN_CANDLE, 1)));
        expected.put("green_carpet", List.of(ItemStack.of(Material.GREEN_CARPET, 1)));
        expected.put("green_concrete", List.of(ItemStack.of(Material.GREEN_CONCRETE, 1)));
        expected.put("green_concrete_powder", List.of(ItemStack.of(Material.GREEN_CONCRETE_POWDER, 1)));
        expected.put("green_glazed_terracotta", List.of(ItemStack.of(Material.GREEN_GLAZED_TERRACOTTA, 1)));
        expected.put("green_shulker_box", List.of(ItemStack.of(Material.GREEN_SHULKER_BOX, 1)));
        expected.put("green_stained_glass", List.of());
        expected.put("green_stained_glass_pane", List.of());
        expected.put("green_terracotta", List.of(ItemStack.of(Material.GREEN_TERRACOTTA, 1)));
        expected.put("green_wool", List.of(ItemStack.of(Material.GREEN_WOOL, 1)));
        expected.put("grindstone", List.of(ItemStack.of(Material.GRINDSTONE, 1)));
        expected.put("hanging_roots", List.of());
        expected.put("hay_block", List.of(ItemStack.of(Material.HAY_BLOCK, 1)));
        expected.put("heavy_weighted_pressure_plate", List.of(ItemStack.of(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, 1)));
        expected.put("honey_block", List.of(ItemStack.of(Material.HONEY_BLOCK, 1)));
        expected.put("honeycomb_block", List.of(ItemStack.of(Material.HONEYCOMB_BLOCK, 1)));
        expected.put("hopper", List.of(ItemStack.of(Material.HOPPER, 1)));
        expected.put("horn_coral", List.of());
        expected.put("horn_coral_block", List.of(ItemStack.of(Material.DEAD_HORN_CORAL_BLOCK, 1)));
        expected.put("horn_coral_fan", List.of());
        expected.put("ice", List.of());
        expected.put("infested_chiseled_stone_bricks", List.of());
        expected.put("infested_cobblestone", List.of());
        expected.put("infested_cracked_stone_bricks", List.of());
        expected.put("infested_deepslate", List.of());
        expected.put("infested_mossy_stone_bricks", List.of());
        expected.put("infested_stone", List.of());
        expected.put("infested_stone_bricks", List.of());
        expected.put("iron_bars", List.of(ItemStack.of(Material.IRON_BARS, 1)));
        expected.put("iron_block", List.of(ItemStack.of(Material.IRON_BLOCK, 1)));
        expected.put("iron_door", List.of());
        expected.put("iron_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("iron_trapdoor", List.of(ItemStack.of(Material.IRON_TRAPDOOR, 1)));
        expected.put("jack_o_lantern", List.of(ItemStack.of(Material.JACK_O_LANTERN, 1)));
        expected.put("jukebox", List.of(ItemStack.of(Material.JUKEBOX, 1)));
        expected.put("jungle_button", List.of(ItemStack.of(Material.JUNGLE_BUTTON, 1)));
        expected.put("jungle_door", List.of());
        expected.put("jungle_fence", List.of(ItemStack.of(Material.JUNGLE_FENCE, 1)));
        expected.put("jungle_fence_gate", List.of(ItemStack.of(Material.JUNGLE_FENCE_GATE, 1)));
        expected.put("jungle_hanging_sign", List.of(ItemStack.of(Material.JUNGLE_HANGING_SIGN, 1)));
        expected.put("jungle_leaves", List.of());
        expected.put("jungle_log", List.of(ItemStack.of(Material.JUNGLE_LOG, 1)));
        expected.put("jungle_planks", List.of(ItemStack.of(Material.JUNGLE_PLANKS, 1)));
        expected.put("jungle_pressure_plate", List.of(ItemStack.of(Material.JUNGLE_PRESSURE_PLATE, 1)));
        expected.put("jungle_sapling", List.of(ItemStack.of(Material.JUNGLE_SAPLING, 1)));
        expected.put("jungle_sign", List.of(ItemStack.of(Material.JUNGLE_SIGN, 1)));
        expected.put("jungle_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("jungle_stairs", List.of(ItemStack.of(Material.JUNGLE_STAIRS, 1)));
        expected.put("jungle_trapdoor", List.of(ItemStack.of(Material.JUNGLE_TRAPDOOR, 1)));
        expected.put("jungle_wood", List.of(ItemStack.of(Material.JUNGLE_WOOD, 1)));
        expected.put("kelp", List.of(ItemStack.of(Material.KELP, 1)));
        expected.put("kelp_plant", List.of(ItemStack.of(Material.KELP, 1)));
        expected.put("ladder", List.of(ItemStack.of(Material.LADDER, 1)));
        expected.put("lantern", List.of(ItemStack.of(Material.LANTERN, 1)));
        expected.put("lapis_block", List.of(ItemStack.of(Material.LAPIS_BLOCK, 1)));
        expected.put("lapis_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("large_amethyst_bud", List.of());
        expected.put("large_fern", List.of());
        expected.put("lava_cauldron", List.of(ItemStack.of(Material.CAULDRON, 1)));
        expected.put("lectern", List.of(ItemStack.of(Material.LECTERN, 1)));
        expected.put("lever", List.of(ItemStack.of(Material.LEVER, 1)));
        expected.put("light_blue_banner", List.of(ItemStack.of(Material.LIGHT_BLUE_BANNER, 1)));
        expected.put("light_blue_bed", List.of());
        expected.put("light_blue_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("light_blue_candle_cake", List.of(ItemStack.of(Material.LIGHT_BLUE_CANDLE, 1)));
        expected.put("light_blue_carpet", List.of(ItemStack.of(Material.LIGHT_BLUE_CARPET, 1)));
        expected.put("light_blue_concrete", List.of(ItemStack.of(Material.LIGHT_BLUE_CONCRETE, 1)));
        expected.put("light_blue_concrete_powder", List.of(ItemStack.of(Material.LIGHT_BLUE_CONCRETE_POWDER, 1)));
        expected.put("light_blue_glazed_terracotta", List.of(ItemStack.of(Material.LIGHT_BLUE_GLAZED_TERRACOTTA, 1)));
        expected.put("light_blue_shulker_box", List.of(ItemStack.of(Material.LIGHT_BLUE_SHULKER_BOX, 1)));
        expected.put("light_blue_stained_glass", List.of());
        expected.put("light_blue_stained_glass_pane", List.of());
        expected.put("light_blue_terracotta", List.of(ItemStack.of(Material.LIGHT_BLUE_TERRACOTTA, 1)));
        expected.put("light_blue_wool", List.of(ItemStack.of(Material.LIGHT_BLUE_WOOL, 1)));
        expected.put("light_gray_banner", List.of(ItemStack.of(Material.LIGHT_GRAY_BANNER, 1)));
        expected.put("light_gray_bed", List.of());
        expected.put("light_gray_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("light_gray_candle_cake", List.of(ItemStack.of(Material.LIGHT_GRAY_CANDLE, 1)));
        expected.put("light_gray_carpet", List.of(ItemStack.of(Material.LIGHT_GRAY_CARPET, 1)));
        expected.put("light_gray_concrete", List.of(ItemStack.of(Material.LIGHT_GRAY_CONCRETE, 1)));
        expected.put("light_gray_concrete_powder", List.of(ItemStack.of(Material.LIGHT_GRAY_CONCRETE_POWDER, 1)));
        expected.put("light_gray_glazed_terracotta", List.of(ItemStack.of(Material.LIGHT_GRAY_GLAZED_TERRACOTTA, 1)));
        expected.put("light_gray_shulker_box", List.of(ItemStack.of(Material.LIGHT_GRAY_SHULKER_BOX, 1)));
        expected.put("light_gray_stained_glass", List.of());
        expected.put("light_gray_stained_glass_pane", List.of());
        expected.put("light_gray_terracotta", List.of(ItemStack.of(Material.LIGHT_GRAY_TERRACOTTA, 1)));
        expected.put("light_gray_wool", List.of(ItemStack.of(Material.LIGHT_GRAY_WOOL, 1)));
        expected.put("light_weighted_pressure_plate", List.of(ItemStack.of(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, 1)));
        expected.put("lightning_rod", List.of(ItemStack.of(Material.LIGHTNING_ROD, 1)));
        expected.put("lilac", List.of());
        expected.put("lily_of_the_valley", List.of(ItemStack.of(Material.LILY_OF_THE_VALLEY, 1)));
        expected.put("lily_pad", List.of(ItemStack.of(Material.LILY_PAD, 1)));
        expected.put("lime_banner", List.of(ItemStack.of(Material.LIME_BANNER, 1)));
        expected.put("lime_bed", List.of());
        expected.put("lime_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("lime_candle_cake", List.of(ItemStack.of(Material.LIME_CANDLE, 1)));
        expected.put("lime_carpet", List.of(ItemStack.of(Material.LIME_CARPET, 1)));
        expected.put("lime_concrete", List.of(ItemStack.of(Material.LIME_CONCRETE, 1)));
        expected.put("lime_concrete_powder", List.of(ItemStack.of(Material.LIME_CONCRETE_POWDER, 1)));
        expected.put("lime_glazed_terracotta", List.of(ItemStack.of(Material.LIME_GLAZED_TERRACOTTA, 1)));
        expected.put("lime_shulker_box", List.of(ItemStack.of(Material.LIME_SHULKER_BOX, 1)));
        expected.put("lime_stained_glass", List.of());
        expected.put("lime_stained_glass_pane", List.of());
        expected.put("lime_terracotta", List.of(ItemStack.of(Material.LIME_TERRACOTTA, 1)));
        expected.put("lime_wool", List.of(ItemStack.of(Material.LIME_WOOL, 1)));
        expected.put("lodestone", List.of(ItemStack.of(Material.LODESTONE, 1)));
        expected.put("loom", List.of(ItemStack.of(Material.LOOM, 1)));
        expected.put("magenta_banner", List.of(ItemStack.of(Material.MAGENTA_BANNER, 1)));
        expected.put("magenta_bed", List.of());
        expected.put("magenta_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("magenta_candle_cake", List.of(ItemStack.of(Material.MAGENTA_CANDLE, 1)));
        expected.put("magenta_carpet", List.of(ItemStack.of(Material.MAGENTA_CARPET, 1)));
        expected.put("magenta_concrete", List.of(ItemStack.of(Material.MAGENTA_CONCRETE, 1)));
        expected.put("magenta_concrete_powder", List.of(ItemStack.of(Material.MAGENTA_CONCRETE_POWDER, 1)));
        expected.put("magenta_glazed_terracotta", List.of(ItemStack.of(Material.MAGENTA_GLAZED_TERRACOTTA, 1)));
        expected.put("magenta_shulker_box", List.of(ItemStack.of(Material.MAGENTA_SHULKER_BOX, 1)));
        expected.put("magenta_stained_glass", List.of());
        expected.put("magenta_stained_glass_pane", List.of());
        expected.put("magenta_terracotta", List.of(ItemStack.of(Material.MAGENTA_TERRACOTTA, 1)));
        expected.put("magenta_wool", List.of(ItemStack.of(Material.MAGENTA_WOOL, 1)));
        expected.put("magma_block", List.of(ItemStack.of(Material.MAGMA_BLOCK, 1)));
        expected.put("mangrove_button", List.of(ItemStack.of(Material.MANGROVE_BUTTON, 1)));
        expected.put("mangrove_door", List.of());
        expected.put("mangrove_fence", List.of(ItemStack.of(Material.MANGROVE_FENCE, 1)));
        expected.put("mangrove_fence_gate", List.of(ItemStack.of(Material.MANGROVE_FENCE_GATE, 1)));
        expected.put("mangrove_hanging_sign", List.of(ItemStack.of(Material.MANGROVE_HANGING_SIGN, 1)));
        expected.put("mangrove_leaves", List.of());
        expected.put("mangrove_log", List.of(ItemStack.of(Material.MANGROVE_LOG, 1)));
        expected.put("mangrove_planks", List.of(ItemStack.of(Material.MANGROVE_PLANKS, 1)));
        expected.put("mangrove_pressure_plate", List.of(ItemStack.of(Material.MANGROVE_PRESSURE_PLATE, 1)));
        expected.put("mangrove_propagule", List.of());
        expected.put("mangrove_roots", List.of(ItemStack.of(Material.MANGROVE_ROOTS, 1)));
        expected.put("mangrove_sign", List.of(ItemStack.of(Material.MANGROVE_SIGN, 1)));
        expected.put("mangrove_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("mangrove_stairs", List.of(ItemStack.of(Material.MANGROVE_STAIRS, 1)));
        expected.put("mangrove_trapdoor", List.of(ItemStack.of(Material.MANGROVE_TRAPDOOR, 1)));
        expected.put("mangrove_wood", List.of(ItemStack.of(Material.MANGROVE_WOOD, 1)));
        expected.put("medium_amethyst_bud", List.of());
        expected.put("melon", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("melon_stem", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("moss_block", List.of(ItemStack.of(Material.MOSS_BLOCK, 1)));
        expected.put("moss_carpet", List.of(ItemStack.of(Material.MOSS_CARPET, 1)));
        expected.put("mossy_cobblestone", List.of(ItemStack.of(Material.MOSSY_COBBLESTONE, 1)));
        expected.put("mossy_cobblestone_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("mossy_cobblestone_stairs", List.of(ItemStack.of(Material.MOSSY_COBBLESTONE_STAIRS, 1)));
        expected.put("mossy_cobblestone_wall", List.of(ItemStack.of(Material.MOSSY_COBBLESTONE_WALL, 1)));
        expected.put("mossy_stone_brick_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("mossy_stone_brick_stairs", List.of(ItemStack.of(Material.MOSSY_STONE_BRICK_STAIRS, 1)));
        expected.put("mossy_stone_brick_wall", List.of(ItemStack.of(Material.MOSSY_STONE_BRICK_WALL, 1)));
        expected.put("mossy_stone_bricks", List.of(ItemStack.of(Material.MOSSY_STONE_BRICKS, 1)));
        expected.put("mud", List.of(ItemStack.of(Material.MUD, 1)));
        expected.put("mud_brick_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("mud_brick_stairs", List.of(ItemStack.of(Material.MUD_BRICK_STAIRS, 1)));
        expected.put("mud_brick_wall", List.of(ItemStack.of(Material.MUD_BRICK_WALL, 1)));
        expected.put("mud_bricks", List.of(ItemStack.of(Material.MUD_BRICKS, 1)));
        expected.put("muddy_mangrove_roots", List.of(ItemStack.of(Material.MUDDY_MANGROVE_ROOTS, 1)));
        expected.put("mushroom_stem", List.of());
        expected.put("mycelium", List.of(ItemStack.of(Material.DIRT, 1)));
        expected.put("nether_brick_fence", List.of(ItemStack.of(Material.NETHER_BRICK_FENCE, 1)));
        expected.put("nether_brick_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("nether_brick_stairs", List.of(ItemStack.of(Material.NETHER_BRICK_STAIRS, 1)));
        expected.put("nether_brick_wall", List.of(ItemStack.of(Material.NETHER_BRICK_WALL, 1)));
        expected.put("nether_bricks", List.of(ItemStack.of(Material.NETHER_BRICKS, 1)));
        expected.put("nether_gold_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("nether_portal", List.of());
        expected.put("nether_quartz_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("nether_sprouts", List.of());
        expected.put("nether_wart", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("nether_wart_block", List.of(ItemStack.of(Material.NETHER_WART_BLOCK, 1)));
        expected.put("netherite_block", List.of(ItemStack.of(Material.NETHERITE_BLOCK, 1)));
        expected.put("netherrack", List.of(ItemStack.of(Material.NETHERRACK, 1)));
        expected.put("note_block", List.of(ItemStack.of(Material.NOTE_BLOCK, 1)));
        expected.put("oak_button", List.of(ItemStack.of(Material.OAK_BUTTON, 1)));
        expected.put("oak_door", List.of());
        expected.put("oak_fence", List.of(ItemStack.of(Material.OAK_FENCE, 1)));
        expected.put("oak_fence_gate", List.of(ItemStack.of(Material.OAK_FENCE_GATE, 1)));
        expected.put("oak_hanging_sign", List.of(ItemStack.of(Material.OAK_HANGING_SIGN, 1)));
        expected.put("oak_leaves", List.of());
        expected.put("oak_log", List.of(ItemStack.of(Material.OAK_LOG, 1)));
        expected.put("oak_planks", List.of(ItemStack.of(Material.OAK_PLANKS, 1)));
        expected.put("oak_pressure_plate", List.of(ItemStack.of(Material.OAK_PRESSURE_PLATE, 1)));
        expected.put("oak_sapling", List.of(ItemStack.of(Material.OAK_SAPLING, 1)));
        expected.put("oak_sign", List.of(ItemStack.of(Material.OAK_SIGN, 1)));
        expected.put("oak_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("oak_stairs", List.of(ItemStack.of(Material.OAK_STAIRS, 1)));
        expected.put("oak_trapdoor", List.of(ItemStack.of(Material.OAK_TRAPDOOR, 1)));
        expected.put("oak_wood", List.of(ItemStack.of(Material.OAK_WOOD, 1)));
        expected.put("observer", List.of(ItemStack.of(Material.OBSERVER, 1)));
        expected.put("obsidian", List.of(ItemStack.of(Material.OBSIDIAN, 1)));
        expected.put("ochre_froglight", List.of(ItemStack.of(Material.OCHRE_FROGLIGHT, 1)));
        expected.put("orange_banner", List.of(ItemStack.of(Material.ORANGE_BANNER, 1)));
        expected.put("orange_bed", List.of());
        expected.put("orange_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("orange_candle_cake", List.of(ItemStack.of(Material.ORANGE_CANDLE, 1)));
        expected.put("orange_carpet", List.of(ItemStack.of(Material.ORANGE_CARPET, 1)));
        expected.put("orange_concrete", List.of(ItemStack.of(Material.ORANGE_CONCRETE, 1)));
        expected.put("orange_concrete_powder", List.of(ItemStack.of(Material.ORANGE_CONCRETE_POWDER, 1)));
        expected.put("orange_glazed_terracotta", List.of(ItemStack.of(Material.ORANGE_GLAZED_TERRACOTTA, 1)));
        expected.put("orange_shulker_box", List.of(ItemStack.of(Material.ORANGE_SHULKER_BOX, 1)));
        expected.put("orange_stained_glass", List.of());
        expected.put("orange_stained_glass_pane", List.of());
        expected.put("orange_terracotta", List.of(ItemStack.of(Material.ORANGE_TERRACOTTA, 1)));
        expected.put("orange_tulip", List.of(ItemStack.of(Material.ORANGE_TULIP, 1)));
        expected.put("orange_wool", List.of(ItemStack.of(Material.ORANGE_WOOL, 1)));
        expected.put("oxeye_daisy", List.of(ItemStack.of(Material.OXEYE_DAISY, 1)));
        expected.put("oxidized_chiseled_copper", List.of());
        expected.put("oxidized_copper", List.of(ItemStack.of(Material.OXIDIZED_COPPER, 1)));
        expected.put("oxidized_copper_bulb", List.of());
        expected.put("oxidized_copper_door", List.of());
        expected.put("oxidized_copper_grate", List.of());
        expected.put("oxidized_copper_trapdoor", List.of());
        expected.put("oxidized_cut_copper", List.of(ItemStack.of(Material.OXIDIZED_CUT_COPPER, 1)));
        expected.put("oxidized_cut_copper_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("oxidized_cut_copper_stairs", List.of(ItemStack.of(Material.OXIDIZED_CUT_COPPER_STAIRS, 1)));
        expected.put("packed_ice", List.of());
        expected.put("packed_mud", List.of(ItemStack.of(Material.PACKED_MUD, 1)));
        expected.put("pearlescent_froglight", List.of(ItemStack.of(Material.PEARLESCENT_FROGLIGHT, 1)));
        expected.put("peony", List.of());
        expected.put("petrified_oak_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("piglin_head", List.of(ItemStack.of(Material.PIGLIN_HEAD, 1)));
        expected.put("pink_banner", List.of(ItemStack.of(Material.PINK_BANNER, 1)));
        expected.put("pink_bed", List.of());
        expected.put("pink_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("pink_candle_cake", List.of(ItemStack.of(Material.PINK_CANDLE, 1)));
        expected.put("pink_carpet", List.of(ItemStack.of(Material.PINK_CARPET, 1)));
        expected.put("pink_concrete", List.of(ItemStack.of(Material.PINK_CONCRETE, 1)));
        expected.put("pink_concrete_powder", List.of(ItemStack.of(Material.PINK_CONCRETE_POWDER, 1)));
        expected.put("pink_glazed_terracotta", List.of(ItemStack.of(Material.PINK_GLAZED_TERRACOTTA, 1)));
        expected.put("pink_petals", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("pink_shulker_box", List.of(ItemStack.of(Material.PINK_SHULKER_BOX, 1)));
        expected.put("pink_stained_glass", List.of());
        expected.put("pink_stained_glass_pane", List.of());
        expected.put("pink_terracotta", List.of(ItemStack.of(Material.PINK_TERRACOTTA, 1)));
        expected.put("pink_tulip", List.of(ItemStack.of(Material.PINK_TULIP, 1)));
        expected.put("pink_wool", List.of(ItemStack.of(Material.PINK_WOOL, 1)));
        expected.put("piston", List.of(ItemStack.of(Material.PISTON, 1)));
        expected.put("pitcher_crop", List.of());
        expected.put("pitcher_plant", List.of());
        expected.put("player_head", List.of(ItemStack.of(Material.PLAYER_HEAD, 1)));
        expected.put("podzol", List.of(ItemStack.of(Material.DIRT, 1)));
        expected.put("pointed_dripstone", List.of(ItemStack.of(Material.POINTED_DRIPSTONE, 1)));
        expected.put("polished_andesite", List.of(ItemStack.of(Material.POLISHED_ANDESITE, 1)));
        expected.put("polished_andesite_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("polished_andesite_stairs", List.of(ItemStack.of(Material.POLISHED_ANDESITE_STAIRS, 1)));
        expected.put("polished_basalt", List.of(ItemStack.of(Material.POLISHED_BASALT, 1)));
        expected.put("polished_blackstone", List.of(ItemStack.of(Material.POLISHED_BLACKSTONE, 1)));
        expected.put("polished_blackstone_brick_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("polished_blackstone_brick_stairs", List.of(ItemStack.of(Material.POLISHED_BLACKSTONE_BRICK_STAIRS, 1)));
        expected.put("polished_blackstone_brick_wall", List.of(ItemStack.of(Material.POLISHED_BLACKSTONE_BRICK_WALL, 1)));
        expected.put("polished_blackstone_bricks", List.of(ItemStack.of(Material.POLISHED_BLACKSTONE_BRICKS, 1)));
        expected.put("polished_blackstone_button", List.of(ItemStack.of(Material.POLISHED_BLACKSTONE_BUTTON, 1)));
        expected.put("polished_blackstone_pressure_plate", List.of(ItemStack.of(Material.POLISHED_BLACKSTONE_PRESSURE_PLATE, 1)));
        expected.put("polished_blackstone_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("polished_blackstone_stairs", List.of(ItemStack.of(Material.POLISHED_BLACKSTONE_STAIRS, 1)));
        expected.put("polished_blackstone_wall", List.of(ItemStack.of(Material.POLISHED_BLACKSTONE_WALL, 1)));
        expected.put("polished_deepslate", List.of(ItemStack.of(Material.POLISHED_DEEPSLATE, 1)));
        expected.put("polished_deepslate_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("polished_deepslate_stairs", List.of(ItemStack.of(Material.POLISHED_DEEPSLATE_STAIRS, 1)));
        expected.put("polished_deepslate_wall", List.of(ItemStack.of(Material.POLISHED_DEEPSLATE_WALL, 1)));
        expected.put("polished_diorite", List.of(ItemStack.of(Material.POLISHED_DIORITE, 1)));
        expected.put("polished_diorite_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("polished_diorite_stairs", List.of(ItemStack.of(Material.POLISHED_DIORITE_STAIRS, 1)));
        expected.put("polished_granite", List.of(ItemStack.of(Material.POLISHED_GRANITE, 1)));
        expected.put("polished_granite_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("polished_granite_stairs", List.of(ItemStack.of(Material.POLISHED_GRANITE_STAIRS, 1)));
        expected.put("polished_tuff", List.of());
        expected.put("polished_tuff_slab", List.of());
        expected.put("polished_tuff_stairs", List.of());
        expected.put("polished_tuff_wall", List.of());
        expected.put("poppy", List.of(ItemStack.of(Material.POPPY, 1)));
        expected.put("potatoes", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("potted_acacia_sapling", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.ACACIA_SAPLING, 1)));
        expected.put("potted_allium", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.ALLIUM, 1)));
        expected.put("potted_azalea_bush", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.AZALEA, 1)));
        expected.put("potted_azure_bluet", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.AZURE_BLUET, 1)));
        expected.put("potted_bamboo", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.BAMBOO, 1)));
        expected.put("potted_birch_sapling", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.BIRCH_SAPLING, 1)));
        expected.put("potted_blue_orchid", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.BLUE_ORCHID, 1)));
        expected.put("potted_brown_mushroom", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.BROWN_MUSHROOM, 1)));
        expected.put("potted_cactus", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.CACTUS, 1)));
        expected.put("potted_cherry_sapling", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.CHERRY_SAPLING, 1)));
        expected.put("potted_cornflower", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.CORNFLOWER, 1)));
        expected.put("potted_crimson_fungus", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.CRIMSON_FUNGUS, 1)));
        expected.put("potted_crimson_roots", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.CRIMSON_ROOTS, 1)));
        expected.put("potted_dandelion", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.DANDELION, 1)));
        expected.put("potted_dark_oak_sapling", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.DARK_OAK_SAPLING, 1)));
        expected.put("potted_dead_bush", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.DEAD_BUSH, 1)));
        expected.put("potted_fern", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.FERN, 1)));
        expected.put("potted_flowering_azalea_bush", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.FLOWERING_AZALEA, 1)));
        expected.put("potted_jungle_sapling", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.JUNGLE_SAPLING, 1)));
        expected.put("potted_lily_of_the_valley", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.LILY_OF_THE_VALLEY, 1)));
        expected.put("potted_mangrove_propagule", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.MANGROVE_PROPAGULE, 1)));
        expected.put("potted_oak_sapling", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.OAK_SAPLING, 1)));
        expected.put("potted_orange_tulip", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.ORANGE_TULIP, 1)));
        expected.put("potted_oxeye_daisy", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.OXEYE_DAISY, 1)));
        expected.put("potted_pink_tulip", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.PINK_TULIP, 1)));
        expected.put("potted_poppy", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.POPPY, 1)));
        expected.put("potted_red_mushroom", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.RED_MUSHROOM, 1)));
        expected.put("potted_red_tulip", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.RED_TULIP, 1)));
        expected.put("potted_spruce_sapling", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.SPRUCE_SAPLING, 1)));
        expected.put("potted_torchflower", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.TORCHFLOWER, 1)));
        expected.put("potted_warped_fungus", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.WARPED_FUNGUS, 1)));
        expected.put("potted_warped_roots", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.WARPED_ROOTS, 1)));
        expected.put("potted_white_tulip", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.WHITE_TULIP, 1)));
        expected.put("potted_wither_rose", List.of(ItemStack.of(Material.FLOWER_POT, 1), ItemStack.of(Material.WITHER_ROSE, 1)));
        expected.put("powder_snow", List.of());
        expected.put("powder_snow_cauldron", List.of(ItemStack.of(Material.CAULDRON, 1)));
        expected.put("powered_rail", List.of(ItemStack.of(Material.POWERED_RAIL, 1)));
        expected.put("prismarine", List.of(ItemStack.of(Material.PRISMARINE, 1)));
        expected.put("prismarine_brick_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("prismarine_brick_stairs", List.of(ItemStack.of(Material.PRISMARINE_BRICK_STAIRS, 1)));
        expected.put("prismarine_bricks", List.of(ItemStack.of(Material.PRISMARINE_BRICKS, 1)));
        expected.put("prismarine_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("prismarine_stairs", List.of(ItemStack.of(Material.PRISMARINE_STAIRS, 1)));
        expected.put("prismarine_wall", List.of(ItemStack.of(Material.PRISMARINE_WALL, 1)));
        expected.put("pumpkin", List.of(ItemStack.of(Material.PUMPKIN, 1)));
        expected.put("pumpkin_stem", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("purple_banner", List.of(ItemStack.of(Material.PURPLE_BANNER, 1)));
        expected.put("purple_bed", List.of());
        expected.put("purple_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("purple_candle_cake", List.of(ItemStack.of(Material.PURPLE_CANDLE, 1)));
        expected.put("purple_carpet", List.of(ItemStack.of(Material.PURPLE_CARPET, 1)));
        expected.put("purple_concrete", List.of(ItemStack.of(Material.PURPLE_CONCRETE, 1)));
        expected.put("purple_concrete_powder", List.of(ItemStack.of(Material.PURPLE_CONCRETE_POWDER, 1)));
        expected.put("purple_glazed_terracotta", List.of(ItemStack.of(Material.PURPLE_GLAZED_TERRACOTTA, 1)));
        expected.put("purple_shulker_box", List.of(ItemStack.of(Material.PURPLE_SHULKER_BOX, 1)));
        expected.put("purple_stained_glass", List.of());
        expected.put("purple_stained_glass_pane", List.of());
        expected.put("purple_terracotta", List.of(ItemStack.of(Material.PURPLE_TERRACOTTA, 1)));
        expected.put("purple_wool", List.of(ItemStack.of(Material.PURPLE_WOOL, 1)));
        expected.put("purpur_block", List.of(ItemStack.of(Material.PURPUR_BLOCK, 1)));
        expected.put("purpur_pillar", List.of(ItemStack.of(Material.PURPUR_PILLAR, 1)));
        expected.put("purpur_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("purpur_stairs", List.of(ItemStack.of(Material.PURPUR_STAIRS, 1)));
        expected.put("quartz_block", List.of(ItemStack.of(Material.QUARTZ_BLOCK, 1)));
        expected.put("quartz_bricks", List.of(ItemStack.of(Material.QUARTZ_BRICKS, 1)));
        expected.put("quartz_pillar", List.of(ItemStack.of(Material.QUARTZ_PILLAR, 1)));
        expected.put("quartz_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("quartz_stairs", List.of(ItemStack.of(Material.QUARTZ_STAIRS, 1)));
        expected.put("rail", List.of(ItemStack.of(Material.RAIL, 1)));
        expected.put("raw_copper_block", List.of(ItemStack.of(Material.RAW_COPPER_BLOCK, 1)));
        expected.put("raw_gold_block", List.of(ItemStack.of(Material.RAW_GOLD_BLOCK, 1)));
        expected.put("raw_iron_block", List.of(ItemStack.of(Material.RAW_IRON_BLOCK, 1)));
        expected.put("red_banner", List.of(ItemStack.of(Material.RED_BANNER, 1)));
        expected.put("red_bed", List.of());
        expected.put("red_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("red_candle_cake", List.of(ItemStack.of(Material.RED_CANDLE, 1)));
        expected.put("red_carpet", List.of(ItemStack.of(Material.RED_CARPET, 1)));
        expected.put("red_concrete", List.of(ItemStack.of(Material.RED_CONCRETE, 1)));
        expected.put("red_concrete_powder", List.of(ItemStack.of(Material.RED_CONCRETE_POWDER, 1)));
        expected.put("red_glazed_terracotta", List.of(ItemStack.of(Material.RED_GLAZED_TERRACOTTA, 1)));
        expected.put("red_mushroom", List.of(ItemStack.of(Material.RED_MUSHROOM, 1)));
        expected.put("red_mushroom_block", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("red_nether_brick_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("red_nether_brick_stairs", List.of(ItemStack.of(Material.RED_NETHER_BRICK_STAIRS, 1)));
        expected.put("red_nether_brick_wall", List.of(ItemStack.of(Material.RED_NETHER_BRICK_WALL, 1)));
        expected.put("red_nether_bricks", List.of(ItemStack.of(Material.RED_NETHER_BRICKS, 1)));
        expected.put("red_sand", List.of(ItemStack.of(Material.RED_SAND, 1)));
        expected.put("red_sandstone", List.of(ItemStack.of(Material.RED_SANDSTONE, 1)));
        expected.put("red_sandstone_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("red_sandstone_stairs", List.of(ItemStack.of(Material.RED_SANDSTONE_STAIRS, 1)));
        expected.put("red_sandstone_wall", List.of(ItemStack.of(Material.RED_SANDSTONE_WALL, 1)));
        expected.put("red_shulker_box", List.of(ItemStack.of(Material.RED_SHULKER_BOX, 1)));
        expected.put("red_stained_glass", List.of());
        expected.put("red_stained_glass_pane", List.of());
        expected.put("red_terracotta", List.of(ItemStack.of(Material.RED_TERRACOTTA, 1)));
        expected.put("red_tulip", List.of(ItemStack.of(Material.RED_TULIP, 1)));
        expected.put("red_wool", List.of(ItemStack.of(Material.RED_WOOL, 1)));
        expected.put("redstone_block", List.of(ItemStack.of(Material.REDSTONE_BLOCK, 1)));
        expected.put("redstone_lamp", List.of(ItemStack.of(Material.REDSTONE_LAMP, 1)));
        expected.put("redstone_ore", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("redstone_torch", List.of(ItemStack.of(Material.REDSTONE_TORCH, 1)));
        expected.put("redstone_wire", List.of(ItemStack.of(Material.REDSTONE, 1)));
        expected.put("reinforced_deepslate", List.of());
        expected.put("repeater", List.of(ItemStack.of(Material.REPEATER, 1)));
        expected.put("respawn_anchor", List.of(ItemStack.of(Material.RESPAWN_ANCHOR, 1)));
        expected.put("rooted_dirt", List.of(ItemStack.of(Material.ROOTED_DIRT, 1)));
        expected.put("rose_bush", List.of());
        expected.put("sand", List.of(ItemStack.of(Material.SAND, 1)));
        expected.put("sandstone", List.of(ItemStack.of(Material.SANDSTONE, 1)));
        expected.put("sandstone_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("sandstone_stairs", List.of(ItemStack.of(Material.SANDSTONE_STAIRS, 1)));
        expected.put("sandstone_wall", List.of(ItemStack.of(Material.SANDSTONE_WALL, 1)));
        expected.put("scaffolding", List.of(ItemStack.of(Material.SCAFFOLDING, 1)));
        expected.put("sculk", List.of());
        expected.put("sculk_catalyst", List.of());
        expected.put("sculk_sensor", List.of());
        expected.put("sculk_shrieker", List.of());
        expected.put("sculk_vein", List.of());
        expected.put("sea_lantern", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("sea_pickle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("seagrass", List.of());
        expected.put("short_grass", List.of());
        expected.put("shroomlight", List.of(ItemStack.of(Material.SHROOMLIGHT, 1)));
        expected.put("shulker_box", List.of(ItemStack.of(Material.SHULKER_BOX, 1)));
        expected.put("skeleton_skull", List.of(ItemStack.of(Material.SKELETON_SKULL, 1)));
        expected.put("slime_block", List.of(ItemStack.of(Material.SLIME_BLOCK, 1)));
        expected.put("small_amethyst_bud", List.of());
        expected.put("small_dripleaf", List.of());
        expected.put("smithing_table", List.of(ItemStack.of(Material.SMITHING_TABLE, 1)));
        expected.put("smoker", List.of(ItemStack.of(Material.SMOKER, 1)));
        expected.put("smooth_basalt", List.of(ItemStack.of(Material.SMOOTH_BASALT, 1)));
        expected.put("smooth_quartz", List.of(ItemStack.of(Material.SMOOTH_QUARTZ, 1)));
        expected.put("smooth_quartz_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("smooth_quartz_stairs", List.of(ItemStack.of(Material.SMOOTH_QUARTZ_STAIRS, 1)));
        expected.put("smooth_red_sandstone", List.of(ItemStack.of(Material.SMOOTH_RED_SANDSTONE, 1)));
        expected.put("smooth_red_sandstone_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("smooth_red_sandstone_stairs", List.of(ItemStack.of(Material.SMOOTH_RED_SANDSTONE_STAIRS, 1)));
        expected.put("smooth_sandstone", List.of(ItemStack.of(Material.SMOOTH_SANDSTONE, 1)));
        expected.put("smooth_sandstone_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("smooth_sandstone_stairs", List.of(ItemStack.of(Material.SMOOTH_SANDSTONE_STAIRS, 1)));
        expected.put("smooth_stone", List.of(ItemStack.of(Material.SMOOTH_STONE, 1)));
        expected.put("smooth_stone_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("sniffer_egg", List.of(ItemStack.of(Material.SNIFFER_EGG, 1)));
        expected.put("snow", List.of());
        expected.put("snow_block", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("soul_campfire", List.of(ItemStack.of(Material.SOUL_SOIL, 1)));
        expected.put("soul_fire", List.of());
        expected.put("soul_lantern", List.of(ItemStack.of(Material.SOUL_LANTERN, 1)));
        expected.put("soul_sand", List.of(ItemStack.of(Material.SOUL_SAND, 1)));
        expected.put("soul_soil", List.of(ItemStack.of(Material.SOUL_SOIL, 1)));
        expected.put("soul_torch", List.of(ItemStack.of(Material.SOUL_TORCH, 1)));
        expected.put("spawner", List.of());
        expected.put("sponge", List.of(ItemStack.of(Material.SPONGE, 1)));
        expected.put("spore_blossom", List.of(ItemStack.of(Material.SPORE_BLOSSOM, 1)));
        expected.put("spruce_button", List.of(ItemStack.of(Material.SPRUCE_BUTTON, 1)));
        expected.put("spruce_door", List.of());
        expected.put("spruce_fence", List.of(ItemStack.of(Material.SPRUCE_FENCE, 1)));
        expected.put("spruce_fence_gate", List.of(ItemStack.of(Material.SPRUCE_FENCE_GATE, 1)));
        expected.put("spruce_hanging_sign", List.of(ItemStack.of(Material.SPRUCE_HANGING_SIGN, 1)));
        expected.put("spruce_leaves", List.of());
        expected.put("spruce_log", List.of(ItemStack.of(Material.SPRUCE_LOG, 1)));
        expected.put("spruce_planks", List.of(ItemStack.of(Material.SPRUCE_PLANKS, 1)));
        expected.put("spruce_pressure_plate", List.of(ItemStack.of(Material.SPRUCE_PRESSURE_PLATE, 1)));
        expected.put("spruce_sapling", List.of(ItemStack.of(Material.SPRUCE_SAPLING, 1)));
        expected.put("spruce_sign", List.of(ItemStack.of(Material.SPRUCE_SIGN, 1)));
        expected.put("spruce_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("spruce_stairs", List.of(ItemStack.of(Material.SPRUCE_STAIRS, 1)));
        expected.put("spruce_trapdoor", List.of(ItemStack.of(Material.SPRUCE_TRAPDOOR, 1)));
        expected.put("spruce_wood", List.of(ItemStack.of(Material.SPRUCE_WOOD, 1)));
        expected.put("sticky_piston", List.of(ItemStack.of(Material.STICKY_PISTON, 1)));
        expected.put("stone", List.of(ItemStack.of(Material.COBBLESTONE, 1)));
        expected.put("stone_brick_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("stone_brick_stairs", List.of(ItemStack.of(Material.STONE_BRICK_STAIRS, 1)));
        expected.put("stone_brick_wall", List.of(ItemStack.of(Material.STONE_BRICK_WALL, 1)));
        expected.put("stone_bricks", List.of(ItemStack.of(Material.STONE_BRICKS, 1)));
        expected.put("stone_button", List.of(ItemStack.of(Material.STONE_BUTTON, 1)));
        expected.put("stone_pressure_plate", List.of(ItemStack.of(Material.STONE_PRESSURE_PLATE, 1)));
        expected.put("stone_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("stone_stairs", List.of(ItemStack.of(Material.STONE_STAIRS, 1)));
        expected.put("stonecutter", List.of(ItemStack.of(Material.STONECUTTER, 1)));
        expected.put("stripped_acacia_log", List.of(ItemStack.of(Material.STRIPPED_ACACIA_LOG, 1)));
        expected.put("stripped_acacia_wood", List.of(ItemStack.of(Material.STRIPPED_ACACIA_WOOD, 1)));
        expected.put("stripped_bamboo_block", List.of(ItemStack.of(Material.STRIPPED_BAMBOO_BLOCK, 1)));
        expected.put("stripped_birch_log", List.of(ItemStack.of(Material.STRIPPED_BIRCH_LOG, 1)));
        expected.put("stripped_birch_wood", List.of(ItemStack.of(Material.STRIPPED_BIRCH_WOOD, 1)));
        expected.put("stripped_cherry_log", List.of(ItemStack.of(Material.STRIPPED_CHERRY_LOG, 1)));
        expected.put("stripped_cherry_wood", List.of(ItemStack.of(Material.STRIPPED_CHERRY_WOOD, 1)));
        expected.put("stripped_crimson_hyphae", List.of(ItemStack.of(Material.STRIPPED_CRIMSON_HYPHAE, 1)));
        expected.put("stripped_crimson_stem", List.of(ItemStack.of(Material.STRIPPED_CRIMSON_STEM, 1)));
        expected.put("stripped_dark_oak_log", List.of(ItemStack.of(Material.STRIPPED_DARK_OAK_LOG, 1)));
        expected.put("stripped_dark_oak_wood", List.of(ItemStack.of(Material.STRIPPED_DARK_OAK_WOOD, 1)));
        expected.put("stripped_jungle_log", List.of(ItemStack.of(Material.STRIPPED_JUNGLE_LOG, 1)));
        expected.put("stripped_jungle_wood", List.of(ItemStack.of(Material.STRIPPED_JUNGLE_WOOD, 1)));
        expected.put("stripped_mangrove_log", List.of(ItemStack.of(Material.STRIPPED_MANGROVE_LOG, 1)));
        expected.put("stripped_mangrove_wood", List.of(ItemStack.of(Material.STRIPPED_MANGROVE_WOOD, 1)));
        expected.put("stripped_oak_log", List.of(ItemStack.of(Material.STRIPPED_OAK_LOG, 1)));
        expected.put("stripped_oak_wood", List.of(ItemStack.of(Material.STRIPPED_OAK_WOOD, 1)));
        expected.put("stripped_spruce_log", List.of(ItemStack.of(Material.STRIPPED_SPRUCE_LOG, 1)));
        expected.put("stripped_spruce_wood", List.of(ItemStack.of(Material.STRIPPED_SPRUCE_WOOD, 1)));
        expected.put("stripped_warped_hyphae", List.of(ItemStack.of(Material.STRIPPED_WARPED_HYPHAE, 1)));
        expected.put("stripped_warped_stem", List.of(ItemStack.of(Material.STRIPPED_WARPED_STEM, 1)));
        expected.put("sugar_cane", List.of(ItemStack.of(Material.SUGAR_CANE, 1)));
        expected.put("sunflower", List.of());
        expected.put("suspicious_gravel", List.of());
        expected.put("suspicious_sand", List.of());
        expected.put("sweet_berry_bush", List.of());
        expected.put("tall_grass", List.of());
        expected.put("tall_seagrass", List.of());
        expected.put("target", List.of(ItemStack.of(Material.TARGET, 1)));
        expected.put("terracotta", List.of(ItemStack.of(Material.TERRACOTTA, 1)));
        expected.put("tinted_glass", List.of(ItemStack.of(Material.TINTED_GLASS, 1)));
        expected.put("tnt", List.of());
        expected.put("torch", List.of(ItemStack.of(Material.TORCH, 1)));
        expected.put("torchflower", List.of(ItemStack.of(Material.TORCHFLOWER, 1)));
        expected.put("torchflower_crop", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("trapped_chest", List.of(ItemStack.of(Material.TRAPPED_CHEST, 1)));
        expected.put("trial_spawner", List.of());
        expected.put("tripwire", List.of(ItemStack.of(Material.STRING, 1)));
        expected.put("tripwire_hook", List.of(ItemStack.of(Material.TRIPWIRE_HOOK, 1)));
        expected.put("tube_coral", List.of());
        expected.put("tube_coral_block", List.of(ItemStack.of(Material.DEAD_TUBE_CORAL_BLOCK, 1)));
        expected.put("tube_coral_fan", List.of());
        expected.put("tuff", List.of(ItemStack.of(Material.TUFF, 1)));
        expected.put("tuff_brick_slab", List.of());
        expected.put("tuff_brick_stairs", List.of());
        expected.put("tuff_brick_wall", List.of());
        expected.put("tuff_bricks", List.of());
        expected.put("tuff_slab", List.of());
        expected.put("tuff_stairs", List.of());
        expected.put("tuff_wall", List.of());
        expected.put("turtle_egg", List.of());
        expected.put("twisting_vines", List.of());
        expected.put("twisting_vines_plant", List.of());
        expected.put("verdant_froglight", List.of(ItemStack.of(Material.VERDANT_FROGLIGHT, 1)));
        expected.put("vine", List.of());
        expected.put("warped_button", List.of(ItemStack.of(Material.WARPED_BUTTON, 1)));
        expected.put("warped_door", List.of());
        expected.put("warped_fence", List.of(ItemStack.of(Material.WARPED_FENCE, 1)));
        expected.put("warped_fence_gate", List.of(ItemStack.of(Material.WARPED_FENCE_GATE, 1)));
        expected.put("warped_fungus", List.of(ItemStack.of(Material.WARPED_FUNGUS, 1)));
        expected.put("warped_hanging_sign", List.of(ItemStack.of(Material.WARPED_HANGING_SIGN, 1)));
        expected.put("warped_hyphae", List.of(ItemStack.of(Material.WARPED_HYPHAE, 1)));
        expected.put("warped_nylium", List.of(ItemStack.of(Material.NETHERRACK, 1)));
        expected.put("warped_planks", List.of(ItemStack.of(Material.WARPED_PLANKS, 1)));
        expected.put("warped_pressure_plate", List.of(ItemStack.of(Material.WARPED_PRESSURE_PLATE, 1)));
        expected.put("warped_roots", List.of(ItemStack.of(Material.WARPED_ROOTS, 1)));
        expected.put("warped_sign", List.of(ItemStack.of(Material.WARPED_SIGN, 1)));
        expected.put("warped_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("warped_stairs", List.of(ItemStack.of(Material.WARPED_STAIRS, 1)));
        expected.put("warped_stem", List.of(ItemStack.of(Material.WARPED_STEM, 1)));
        expected.put("warped_trapdoor", List.of(ItemStack.of(Material.WARPED_TRAPDOOR, 1)));
        expected.put("warped_wart_block", List.of(ItemStack.of(Material.WARPED_WART_BLOCK, 1)));
        expected.put("water_cauldron", List.of(ItemStack.of(Material.CAULDRON, 1)));
        expected.put("waxed_chiseled_copper", List.of());
        expected.put("waxed_copper_block", List.of(ItemStack.of(Material.WAXED_COPPER_BLOCK, 1)));
        expected.put("waxed_copper_bulb", List.of());
        expected.put("waxed_copper_door", List.of());
        expected.put("waxed_copper_grate", List.of());
        expected.put("waxed_copper_trapdoor", List.of());
        expected.put("waxed_cut_copper", List.of(ItemStack.of(Material.WAXED_CUT_COPPER, 1)));
        expected.put("waxed_cut_copper_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("waxed_cut_copper_stairs", List.of(ItemStack.of(Material.WAXED_CUT_COPPER_STAIRS, 1)));
        expected.put("waxed_exposed_chiseled_copper", List.of());
        expected.put("waxed_exposed_copper", List.of(ItemStack.of(Material.WAXED_EXPOSED_COPPER, 1)));
        expected.put("waxed_exposed_copper_bulb", List.of());
        expected.put("waxed_exposed_copper_door", List.of());
        expected.put("waxed_exposed_copper_grate", List.of());
        expected.put("waxed_exposed_copper_trapdoor", List.of());
        expected.put("waxed_exposed_cut_copper", List.of(ItemStack.of(Material.WAXED_EXPOSED_CUT_COPPER, 1)));
        expected.put("waxed_exposed_cut_copper_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("waxed_exposed_cut_copper_stairs", List.of(ItemStack.of(Material.WAXED_EXPOSED_CUT_COPPER_STAIRS, 1)));
        expected.put("waxed_oxidized_chiseled_copper", List.of());
        expected.put("waxed_oxidized_copper", List.of(ItemStack.of(Material.WAXED_OXIDIZED_COPPER, 1)));
        expected.put("waxed_oxidized_copper_bulb", List.of());
        expected.put("waxed_oxidized_copper_door", List.of());
        expected.put("waxed_oxidized_copper_grate", List.of());
        expected.put("waxed_oxidized_copper_trapdoor", List.of());
        expected.put("waxed_oxidized_cut_copper", List.of(ItemStack.of(Material.WAXED_OXIDIZED_CUT_COPPER, 1)));
        expected.put("waxed_oxidized_cut_copper_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("waxed_oxidized_cut_copper_stairs", List.of(ItemStack.of(Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS, 1)));
        expected.put("waxed_weathered_chiseled_copper", List.of());
        expected.put("waxed_weathered_copper", List.of(ItemStack.of(Material.WAXED_WEATHERED_COPPER, 1)));
        expected.put("waxed_weathered_copper_bulb", List.of());
        expected.put("waxed_weathered_copper_door", List.of());
        expected.put("waxed_weathered_copper_grate", List.of());
        expected.put("waxed_weathered_copper_trapdoor", List.of());
        expected.put("waxed_weathered_cut_copper", List.of(ItemStack.of(Material.WAXED_WEATHERED_CUT_COPPER, 1)));
        expected.put("waxed_weathered_cut_copper_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("waxed_weathered_cut_copper_stairs", List.of(ItemStack.of(Material.WAXED_WEATHERED_CUT_COPPER_STAIRS, 1)));
        expected.put("weathered_chiseled_copper", List.of());
        expected.put("weathered_copper", List.of(ItemStack.of(Material.WEATHERED_COPPER, 1)));
        expected.put("weathered_copper_bulb", List.of());
        expected.put("weathered_copper_door", List.of());
        expected.put("weathered_copper_grate", List.of());
        expected.put("weathered_copper_trapdoor", List.of());
        expected.put("weathered_cut_copper", List.of(ItemStack.of(Material.WEATHERED_CUT_COPPER, 1)));
        expected.put("weathered_cut_copper_slab", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("weathered_cut_copper_stairs", List.of(ItemStack.of(Material.WEATHERED_CUT_COPPER_STAIRS, 1)));
        expected.put("weeping_vines", List.of());
        expected.put("weeping_vines_plant", List.of());
        expected.put("wet_sponge", List.of(ItemStack.of(Material.WET_SPONGE, 1)));
        expected.put("wheat", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("white_banner", List.of(ItemStack.of(Material.WHITE_BANNER, 1)));
        expected.put("white_bed", List.of());
        expected.put("white_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("white_candle_cake", List.of(ItemStack.of(Material.WHITE_CANDLE, 1)));
        expected.put("white_carpet", List.of(ItemStack.of(Material.WHITE_CARPET, 1)));
        expected.put("white_concrete", List.of(ItemStack.of(Material.WHITE_CONCRETE, 1)));
        expected.put("white_concrete_powder", List.of(ItemStack.of(Material.WHITE_CONCRETE_POWDER, 1)));
        expected.put("white_glazed_terracotta", List.of(ItemStack.of(Material.WHITE_GLAZED_TERRACOTTA, 1)));
        expected.put("white_shulker_box", List.of(ItemStack.of(Material.WHITE_SHULKER_BOX, 1)));
        expected.put("white_stained_glass", List.of());
        expected.put("white_stained_glass_pane", List.of());
        expected.put("white_terracotta", List.of(ItemStack.of(Material.WHITE_TERRACOTTA, 1)));
        expected.put("white_tulip", List.of(ItemStack.of(Material.WHITE_TULIP, 1)));
        expected.put("white_wool", List.of(ItemStack.of(Material.WHITE_WOOL, 1)));
        expected.put("wither_rose", List.of(ItemStack.of(Material.WITHER_ROSE, 1)));
        expected.put("wither_skeleton_skull", List.of(ItemStack.of(Material.WITHER_SKELETON_SKULL, 1)));
        expected.put("yellow_banner", List.of(ItemStack.of(Material.YELLOW_BANNER, 1)));
        expected.put("yellow_bed", List.of());
        expected.put("yellow_candle", List.of(ItemStack.of(Material.AIR, 1)));
        expected.put("yellow_candle_cake", List.of(ItemStack.of(Material.YELLOW_CANDLE, 1)));
        expected.put("yellow_carpet", List.of(ItemStack.of(Material.YELLOW_CARPET, 1)));
        expected.put("yellow_concrete", List.of(ItemStack.of(Material.YELLOW_CONCRETE, 1)));
        expected.put("yellow_concrete_powder", List.of(ItemStack.of(Material.YELLOW_CONCRETE_POWDER, 1)));
        expected.put("yellow_glazed_terracotta", List.of(ItemStack.of(Material.YELLOW_GLAZED_TERRACOTTA, 1)));
        expected.put("yellow_shulker_box", List.of(ItemStack.of(Material.YELLOW_SHULKER_BOX, 1)));
        expected.put("yellow_stained_glass", List.of());
        expected.put("yellow_stained_glass_pane", List.of());
        expected.put("yellow_terracotta", List.of(ItemStack.of(Material.YELLOW_TERRACOTTA, 1)));
        expected.put("yellow_wool", List.of(ItemStack.of(Material.YELLOW_WOOL, 1)));
        expected.put("zombie_head", List.of(ItemStack.of(Material.ZOMBIE_HEAD, 1)));

        EXPECTED_RESULTS = Map.copyOf(expected);
    }

    public static final Map<String, Map<LootContext.Trait<?>, Object>> TRAITS;

    static {
        Map<String, Map<LootContext.Trait<?>, Object>> traits = new HashMap<>();

        // acacia_leaves : tool
        traits.put("acacia_leaves", Map.of(
            LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));
        // acacia_slab : explosion radius
        traits.put("acacia_slab", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // amethyst_cluster : explosion radius
        traits.put("amethyst_cluster", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // andesite_slab : explosion radius
        traits.put("andesite_slab", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // attached_pumpkin_stem : explosion_radius
        traits.put("attached_pumpkin_stem", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // azalea_leaves : tool
        traits.put("azalea_leaves", Map.of(
            LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));
        // bamboo_mosaic_slab : explosion_radius
        traits.put("bamboo_mosaic_slab", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // bamboo_slab : explosion_radius
        traits.put("bamboo_slab", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // barrel : block_entity
        traits.put("barrel", Map.of(
            LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // beacon : block_entity
        traits.put("beacon", Map.of(
            LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // beetroots : explosion_radius
        traits.put("beetroots", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // birch_leaves : tool
        traits.put("birch_leaves", Map.of(
            LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));
        // birch_slab : explosion_radius
        traits.put("birch_slab", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // black_banner : block_entity
        traits.put("black_banner", Map.of(
            LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // black_candle : explosion_radius
        traits.put("black_candle", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // black_shulker_box : block_entity
        traits.put("black_shulker_box", Map.of(
            LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // blackstone_slab : explosion_radius
        traits.put("blackstone_slab", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // blast_furnace : block_entity
        traits.put("blast_furnace", Map.of(
            LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // blue_banner : block_entity
        traits.put("blue_banner", Map.of(
            LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // blue_candle : explosion_radius
        traits.put("blue_candle", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // blue_shulker_box : block_entity
        traits.put("blue_shulker_box", Map.of(
            LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // bookshelf : explosion_radius
        traits.put("bookshelf", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // brewing_stand : block_entity
        traits.put("brewing_stand", Map.of(
            LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // brick_slab : explosion_radius
        traits.put("brick_slab", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // brown_banner : block_entity
        traits.put("brown_banner", Map.of(
            LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // brown_candle : explosion_radius
        traits.put("brown_candle", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // brown_mushroom : explosion_radius
        traits.put("brown_mushroom_block", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // brown_shulker_box : block_entity
        traits.put("brown_shulker_box", Map.of(
            LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // candle : explosion_radius
        traits.put("candle", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // carrots : explosion_radius
        traits.put("carrots", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // cherry_leaves : tool
        traits.put("cherry_leaves", Map.of(
            LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));
        // cherry_slab : explosion_radius
        traits.put("cherry_slab", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // chest : block_entity
        traits.put("chest", Map.of(
            LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // clay : explosion_radius
        traits.put("clay", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // coal_ore : explosion_radius
        traits.put("coal_ore", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // cobbled_deepslate_slab : explosion_radius
        traits.put("cobbled_deepslate_slab", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // cobblestone_slab : explosion_radius
        traits.put("cobblestone_slab", Map.of(
            LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // cocoa : explosion_radius
        traits.put("cocoa", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // composter : explosion_radius
        traits.put("composter", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // copper_ore : explosion_radius
        traits.put("copper_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // crimson_slab : explosion_radius
        traits.put("crimson_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // cut_copper_slab : explosion_radius
        traits.put("cut_copper_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // cut_red_sandstone_slab : explosion_radius
        traits.put("cut_red_sandstone_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // cut_sandstone_slab : explosion_radius
        traits.put("cut_sandstone_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // cyan_banner : block_entity
        traits.put("cyan_banner", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // cyan_candle : explosion_radius
        traits.put("cyan_candle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // cyan_shulker_box : block_entity
        traits.put("cyan_shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // dark_oak_leaves : tool
        traits.put("dark_oak_leaves", Map.of(
                LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));
        // dark_oak_slab : explosion_radius
        traits.put("dark_oak_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // dark_prismarine_slab : explosion_radius
        traits.put("dark_prismarine_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // decorated_pot : block_entity
        traits.put("decorated_pot", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // deepslate_brick_slab : explosion_radius
        traits.put("deepslate_brick_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // deepslate_coal_ore : explosion_radius
        traits.put("deepslate_coal_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // deepslate_copper_ore : explosion_radius
        traits.put("deepslate_copper_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // deepslate_diamond_ore : explosion_radius
        traits.put("deepslate_diamond_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // deepslate_emerald_ore : explosion_radius
        traits.put("deepslate_emerald_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // deepslate_gold_ore : explosion_radius
        traits.put("deepslate_gold_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // deepslate_iron_ore : explosion_radius
        traits.put("deepslate_iron_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // deepslate_lapis_ore : explosion_radius
        traits.put("deepslate_lapis_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // deepslate_tile_slab : explosion_radius
        traits.put("deepslate_tile_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // diamond_ore : explosion_radius
        traits.put("diamond_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // diorite_slab : explosion_radius
        traits.put("diorite_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // dispenser : block_entity
        traits.put("dispenser", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // dropper : block_entity
        traits.put("dropper", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // emerald_ore : explosion_radius
        traits.put("emerald_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // enchanting_table : block_entity
        traits.put("enchanting_table", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // end_stone_brick_slab : explosion_radius
        traits.put("end_stone_brick_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // ender_chest : explosion_radius
        traits.put("ender_chest", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // exposed_cut_copper_slab : explosion_radius
        traits.put("exposed_cut_copper_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // flowering_azalea_leaves : tool
        traits.put("flowering_azalea_leaves", Map.of(
                LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));
        // furnace : block_entity
        traits.put("furnace", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // gilded_blackstone : tool
        traits.put("gilded_blackstone", Map.of(
                LootContext.TOOL, ItemStack.of(Material.DIAMOND_PICKAXE, 1)
        ));
        // gold_ore : explosion_radius
        traits.put("gold_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // granite_slab : explosion_radius
        traits.put("granite_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // gravel : tool
        traits.put("gravel", Map.of(
                LootContext.TOOL, ItemStack.of(Material.IRON_SHOVEL, 1)
        ));
        // gray_banner : block_entity
        traits.put("gray_banner", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // gray_candle : explosion_radius
        traits.put("gray_candle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // gray_shulker_box : block_entity
        traits.put("gray_shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // green_banner : block_entity
        traits.put("green_banner", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // green_candle : explosion_radius
        traits.put("green_candle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // green_shulker_box : block_entity
        traits.put("green_shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // hopper : block_entity
        traits.put("hopper", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // iron_ore : explosion_radius
        traits.put("iron_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // jungle_leaves : tool
        traits.put("jungle_leaves", Map.of(
                LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));
        // jungle_slab : explosion_radius
        traits.put("jungle_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // lapis_ore : explosion_radius
        traits.put("lapis_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // light_blue_banner : block_entity
        traits.put("light_blue_banner", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // light_blue_candle : explosion_radius
        traits.put("light_blue_candle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // light_blue_shulker_box : block_entity
        traits.put("light_blue_shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // light_gray_banner : block_entity
        traits.put("light_gray_banner", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // light_gray_candle : explosion_radius
        traits.put("light_gray_candle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // light_gray_shulker_box : block_entity
        traits.put("light_gray_shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // lime_banner : block_entity
        traits.put("lime_banner", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // lime_candle : explosion_radius
        traits.put("lime_candle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // lime_shulker_box : block_entity
        traits.put("lime_shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // magenta_banner : block_entity
        traits.put("magenta_banner", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // magenta_candle : explosion_radius
        traits.put("magenta_candle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // magenta_shulker_box : block_entity
        traits.put("magenta_shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // mangrove_leaves : tool
        traits.put("mangrove_leaves", Map.of(
                LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));
        // mangrove_slab : explosion_radius
        traits.put("mangrove_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // mossy_cobblestone_slab : explosion_radius
        traits.put("mossy_cobblestone_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // mossy_stone_brick_slab : explosion_radius
        traits.put("mossy_stone_brick_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // mud_brick_slab : explosion_radius
        traits.put("mud_brick_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // nether_brick_slab : explosion_radius
        traits.put("nether_brick_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // nether_gold_ore : explosion_radius
        traits.put("nether_gold_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // nether_quartz_ore : explosion_radius
        traits.put("nether_quartz_ore", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // oak_leaves : tool
        traits.put("oak_leaves", Map.of(
                LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));
        // oak_slab : explosion_radius
        traits.put("oak_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // orange_banner : block_entity
        traits.put("orange_banner", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // orange_candle : explosion_radius
        traits.put("orange_candle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // orange_shulker_box : block_entity
        traits.put("orange_shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // oxidized_cut_copper_slab : explosion_radius
        traits.put("oxidized_cut_copper_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // petrified_oak_slab : explosion_radius
        traits.put("petrified_oak_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // pink_banner : block_entity
        traits.put("pink_banner", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // pink_candle : explosion_radius
        traits.put("pink_candle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // pink_petals : explosion_radius
        traits.put("pink_petals", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // pink_shulker_box : block_entity
        traits.put("pink_shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // player_head : block_entity
        traits.put("player_head", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // polished_andesite_slab : explosion_radius
        traits.put("polished_andesite_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // polished_blackstone_brick_slab : explosion_radius
        traits.put("polished_blackstone_brick_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // polished_blackstone_slab : explosion_radius
        traits.put("polished_blackstone_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // polished_deepslate_slab : explosion_radius
        traits.put("polished_deepslate_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // polished_diorite_slab : explosion_radius
        traits.put("polished_diorite_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // polished_granite_slab : explosion_radius
        traits.put("polished_granite_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // potatoes : explosion_radius
        traits.put("potatoes", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // prismarine_brick_slab : explosion_radius
        traits.put("prismarine_brick_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // prismarine_slab : explosion_radius
        traits.put("prismarine_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // purple_banner : block_entity
        traits.put("purple_banner", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // purple_candle : explosion_radius
        traits.put("purple_candle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // purple_shulker_box : block_entity
        traits.put("purple_shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // purpur_slab : explosion_radius
        traits.put("purpur_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // quartz_slab : explosion_radius
        traits.put("quartz_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // red_banner : block_entity
        traits.put("red_banner", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // red_candle : explosion_radius
        traits.put("red_candle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // red_nether_brick_slab : explosion_radius
        traits.put("red_nether_brick_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // red_sandstone_slab : explosion_radius
        traits.put("red_sandstone_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // red_shulker_box : block_entity
        traits.put("red_shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // sandstone_slab : explosion_radius
        traits.put("sandstone_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // sea_pickle : explosion_radius
        traits.put("sea_pickle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // shulker_box : block_entity
        traits.put("shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // smoker : block_entity
        traits.put("smoker", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // smooth_quartz_slab : explosion_radius
        traits.put("smooth_quartz_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // smooth_red_sandstone_slab : explosion_radius
        traits.put("smooth_red_sandstone_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // smooth_sandstone_slab : explosion_radius
        traits.put("smooth_sandstone_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // smooth_stone_slab : explosion_radius
        traits.put("smooth_stone_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // snow_block : explosion_radius
        traits.put("snow_block", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));

        // spruce_leaves : tool
        traits.put("spruce_leaves", Map.of(
                LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));

        // spruce_slab : explosion_radius
        traits.put("spruce_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // stone_brick_slab : explosion_radius
        traits.put("stone_brick_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // stone_slab : explosion_radius
        traits.put("stone_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // torchflower_crop : explosion_radius
        traits.put("torchflower_crop", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // trapped_chest : block_entity
        traits.put("trapped_chest", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // twisting_vines : tool
        traits.put("twisting_vines", Map.of(
                LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));
        // twisting_vines_plant : tool
        traits.put("twisting_vines_plant", Map.of(
                LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));
        // warped_slab : explosion_radius
        traits.put("warped_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // waxed_cut_copper_slab : explosion_radius
        traits.put("waxed_cut_copper_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // waxed_exposed_cut_copper_slab : explosion_radius
        traits.put("waxed_exposed_cut_copper_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // waxed_oxidized_cut_copper_slab : explosion_radius
        traits.put("waxed_oxidized_cut_copper_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // waxed_weathered_cut_copper_slab : explosion_radius
        traits.put("waxed_weathered_cut_copper_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // weathered_cut_copper_slab : explosion_radius
        traits.put("weathered_cut_copper_slab", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // weeping_vines : tool
        traits.put("weeping_vines", Map.of(
                LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));
        // weeping_vines_plant : tool
        traits.put("weeping_vines_plant", Map.of(
                LootContext.TOOL, ItemStack.of(Material.DIAMOND_AXE, 1)
        ));
        // wheat : explosion_radius
        traits.put("wheat", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // white_banner : block_entity
        traits.put("white_banner", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // white_candle : explosion_radius
        traits.put("white_candle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // white_shulker_box : block_entity
        traits.put("white_shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // yellow_banner : block_entity
        traits.put("yellow_banner", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));
        // yellow_candle : explosion_radius
        traits.put("yellow_candle", Map.of(
                LootContext.EXPLOSION_RADIUS, 0.0
        ));
        // yellow_shulker_box : block_entity
        traits.put("yellow_shulker_box", Map.of(
                LootContext.BLOCK_ENTITY, CompoundBinaryTag.empty()
        ));


        TRAITS = Map.copyOf(traits);
    }
}
