package net.minestom.vanilla.commands;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.instancemeta.InstanceMetaFeature;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class VanillaCommandsFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull HookContext context) {
        new Logic().hook(context.vri());
    }

    @Override
    public @NotNull NamespaceID namespaceId() {
        return NamespaceID.from("vri:commands");
    }

    private static class Logic {
        private Logic() {
        }

        private void hook(@NotNull VanillaReimplementation vri) {
            VanillaCommands.registerAll(vri.process().command());
        }
    }

    @Override
    public @NotNull Set<Class<? extends VanillaReimplementation.Feature>> dependencies() {
        return Set.of(InstanceMetaFeature.class);
    }
}
