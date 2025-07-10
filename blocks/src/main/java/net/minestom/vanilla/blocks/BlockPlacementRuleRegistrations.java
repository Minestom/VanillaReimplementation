package net.minestom.vanilla.blocks;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.vanilla.blocks.group.VanillaPlacementRules;
import net.minestom.vanilla.blocks.group.placement.PlacementGroup;

public class BlockPlacementRuleRegistrations {
    private static final ComponentLogger logger = MinecraftServer.LOGGER;

    private static final VanillaPlacementRules PLACEMENT_RULES = VanillaPlacementRules.INSTANCE;

    public static void registerDefault() {
        register(PLACEMENT_RULES.ALL.toArray(new PlacementGroup[0]));
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
