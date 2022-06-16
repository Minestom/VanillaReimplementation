package net.minestom.vanilla.commands;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

public class VanillaCommandsFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {
        new Logic().hook(vri);
    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("vri:commands");
    }

    private static class Logic {
        private Logic() {
        }

        private void hook(@NotNull VanillaReimplementation vri) {
            VanillaCommands.registerAll(vri.process().command());
        }
    }
}
