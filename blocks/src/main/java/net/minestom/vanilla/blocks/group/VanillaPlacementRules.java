package net.minestom.vanilla.blocks.group;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.vanilla.blocks.group.block.BlockGroup;
import net.minestom.vanilla.blocks.group.placement.PlacementGroup;
import net.minestom.vanilla.blocks.placement.*;

import java.util.function.Function;

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

    public static final PlacementGroup VERTICALLYROTATED = INSTANCE.group(
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

    @Override
    protected PlacementGroup createGroup(BlockGroup blockGroup, Function<Block, BlockPlacementRule> valueFunction) {
        return new PlacementGroup(blockGroup, valueFunction);
    }
}
