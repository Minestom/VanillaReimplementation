package net.minestom.vanilla.blocks;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.vanilla.blocks.group.VanillaPlacementRules;
import net.minestom.vanilla.blocks.group.placement.PlacementGroup;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
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
