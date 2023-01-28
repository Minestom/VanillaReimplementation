package net.minestom.vanilla.entities;

import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

public class MinestomEntitiesFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull HookContext context) {
        context.registry().register(EntityType.FALLING_BLOCK, FallingBlockEntity::new);
        context.registry().register(EntityType.TNT, PrimedTNTEntity::new);
    }

    @Override
    public @NotNull NamespaceID namespaceId() {
        return NamespaceID.from("vri:entities");
    }
}
