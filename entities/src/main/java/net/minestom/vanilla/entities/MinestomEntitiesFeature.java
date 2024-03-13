package net.minestom.vanilla.entities;

import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.registry.entity.VanillaEntityRegistry;
import org.jetbrains.annotations.NotNull;

public class MinestomEntitiesFeature implements VanillaReimplementation.Feature {

    @Override
    public void hook(@NotNull HookContext context) {
        VanillaEntityRegistry registry = context.registry().entityRegistry();

        registry.bindEntitySpawner(EntityType.FALLING_BLOCK, FallingBlockEntity::new);
        registry.bindEntitySpawner(EntityType.TNT, PrimedTNTEntity::new);
    }

    @Override
    public @NotNull NamespaceID namespaceId() {
        return NamespaceID.from("vri:entities");
    }
}
