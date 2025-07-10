package net.minestom.vanilla.blocks;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.vanilla.blocks.group.VanillaPlacementRules;
import net.minestom.vanilla.blocks.group.placement.PlacementGroup;

public class BlockPlacementRuleRegistrations {
    private static final ComponentLogger logger = MinecraftServer.LOGGER;

    public static void registerDefault() {
        register(VanillaPlacementRules.ROTATED_PILLARS);
        register(VanillaPlacementRules.SLAB);
        register(VanillaPlacementRules.VERTICALLYROTATED);
        register(VanillaPlacementRules.ROTATED_WORKSTATIONS);
        register(VanillaPlacementRules.AMETHYST);
        register(VanillaPlacementRules.BAMBOO);
        register(VanillaPlacementRules.BANNER);
        register(VanillaPlacementRules.FACING);
        register(VanillaPlacementRules.OBSERVER);
        register(VanillaPlacementRules.SIMPLE_WATERLOGGABLE);
        register(VanillaPlacementRules.BEDS);
        register(VanillaPlacementRules.CROPS);
        register(VanillaPlacementRules.BELL);
        register(VanillaPlacementRules.BIG_DRIPLEAF);
        register(VanillaPlacementRules.BOTTOM_SUPPORTED);
        register(VanillaPlacementRules.PIN_BOTTOM_SUPPORTED);
        register(VanillaPlacementRules.BUTTONS);
        register(VanillaPlacementRules.CACTUS);
        register(VanillaPlacementRules.CAMPFIRE);
        register(VanillaPlacementRules.CANDLES);
        register(VanillaPlacementRules.VINES_TOP);
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

        logger.info("Registered " + count + " block placement rules");
    }
}
