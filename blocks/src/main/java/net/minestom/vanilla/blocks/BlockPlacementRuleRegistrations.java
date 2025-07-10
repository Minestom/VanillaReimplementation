package net.minestom.vanilla.blocks;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.vanilla.blocks.group.VanillaPlacementRules;
import net.minestom.vanilla.blocks.group.placement.PlacementGroup;

public class BlockPlacementRuleRegistrations {
    private static final ComponentLogger logger = MinecraftServer.LOGGER;

    public static void registerDefault() {
        register(
          VanillaPlacementRules.ROTATED_PILLARS,
          VanillaPlacementRules.SLAB,
          VanillaPlacementRules.VERTICALLY_ROTATED,
          VanillaPlacementRules.ROTATED_WORKSTATIONS,
          VanillaPlacementRules.AMETHYST,
          VanillaPlacementRules.BAMBOO,
          VanillaPlacementRules.BANNER,
          VanillaPlacementRules.FACING,
          VanillaPlacementRules.OBSERVER,
          VanillaPlacementRules.SIMPLE_WATERLOGGABLE,
          VanillaPlacementRules.BEDS,
          VanillaPlacementRules.CROPS,
          VanillaPlacementRules.BELL,
          VanillaPlacementRules.BIG_DRIPLEAF,
          VanillaPlacementRules.BOTTOM_SUPPORTED,
          VanillaPlacementRules.PIN_BOTTOM_SUPPORTED,
          VanillaPlacementRules.BUTTONS,
          VanillaPlacementRules.CACTUS,
          VanillaPlacementRules.CAMPFIRE,
          VanillaPlacementRules.CANDLES,
          VanillaPlacementRules.VINES_TOP,
          VanillaPlacementRules.TRAPDOOR,
          VanillaPlacementRules.FENCE,
          VanillaPlacementRules.FENCE_GATE,
          VanillaPlacementRules.STAIRS,
          VanillaPlacementRules.VERTICAL_SLIM,
          VanillaPlacementRules.LADDERS,
          VanillaPlacementRules.TORCHES,
          VanillaPlacementRules.WALLS,
          VanillaPlacementRules.DOORS,
          VanillaPlacementRules.LANTERNS,
          VanillaPlacementRules.GLAZED_TERRACOTTA,
          VanillaPlacementRules.CHAINS,
          VanillaPlacementRules.TALL_FLOWERS,
          VanillaPlacementRules.SIGNS,
          VanillaPlacementRules.CHESTS,
          VanillaPlacementRules.HOPPERS,
          VanillaPlacementRules.SHULKERBOXES,
          VanillaPlacementRules.FLOOR_FLOWER,
          VanillaPlacementRules.CORALS,
          VanillaPlacementRules.WALL_CORALS,
          VanillaPlacementRules.HEADS,
          VanillaPlacementRules.SUGAR_CANE,
          VanillaPlacementRules.GROUNDED_PLANTS,
          VanillaPlacementRules.CRAFTER,
          VanillaPlacementRules.LEVER,
          VanillaPlacementRules.REDSTONE_STUFF,
          VanillaPlacementRules.FARMLAND,
          VanillaPlacementRules.SNOWY,
          VanillaPlacementRules.MUSHROOM,
          VanillaPlacementRules.RAIL,
          VanillaPlacementRules.FEATURE_RAIL,
          VanillaPlacementRules.GRINDSTONE
        );
    }

    public static void register(PlacementGroup... blockGroups) {
        var blockManager = MinecraftServer.getBlockManager();
        int count = 0;

        for (PlacementGroup placementGroup : blockGroups) {
            var blockGroup = placementGroup.getBlockGroup();
            for (var block : blockGroup.allMatching()) {
                count++;
                blockManager.registerBlockPlacementRule(placementGroup.createRule(block));
            }
        }

      logger.info("Registered {} block placement rules", count);
    }
}
