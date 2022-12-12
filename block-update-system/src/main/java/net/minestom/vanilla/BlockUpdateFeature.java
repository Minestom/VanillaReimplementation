package net.minestom.vanilla;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.blockupdatesystem.BlockUpdateManager;
import net.minestom.vanilla.randomticksystem.RandomTickManager;
import org.jetbrains.annotations.NotNull;

public class BlockUpdateFeature implements VanillaReimplementation.Feature {
    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {
        BlockUpdateManager.init(vri.process().eventHandler());
        RandomTickManager.init(vri);
    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("vri:blockupdate");
    }
}
