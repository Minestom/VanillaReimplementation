package net.minestom.vanilla;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.blockupdatesystem.BlockUpdateManager;
import net.minestom.vanilla.logging.Loading;
import net.minestom.vanilla.randomticksystem.RandomTickManager;
import org.jetbrains.annotations.NotNull;

public class BlockUpdateFeature implements VanillaReimplementation.Feature {
    @Override
    public void hook(@NotNull HookContext context) {
        Loading.start("Block Update Manager");
        BlockUpdateManager.init(context);
        Loading.finish();

        Loading.start("Random Tick Manager");
        RandomTickManager.init(context);
        Loading.finish();
    }

    @Override
    public @NotNull NamespaceID namespaceId() {
        return NamespaceID.from("vri:blockupdate");
    }
}
