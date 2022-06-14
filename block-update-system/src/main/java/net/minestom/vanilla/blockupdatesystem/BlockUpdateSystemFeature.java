package net.minestom.vanilla.blockupdatesystem;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

public class BlockUpdateSystemFeature implements VanillaReimplementation.Feature {
    @Override
    public void hook(@NotNull VanillaReimplementation vri) {
        BlockUpdateManager.init(vri.process().eventHandler());
    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("vri:blockupdatesystem");
    }
}
