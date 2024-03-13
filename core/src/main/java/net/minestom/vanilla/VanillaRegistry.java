package net.minestom.vanilla;

import net.minestom.vanilla.registry.entity.VanillaEntityRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * This registry object is used to register data and logic.
 */
public sealed interface VanillaRegistry permits VanillaReimplementationImpl.VanillaRegistryImpl {
  @NotNull VanillaEntityRegistry entityRegistry();
}
