package net.minestom.vanilla.blocks.group;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.blocks.group.block.BlockGroup;
import net.minestom.vanilla.blocks.group.placement.PlacementGroup;
import net.minestom.vanilla.blocks.placement.*;

import java.util.function.Function;


/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class VanillaPlacementRules extends VanillaRuleset<PlacementGroup, Function<Block, BlockPlacementRule>> {

    public static final VanillaPlacementRules INSTANCE = new VanillaPlacementRules();

    public static final PlacementGroup ROTATED_PILLARS = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byTag("minecraft:logs"),
            INSTANCE.byBlock(Block.MUDDY_MANGROVE_ROOTS),
            INSTANCE.byBlock(Block.BAMBOO_BLOCK),
            INSTANCE.byBlock(Block.STRIPPED_BAMBOO_BLOCK),
            INSTANCE.byBlock(Block.BASALT),
            INSTANCE.byBlock(Block.POLISHED_BASALT),
            INSTANCE.byBlock(Block.QUARTZ_PILLAR),
            INSTANCE.byBlock(Block.PURPUR_PILLAR),
            INSTANCE.byBlock(Block.BONE_BLOCK),
            INSTANCE.byBlock(Block.DEEPSLATE),
            INSTANCE.byBlock(Block.INFESTED_DEEPSLATE),
            INSTANCE.byBlock(Block.OCHRE_FROGLIGHT),
            INSTANCE.byBlock(Block.VERDANT_FROGLIGHT),
            INSTANCE.byBlock(Block.PEARLESCENT_FROGLIGHT),
            INSTANCE.byBlock(Block.HAY_BLOCK)
        ),
        RotatedPillarPlacementRule::new
    );

    public static final PlacementGroup SLAB = INSTANCE.group(
        INSTANCE.byTag("minecraft:slabs"),
        SlabPlacementRule::new
    );

    public static final PlacementGroup VERTICALLY_ROTATED = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byBlock(Block.FURNACE),
            INSTANCE.byBlock(Block.BLAST_FURNACE),
            INSTANCE.byBlock(Block.SMOKER),
            INSTANCE.byBlock(Block.LECTERN),
            INSTANCE.byBlock(Block.ENDER_CHEST),
            INSTANCE.byBlock(Block.CHISELED_BOOKSHELF),
            INSTANCE.byBlock(Block.CARVED_PUMPKIN),
            INSTANCE.byBlock(Block.JACK_O_LANTERN),
            INSTANCE.byBlock(Block.BEEHIVE),
            INSTANCE.byBlock(Block.STONECUTTER),
            INSTANCE.byBlock(Block.LOOM),
            INSTANCE.byBlock(Block.BEE_NEST),
            INSTANCE.byBlock(Block.END_PORTAL_FRAME),
            INSTANCE.byBlock(Block.VAULT)
        ),
        VerticallyRotatedPlacementRule::new
    );

    public static final PlacementGroup ROTATED_WORKSTATIONS = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byBlock(Block.ANVIL),
            INSTANCE.byBlock(Block.CHIPPED_ANVIL),
            INSTANCE.byBlock(Block.DAMAGED_ANVIL)
        ),
        InverseWorkstationPlacementRule::new
    );

    public static final PlacementGroup AMETHYST = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byBlock(Block.AMETHYST_CLUSTER),
            INSTANCE.byBlock(Block.SMALL_AMETHYST_BUD),
            INSTANCE.byBlock(Block.MEDIUM_AMETHYST_BUD),
            INSTANCE.byBlock(Block.LARGE_AMETHYST_BUD)
        ),
        AmethystPlacementRule::new
    );

    public static final PlacementGroup BAMBOO = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byBlock(Block.BAMBOO),
            INSTANCE.byBlock(Block.BAMBOO_SAPLING)
        ),
        BambooPlantPlacementRule::new
    );

    public static final PlacementGroup BANNER = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byTag("minecraft:banners")
        ),
        BannerPlacementRule::new
    );

    public static final PlacementGroup FACING = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byBlock(Block.BARREL),
            INSTANCE.byBlock(Block.PISTON),
            INSTANCE.byBlock(Block.STICKY_PISTON),
            INSTANCE.byBlock(Block.COMMAND_BLOCK),
            INSTANCE.byBlock(Block.CHAIN_COMMAND_BLOCK),
            INSTANCE.byBlock(Block.REPEATING_COMMAND_BLOCK),
            INSTANCE.byBlock(Block.DROPPER),
            INSTANCE.byBlock(Block.DISPENSER)
        ),
        FacingPlacementRule::new
    );

    public static final PlacementGroup OBSERVER = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byBlock(Block.OBSERVER)
        ),
        ObserverPlacementRule::new
    );

    public static final PlacementGroup SIMPLE_WATERLOGGABLE = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byBlock(Block.BARRIER),
            INSTANCE.byBlock(Block.COPPER_GRATE),
            INSTANCE.byBlock(Block.EXPOSED_COPPER_GRATE),
            INSTANCE.byBlock(Block.WEATHERED_COPPER_GRATE),
            INSTANCE.byBlock(Block.OXIDIZED_COPPER_GRATE),
            INSTANCE.byBlock(Block.WAXED_COPPER_GRATE),
            INSTANCE.byBlock(Block.WAXED_EXPOSED_COPPER_GRATE),
            INSTANCE.byBlock(Block.WAXED_WEATHERED_COPPER_GRATE),
            INSTANCE.byBlock(Block.WAXED_OXIDIZED_COPPER_GRATE),
//            INSTANCE.byBlock(Block.DRIED_GHAST),
            INSTANCE.byBlock(Block.HEAVY_CORE)
        ),
        SimpleWaterloggablePlacementRule::new
    );

    public static final PlacementGroup BEDS = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byTag("minecraft:beds")
        ),
        BedPlacementRule::new
    );

    public static final PlacementGroup CROPS = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byTag("minecraft:crops")
        ),
        CropPlacementRule::new
    );

    public static final PlacementGroup BELL = INSTANCE.group(
        INSTANCE.byBlock(Block.BELL),
        BellPlacementRule::new
    );

    public static final PlacementGroup BIG_DRIPLEAF = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byBlock(Block.BIG_DRIPLEAF),
            INSTANCE.byBlock(Block.BIG_DRIPLEAF_STEM)
        ),
        BigDripleafPlacementRule::new
    );

    public static final PlacementGroup BOTTOM_SUPPORTED = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byTag("minecraft:pressure_plates"),
            INSTANCE.byBlock(Block.CAKE)
        ),
        SupportedBelowPlacementRule::new
    );

    public static final PlacementGroup PIN_BOTTOM_SUPPORTED = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byTag("minecraft:wool_carpets")
        ),
        PinSupportedBelowPlacementRule::new
    );

    public static final PlacementGroup BUTTONS = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byTag("minecraft:buttons")
        ),
        FacedFacingPlacementRule::new
    );

    public static final PlacementGroup CACTUS = INSTANCE.group(
        INSTANCE.byBlock(Block.CACTUS),
        CactusPlacementRule::new
    );

    public static final PlacementGroup CAMPFIRE = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byBlock(Block.CAMPFIRE),
            INSTANCE.byBlock(Block.SOUL_CAMPFIRE)
        ),
        CampfireBlockPlacementRule::new
    );

    public static final PlacementGroup CANDLES = INSTANCE.group(
        INSTANCE.byTag("minecraft:candles"),
        CandlePlacementRule::new
    );

    public static final PlacementGroup VINES_TOP = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byBlock(Block.CAVE_VINES),
            INSTANCE.byBlock(Block.WEEPING_VINES)
        ),
        TopAttachedVinePlacementRule::new
    );

    public static final PlacementGroup TRAPDOOR = INSTANCE.group(
        INSTANCE.all(
          INSTANCE.byTag("minecraft:trapdoors")
        ),
        TrapdoorPlacementRule::new
    );

    public static final PlacementGroup FENCE = INSTANCE.group(
        INSTANCE.all(
          INSTANCE.byTag("minecraft:fences")
        ),
        FencePlacementRule::new
    );

    public static final PlacementGroup FENCE_GATE = INSTANCE.group(
        INSTANCE.all(
          INSTANCE.byTag("minecraft:fence_gates")
        ),
        FenceGatePlacementRule::new
    );

    public static final PlacementGroup STAIRS = INSTANCE.group(
        INSTANCE.all(
          INSTANCE.byTag("minecraft:stairs")
        ),
        StairsPlacementRule::new
    );

    public static final PlacementGroup VERTICAL_SLIM = INSTANCE.group(
        INSTANCE.all(
          INSTANCE.byBlock(Block.IRON_BARS),
          INSTANCE.byTag("vri:glass_panes")
        ),
        VerticalSlimBlockPlacementRule::new
    );

    public static final PlacementGroup LADDERS = INSTANCE.group(
        INSTANCE.all(
          INSTANCE.byTag("minecraft:ladders")
        ),
        LadderPlacementRule::new
    );

    public static final PlacementGroup TORCHES = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byBlock(Block.TORCH),
            INSTANCE.byBlock(Block.SOUL_TORCH),
            INSTANCE.byBlock(Block.REDSTONE_TORCH),
            INSTANCE.byBlock(Block.WALL_TORCH),
            INSTANCE.byBlock(Block.SOUL_WALL_TORCH),
            INSTANCE.byBlock(Block.SOUL_FIRE)
        ),
      TorchPlacementRule::new
    );


    public static final PlacementGroup WALLS = INSTANCE.group(
        INSTANCE.byTag("minecraft:walls"),
        WallBlockPlacementRule::new
    );

    public static final PlacementGroup DOORS = INSTANCE.group(
        INSTANCE.byTag("minecraft:doors"),
        DoorPlacementRule::new
    );

    public static final PlacementGroup LANTERNS = INSTANCE.group(
        INSTANCE.all(
          INSTANCE.byBlock(Block.LANTERN),
          INSTANCE.byBlock(Block.SOUL_LANTERN)
        ),
        LanternPlacementRule::new
    );

    public static final PlacementGroup GLAZED_TERRACOTTA = INSTANCE.group(
        INSTANCE.all(
            INSTANCE.byBlock(Block.MAGENTA_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.WHITE_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.LIGHT_GRAY_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.GRAY_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.BLACK_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.BROWN_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.RED_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.ORANGE_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.YELLOW_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.LIME_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.GREEN_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.CYAN_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.LIGHT_BLUE_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.BLUE_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.PURPLE_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.MAGENTA_GLAZED_TERRACOTTA),
            INSTANCE.byBlock(Block.PINK_GLAZED_TERRACOTTA)
        ),
        GlazedTerracottaPlacementRule::new
    );

    public static final PlacementGroup CHAINS = INSTANCE.group(
        INSTANCE.byBlock(Block.CHAIN),
        ChainPlacementRule::new
    );

    public static final PlacementGroup TALL_FLOWERS = INSTANCE.group(
        INSTANCE.all(
          INSTANCE.byBlock(Block.PEONY),
          INSTANCE.byBlock(Block.TALL_GRASS),
          INSTANCE.byBlock(Block.LARGE_FERN),
          INSTANCE.byBlock(Block.SUNFLOWER),
          INSTANCE.byBlock(Block.LILAC),
          INSTANCE.byBlock(Block.ROSE_BUSH)
        ),
        TallFlowerPlacementRule::new
    );

    public static final PlacementGroup SIGNS = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byTag("minecraft:all_signs")
      ),
      SignPlacementRule::new
    );

    public static final PlacementGroup CHESTS = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byTag("minecraft:chests"),
        INSTANCE.byBlock(Block.CHEST),
        INSTANCE.byBlock(Block.TRAPPED_CHEST)
      ),
      ChestPlacementRule::new
    );

    public static final PlacementGroup HOPPERS = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byBlock(Block.HOPPER)
      ),
      HopperPlacementRule::new
    );

    public static final PlacementGroup SHULKERBOXES = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byTag("minecraft:shulker_boxes")
      ),
      ShulkerPlacementRule::new
    );

    public static final PlacementGroup FLOOR_FLOWER = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byBlock(Block.WILDFLOWERS),
        INSTANCE.byBlock(Block.LEAF_LITTER),
        INSTANCE.byBlock(Block.PINK_PETALS)
      ),
      FloorFillerPlacementRule::new
    );

    public static final PlacementGroup CORALS = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byTag("minecraft:corals"),
        INSTANCE.byBlock(Block.DEAD_TUBE_CORAL),
        INSTANCE.byBlock(Block.DEAD_BRAIN_CORAL),
        INSTANCE.byBlock(Block.DEAD_BUBBLE_CORAL),
        INSTANCE.byBlock(Block.DEAD_FIRE_CORAL),
        INSTANCE.byBlock(Block.DEAD_HORN_CORAL),
        INSTANCE.byBlock(Block.DEAD_TUBE_CORAL_FAN),
        INSTANCE.byBlock(Block.DEAD_BRAIN_CORAL_FAN),
        INSTANCE.byBlock(Block.DEAD_BUBBLE_CORAL_FAN),
        INSTANCE.byBlock(Block.DEAD_FIRE_CORAL_FAN),
        INSTANCE.byBlock(Block.DEAD_HORN_CORAL_FAN)
      ),
      CoralPlacementRule::new
    );

    public static final PlacementGroup WALL_CORALS = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byBlock(Block.TUBE_CORAL_WALL_FAN),
        INSTANCE.byBlock(Block.BRAIN_CORAL_WALL_FAN),
        INSTANCE.byBlock(Block.BUBBLE_CORAL_WALL_FAN),
        INSTANCE.byBlock(Block.FIRE_CORAL_WALL_FAN),
        INSTANCE.byBlock(Block.HORN_CORAL_WALL_FAN),
        INSTANCE.byBlock(Block.DEAD_TUBE_CORAL_WALL_FAN),
        INSTANCE.byBlock(Block.DEAD_BRAIN_CORAL_WALL_FAN),
        INSTANCE.byBlock(Block.DEAD_BUBBLE_CORAL_WALL_FAN),
        INSTANCE.byBlock(Block.DEAD_FIRE_CORAL_WALL_FAN),
        INSTANCE.byBlock(Block.DEAD_HORN_CORAL_WALL_FAN)
      ),
      WallCoralPlacementRule::new
    );

    public static final PlacementGroup HEADS = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byBlock(Block.SKELETON_SKULL),
        INSTANCE.byBlock(Block.WITHER_SKELETON_SKULL),
        INSTANCE.byBlock(Block.ZOMBIE_HEAD),
        INSTANCE.byBlock(Block.CREEPER_HEAD),
        INSTANCE.byBlock(Block.DRAGON_HEAD),
        INSTANCE.byBlock(Block.PLAYER_HEAD),
        INSTANCE.byBlock(Block.PIGLIN_HEAD)
      ),
      HeadPlacementRule::new
    );

    public static final PlacementGroup SUGAR_CANE = INSTANCE.group(
      INSTANCE.byBlock(Block.SUGAR_CANE),
      SugarCanePlacementRule::new
    );

    public static final PlacementGroup GROUNDED_PLANTS = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byTag("minecraft:saplings"),
        INSTANCE.byTag("minecraft:small_flowers")
      ),
      GroundedPlantBlockPlacementRule::new
    );

    public static final PlacementGroup CRAFTER = INSTANCE.group(
      INSTANCE.byBlock(Block.CRAFTER),
      CrafterPlacementRule::new
    );

    public static final PlacementGroup LEVER = INSTANCE.group(
      INSTANCE.byBlock(Block.LEVER),
      LeverPlacementRule::new
    );

    public static final PlacementGroup REDSTONE_STUFF = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byBlock(Block.COMPARATOR),
        INSTANCE.byBlock(Block.REPEATER)
      ),
      RedstoneStuffPlacementRule::new
    );

    public static final PlacementGroup FARMLAND = INSTANCE.group(
      INSTANCE.byBlock(Block.FARMLAND),
      FarmlandPlacementRule::new
    );

    public static final PlacementGroup SNOWY = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byBlock(Block.GRASS_BLOCK),
        INSTANCE.byBlock(Block.PODZOL),
        INSTANCE.byBlock(Block.MYCELIUM)
      ),
      SnowyUpdateRule::new
    );

    public static final PlacementGroup MUSHROOM = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byBlock(Block.MUSHROOM_STEM),
        INSTANCE.byBlock(Block.BROWN_MUSHROOM_BLOCK),
        INSTANCE.byBlock(Block.RED_MUSHROOM_BLOCK)
      ),
      MushroomPlacementRule::new
    );

    public static final PlacementGroup RAIL = INSTANCE.group(
      INSTANCE.byBlock(Block.RAIL),
      RailPlacementRule::new
    );

    public static final PlacementGroup FEATURE_RAIL = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byBlock(Block.ACTIVATOR_RAIL),
        INSTANCE.byBlock(Block.DETECTOR_RAIL),
        INSTANCE.byBlock(Block.POWERED_RAIL)
      ),
      FeatureRailPlacementRule::new
    );

    public static final PlacementGroup GRINDSTONE = INSTANCE.group(
      INSTANCE.all(
        INSTANCE.byBlock(Block.GRINDSTONE)
      ),
      GrindstonePlacementRule::new
    );

    @Override
    protected PlacementGroup createGroup(BlockGroup blockGroup, Function<Block, BlockPlacementRule> valueFunction) {
        return new PlacementGroup(blockGroup, valueFunction);
    }
}
