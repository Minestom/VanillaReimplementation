package io.github.pesto;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

public class MojangDataFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {

    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("io.github.pesto:mojang_data");
    }
}