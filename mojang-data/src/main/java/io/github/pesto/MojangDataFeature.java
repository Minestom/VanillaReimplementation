package io.github.pesto;

import net.minestom.vanilla.files.ByteArray;
import net.minestom.vanilla.files.FileSystem;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class MojangDataFeature implements VanillaReimplementation.Feature {

    private static final String LATEST = "1.20.1";
    private final MojangAssets assets = new MojangAssets();

    @Override
    public void hook(@NotNull HookContext context) {
        assets.getAssets(LATEST).join();
    }

    @Override
    public @NotNull NamespaceID namespaceId() {
        return NamespaceID.from("io.github.pesto:mojang_data");
    }

    public CompletableFuture<FileSystem<ByteArray>> getAssets(@NotNull String version) {
        return assets.getAssets(version);
    }

    public CompletableFuture<FileSystem<ByteArray>> getLatest() {
        return assets.getAssets(LATEST);
    }
}