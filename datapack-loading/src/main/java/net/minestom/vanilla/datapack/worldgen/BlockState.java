package net.minestom.vanilla.datapack.worldgen;

import net.minestom.server.instance.block.Block;

import java.util.Map;
import java.util.Objects;

public record BlockState(String name, Map<String, String> properties) {
    public Block toMinestom() {
        return Objects.requireNonNull(Block.fromNamespaceId(name), () -> "Unknown block: " + name)
                .withProperties(properties);
    }
}
