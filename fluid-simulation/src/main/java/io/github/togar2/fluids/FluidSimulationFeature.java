package io.github.togar2.fluids;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

public class FluidSimulationFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {
        // TODO: Use the block-update-system
        MinestomFluids.init(vri.process());
    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("io-github-togar2:fluids");
    }
}
