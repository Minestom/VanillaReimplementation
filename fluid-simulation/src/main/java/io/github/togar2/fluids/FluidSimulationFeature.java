package io.github.togar2.fluids;


import net.kyori.adventure.key.Key;
import net.minestom.vanilla.BlockUpdateFeature;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class FluidSimulationFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull HookContext context) {
        // TODO: Use the block-update-system
        MinestomFluids.init(context.vri().process());
    }

    @Override
    public @NotNull Key key() {
        return Key.key("io.github.togar2:fluids");
    }

    @NotNull
    public Set<Class<? extends VanillaReimplementation.Feature>> dependencies() {
        return Set.of(BlockUpdateFeature.class);
    }
}
