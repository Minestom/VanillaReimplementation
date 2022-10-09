package net.minestom.vanilla.server;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.entitymeta.EntityTags;

import java.util.Random;

/**
 * This debug server can be edited and committed to master without any consequences.
 * Make sure to keep anything cool you make! A quick comment is always a good idea.
 * <p>
 * And try not to remove anyone else's additions, just comment them out.
 */
public class VanillaDebug {
    public static void hook(VanillaServer server) {
        VanillaReimplementation vri = server.vri();
        vri.process().eventHandler()
                .addListener(PlayerChatEvent.class, event -> handleMessage(server, event.getPlayer(), event.getMessage()))
//            .addListener(PlayerTickEvent.class, event -> {
//                if (event.getPlayer().getInstance().getWorldAge() % 10 == 0) {
//                    handleMessage(server, event.getPlayer(), "fallingblock");
//                }
//            })
        ;
    }

    private static void handleMessage(VanillaServer server, Player player, String message) {
        VanillaReimplementation vri = server.vri();
        Block[] blocks = Block.values().toArray(new Block[0]);
        Random random = new Random();
        switch (message) {
            case "tnt" -> {
                Pos pos = player.getPosition();
                VanillaRegistry.EntityContext context = vri.entityContext(EntityType.TNT, pos);
                Entity entity = vri.createEntityOrDummy(context);
                entity.setInstance(server.overworld(), pos);
            }
            case "fallingblock" -> {
                Pos pos = player.getPosition();
                VanillaRegistry.EntityContext context = vri.entityContext(EntityType.FALLING_BLOCK, pos,
                        tags -> tags.setTag(EntityTags.FallingBlock.BLOCK, blocks[random.nextInt(blocks.length)]));
                Entity entity = vri.createEntityOrDummy(context);
                entity.setInstance(server.overworld(), pos);
            }
        }
    }
}
