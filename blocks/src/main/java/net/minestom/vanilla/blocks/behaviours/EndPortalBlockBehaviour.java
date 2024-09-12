package net.minestom.vanilla.blocks.behaviours;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import net.minestom.vanilla.blocks.VanillaBlockBehaviour;
import net.minestom.vanilla.blocks.VanillaBlocks;
import net.minestom.vanilla.dimensions.VanillaDimensionTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EndPortalBlockBehaviour extends VanillaBlockBehaviour {
    public EndPortalBlockBehaviour(@NotNull VanillaBlocks.BlockContext context) {
        super(context);
    }

//    @Override
//    protected BlockPropertyList createPropertyValues() {
//        return new BlockPropertyList();
//    }

    @Override
    public void onTouch(@NotNull Touch touch) {
        Instance instance = touch.getInstance();
        Entity touching = touch.getTouching();
        var key = instance.getDimensionType();
        DimensionType dimension = MinecraftServer.getDimensionTypeRegistry().get(key);

        DimensionType targetDimension = VanillaDimensionTypes.OVERWORLD;
        Optional<Instance> potentialTargetInstance = MinecraftServer.getInstanceManager().getInstances().stream()
                .filter(in -> {
                    var key1 = in.getDimensionType();
                    return MinecraftServer.getDimensionTypeRegistry().get(key1) == targetDimension;
                })
                .findFirst();

        // TODO: event
        if (potentialTargetInstance.isPresent()) {
            Instance targetInstance = potentialTargetInstance.get();
            Pos spawnPoint;
            final int obsidianPlatformX = 100;
            final int obsidianPlatformY = 48;
            final int obsidianPlatformZ = 0;

            if (targetDimension == VanillaDimensionTypes.OVERWORLD) { // teleport to spawn point
                if (touching instanceof Player) {
                    spawnPoint = ((Player) touching).getRespawnPoint();
                } else { // TODO: world spawnpoint
                    spawnPoint = new Pos(0, 80, 0);
                }
            } else {
                // teleport to the obsidian platform, and recreate it if necessary
                int yLevel = touching instanceof Player ? 49 : 50;
                spawnPoint = new Pos(obsidianPlatformX, yLevel, obsidianPlatformZ);
            }

            if (targetDimension.effects().equals("the_end")) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        targetInstance.loadChunk(obsidianPlatformX / 16 + x, obsidianPlatformZ / 16 + z);
                    }
                }

                // clear 5x3x5 area around platform
                for (int x = 0; x < 5; x++) {
                    for (int z = 0; z < 5; z++) {
                        for (int y = 0; y < 3; y++) {
                            targetInstance.setBlock(obsidianPlatformX + x, obsidianPlatformY + y + 1, obsidianPlatformZ + z, Block.AIR);
                        }
                    }
                }
            }
            touching.setInstance(targetInstance);
            touching.teleport(spawnPoint);
        }
    }
}
