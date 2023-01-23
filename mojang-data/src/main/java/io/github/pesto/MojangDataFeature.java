package io.github.pesto;

import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaRegistry;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MojangDataFeature implements VanillaReimplementation.Feature {
    private static final File ROOT = new File(".", "mojang-data");
    private static final String VERSION = "1.19.2";

    private final MojangAssets assets;

    public MojangDataFeature() {
        this.assets = new MojangAssets();
    }

    @Override
    public void hook(@NotNull VanillaReimplementation vri, @NotNull VanillaRegistry registry) {
        assets.downloadResources(VERSION, ROOT);
    }

    @Override
    public @NotNull NamespaceID namespaceID() {
        return NamespaceID.from("io.github.pesto:mojang_data");
    }

    public File getAssetsDirectory() {
        return assets.getDataDirectory();
    }

    public boolean isCompleted() {
        return assets.isCompleted();
    }
}