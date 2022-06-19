package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class VanillaBlocksFeature implements VanillaReimplementation.Feature {
    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {
        VanillaBlocks.registerAll(vri);

        vri.process().eventHandler().addListener(PlayerBlockPlaceEvent.class, event -> {
            Block block = event.getBlock();
            Instance instance = event.getInstance();
            Point position = event.getBlockPosition();
            AtomicReference<Block> blockToPlace = new AtomicReference<>(block);

            if (block.handler() instanceof VanillaBlockHandler vanillaHandler) {
                // Create the new placement object
                VanillaBlockHandler.VanillaPlacement placement = new VanillaBlockHandler.VanillaPlacement() {
                    @Override public @NotNull Block blockToPlace() {
                        return blockToPlace.get();
                    }
                    @Override public @NotNull Instance instance() {
                        return instance;
                    }
                    @Override public @NotNull Point position() {
                        return position;
                    }
                    @Override public @NotNull Block blockToPlace(@NotNull Block newBlock) {
                        return blockToPlace.getAndSet(newBlock);
                    }
                };

                vanillaHandler.onPlace(placement);
            }

            event.setBlock(blockToPlace.get());
        });
    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("vri:vanilla-blocks");
    }
}
