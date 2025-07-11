package net.minestom.vanilla.blocks;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.vanilla.blocks.group.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.group.behaviour.BehaviourGroup;

/**
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 */
public class BlockBehaviorRuleRegistrations {
    private static final ComponentLogger logger = MinecraftServer.LOGGER;

    private static final VanillaBlockBehaviour BLOCK_BEHAVIOUR = VanillaBlockBehaviour.INSTANCE;

    public static void registerDefault() {
        register(VanillaBlockBehaviour.CRAFTING_TABLE);
        register(VanillaBlockBehaviour.ANVIL);
        register(VanillaBlockBehaviour.BREWING_STAND);
        register(VanillaBlockBehaviour.LOOM);
        register(VanillaBlockBehaviour.GRINDSTONE);
        register(VanillaBlockBehaviour.SMITHING_TABLE);
        register(VanillaBlockBehaviour.CARTOGRAPHY_TABLE);
        register(VanillaBlockBehaviour.STONECUTTER);
        register(VanillaBlockBehaviour.ENCHANTING_TABLE);
        register(VanillaBlockBehaviour.TRAPDOOR);
        register(VanillaBlockBehaviour.FENCE_GATE);
        register(VanillaBlockBehaviour.COPPER);
        register(VanillaBlockBehaviour.WOODEN_DOORS);
        register(VanillaBlockBehaviour.SIGNS);
        register(VanillaBlockBehaviour.CAKE);
        register(VanillaBlockBehaviour.CANDLE_CAKE);
        register(VanillaBlockBehaviour.STRIPPABLE_WOOD);
    }

    public static void register(BehaviourGroup... blockGroups) {
        var blockManager = MinecraftServer.getBlockManager();
        int count = 0;

        for (BehaviourGroup group : blockGroups) {
            var blockGroup = group.getBlockGroup();
            for (var block : blockGroup.allMatching()) {
                count++;
                var handler = group.createHandler(block);
                blockManager.registerHandler(block.key().asString(), () -> handler);
            }
        }

      logger.info("Registered {} handlers", count);
    }
}
