package net.minestom.vanilla.blocks;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

public class VanillaBlocksFeature implements VanillaReimplementation.Feature {
    @Override
    public void hook(@NotNull VanillaReimplementation vri) {
        VanillaBlocks.registerAll(vri.process().eventHandler());
    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("vri:vanilla-blocks");
    }
}
