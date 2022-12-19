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
        VanillaBlocks.registerAll(vri, registry);

        vri.process().eventHandler().addListener(PlayerBlockPlaceEvent.class, event -> {
            Block block = event.getBlock();
            Instance instance = event.getInstance();
            Point position = event.getBlockPosition();
            AtomicReference<Block> blockToPlace = new AtomicReference<>(block);

            if (block.handler() instanceof VanillaBlockBehaviour vanillaHandler) {
                // Create the new placement object
                VanillaBlockBehaviour.VanillaPlacement placement = new VanillaBlockBehaviour.VanillaPlacement() {
                    @Override
                    public @NotNull Block blockToPlace() {
                        return blockToPlace.get();
                    }

                    @Override
                    public @NotNull Instance instance() {
                        return instance;
                    }

                    @Override
                    public @NotNull Point position() {
                        return position;
                    }

                    @Override
                    public void blockToPlace(@NotNull Block newBlock) {
                        blockToPlace.getAndSet(newBlock);
                        // TODO: Run vanillaHandler.onPlace again on the new block if it's a vanilla block
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
