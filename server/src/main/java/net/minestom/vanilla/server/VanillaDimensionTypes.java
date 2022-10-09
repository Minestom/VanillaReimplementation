package net.minestom.vanilla.server;

import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;

import java.util.Collection;
import java.util.List;

public class VanillaDimensionTypes {

    public static final DimensionType OVERWORLD = DimensionType.builder(NamespaceID.from("minecraft:overworld"))
            .ultrawarm(false)
            .natural(true)
            .piglinSafe(false)
            .respawnAnchorSafe(false)
            .bedSafe(true)
            .raidCapable(true)
            .skylightEnabled(true)
            .ceilingEnabled(false)
            .fixedTime(null)
            .ambientLight(0.0f)
            .logicalHeight(256)
            .infiniburn(NamespaceID.from("minecraft:infiniburn_overworld"))
            .build();

    public static final DimensionType NETHER = DimensionType.builder(NamespaceID.from("minecraft:the_nether"))
            .ultrawarm(true)
            .respawnAnchorSafe(true)
            .piglinSafe(true)
            .natural(false)
            .logicalHeight(128)
            .raidCapable(false)
            .skylightEnabled(false)
            .ceilingEnabled(true)
            .fixedTime(18000L)
            .ambientLight(0.1f)
            .logicalHeight(256)
            .coordinateScale(8)
            .effects("the_nether")
            .infiniburn(NamespaceID.from("minecraft:infiniburn_nether"))
            .build();

    public static final DimensionType END = DimensionType.builder(NamespaceID.from("minecraft:the_end"))
            .ultrawarm(false)
            .respawnAnchorSafe(false)
            .piglinSafe(false)
            .natural(false)
            .logicalHeight(256)
            .raidCapable(true)
            .skylightEnabled(false)
            .ceilingEnabled(false)
            .fixedTime(6000L)
            .ambientLight(0.0f)
            .logicalHeight(256)
            .coordinateScale(1)
            .effects("the_end")
            .infiniburn(NamespaceID.from("minecraft:infiniburn_end"))
            .build();

    public static Collection<DimensionType> values() {
        return List.of(OVERWORLD, NETHER, END);
    }
}
