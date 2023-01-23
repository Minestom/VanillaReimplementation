package io.github.pesto;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class MojangDataFeature implements VanillaReimplementation.Feature {
    public static final File root = new File(".", "cache");

    public static void main(String[] args) throws IOException {
        MojangAssets.downloadResources("latest", root);
    }

    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {

    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("io.github.pesto:mojang_data");
    }
}