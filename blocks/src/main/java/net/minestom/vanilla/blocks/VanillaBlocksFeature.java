package net.minestom.vanilla.blocks;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.BlockUpdateFeature;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.datapack.DatapackLoadingFeature;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class VanillaBlocksFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull HookContext context) {
        VanillaReimplementation vri = context.vri();
        VanillaBlocks.registerAll(vri);

        vri.process().eventHandler().addListener(PlayerBlockPlaceEvent.class, event -> {
            Block block = event.getBlock();
            Instance instance = event.getInstance();
            Point position = event.getBlockPosition();
            AtomicReference<Block> blockToPlace = new AtomicReference<>(block);

            if (block.handler() instanceof VanillaBlockBehaviour vanillaHandler) {
                // Create the new placement object
                VanillaBlockBehaviour.VanillaPlacement placement = new PlacementImpl(blockToPlace, instance, position);
                vanillaHandler.onPlace(placement);
            }

            event.setBlock(blockToPlace.get());
        });
    }

    private record PlacementImpl(AtomicReference<Block> blockToPlaceRef, Instance instance, Point position) implements VanillaBlockBehaviour.VanillaPlacement {
        @Override
        public @NotNull Block blockToPlace() {
            return blockToPlaceRef.get();
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
            blockToPlaceRef.getAndSet(newBlock);
            // TODO: Run vanillaHandler.onPlace again on the new block if it's a vanilla block
        }
    }

    @Override
    public @NotNull NamespaceID namespaceId() {
        return NamespaceID.from("vri:blocks");
    }

    @Override
    public @NotNull Set<Class<? extends VanillaReimplementation.Feature>> dependencies() {
        return Set.of(BlockUpdateFeature.class, DatapackLoadingFeature.class);
    }
}
