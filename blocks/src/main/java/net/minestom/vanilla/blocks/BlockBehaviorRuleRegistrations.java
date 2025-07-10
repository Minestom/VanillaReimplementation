package net.minestom.vanilla.blocks;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.vanilla.blocks.group.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.group.behaviour.BehaviourGroup;

public class BlockBehaviorRuleRegistrations {
    private static final ComponentLogger logger = MinecraftServer.LOGGER;

    private static final VanillaBlockBehaviour BLOCK_BEHAVIOUR = VanillaBlockBehaviour.INSTANCE;

    public static void registerDefault() {
        register(BLOCK_BEHAVIOUR.ALL.toArray(new BehaviourGroup[0]));
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
