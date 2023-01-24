package io.github.pesto;

import io.github.pesto.files.FileSystem;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MojangDataFeature implements VanillaReimplementation.Feature {

    private static final File ROOT = new File(".", "mojang-data");
    private static final String VERSION = "1.19.2";

    private final MojangAssets assets = new MojangAssets();

    public void initialize() {
        assets.downloadResources(VERSION, ROOT);
    }

    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {
        initialize();
    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("io.github.pesto:mojang_data");
    }

    public FileSystem<byte[]> getAssetsDirectory() {
        return assets.getFileSystem();
    }
}