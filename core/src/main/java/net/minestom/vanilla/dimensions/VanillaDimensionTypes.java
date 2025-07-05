package net.minestom.vanilla.dimensions;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.DimensionType;

import java.util.Map;

public class VanillaDimensionTypes {

    public static final DimensionType OVERWORLD = DimensionType.builder()
            .build();

    public static Map<DimensionType, Key> values() {
        return Map.of(
                OVERWORLD, Key.key("vri:overworld")
        );
    }

    public static void registerAll(DynamicRegistry<DimensionType> registry) {
        values().forEach((dimensionType, namespaceID) -> {
            registry.register(namespaceID, dimensionType);
        });
    }
}
