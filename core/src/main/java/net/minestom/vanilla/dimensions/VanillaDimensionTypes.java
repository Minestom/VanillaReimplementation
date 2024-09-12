package net.minestom.vanilla.dimensions;

import net.minestom.server.MinecraftServer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;

import java.util.List;
import java.util.Map;

public class VanillaDimensionTypes {

    public static final DimensionType OVERWORLD = DimensionType.builder()
            .build();

    public static Map<DimensionType, NamespaceID> values() {
        return Map.of(
                OVERWORLD, NamespaceID.from("vri:overworld")
        );
    }

    public static void registerAll(DynamicRegistry<DimensionType> registry) {
        values().forEach((dimensionType, namespaceID) -> {
            registry.register(namespaceID, dimensionType);
        });
    }
}
