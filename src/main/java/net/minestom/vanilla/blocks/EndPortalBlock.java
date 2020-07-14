package net.minestom.vanilla.blocks;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.world.DimensionType;

import java.util.Optional;

public class EndPortalBlock extends VanillaBlock {
    public EndPortalBlock() {
        super(Block.END_PORTAL);
    }

    @Override
    protected BlockPropertyList createPropertyValues() {
        return new BlockPropertyList();
    }

    @Override
    public void handleContact(Instance instance, BlockPosition position, Entity touching) {
        DimensionType targetDimension = instance.getDimensionType() == DimensionType.END ? DimensionType.OVERWORLD : DimensionType.END;
        Optional<Instance> potentialTargetInstance = MinecraftServer.getInstanceManager().getInstances().stream()
                .filter(in -> in.getDimensionType() == targetDimension)
                .findFirst();

        // TODO: event
        if(potentialTargetInstance.isPresent()) {
            Instance targetInstance = potentialTargetInstance.get();
            Position spawnPoint;
            final int obsidianPlatformX = 100;
            final int obsidianPlatformY = 48;
            final int obsidianPlatformZ = 0;
            if(targetDimension == DimensionType.OVERWORLD) { // teleport to spawn point
                if(touching instanceof Player) {
                    spawnPoint = ((Player) touching).getRespawnPoint();
                } else { // TODO: world spawnpoint
                    spawnPoint = new Position(0, 80, 0);
                }
            } else {
                // teleport to the obsidian platform, and recreate it if necessary
                int yLevel = touching instanceof Player ? 49 : 50;
                spawnPoint = new Position(obsidianPlatformX, yLevel, obsidianPlatformZ);
            }
            if(targetDimension == DimensionType.END) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        targetInstance.loadChunk(obsidianPlatformX/16+x, obsidianPlatformZ/16+z);
                    }
                }

                // clear 5x3x5 area around platform
                for (int x = 0; x < 5; x++) {
                    for (int z = 0; z < 5; z++) {
                        for (int y = 0; y < 3; y++) {
                            targetInstance.setSeparateBlocks(obsidianPlatformX+x, obsidianPlatformY+y+1, obsidianPlatformZ+z, Block.AIR.getBlockId(), (short)0);
                        }
                    }
                }
            }
            touching.setInstance(targetInstance);
            touching.teleport(spawnPoint);
        }
    }
}
