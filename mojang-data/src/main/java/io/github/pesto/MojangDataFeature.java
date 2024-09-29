package io.github.pesto;

import net.minestom.vanilla.files.ByteArray;
import net.minestom.vanilla.files.FileSystem;
import net.minestom.server.utils.NamespaceID;
import net.minestom.vanilla.VanillaReimplementation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class MojangDataFeature implements VanillaReimplementation.Feature {

    private static final String LATEST = "1.21.1";
    private final MojangAssets assets = new MojangAssets();
    private final CompletableFuture<FileSystem<ByteArray>> latest = new CompletableFuture<>();

    @Override
    public void hook(@NotNull HookContext context) {
        assetsRequest(LATEST)
                .thenAccept(latest::complete)
                .join();
    }

    @Override
    public @NotNull NamespaceID namespaceId() {
        return NamespaceID.from("io_github_pesto:mojang_data");
    }

    public FileSystem<ByteArray> latestAssets() {
        if (!latest.isDone()) {
            throw new IllegalStateException("Cannot request assets before {@link MojangDataFeature} is loaded");
        }
        return latest.join();
    }

    public CompletableFuture<FileSystem<ByteArray>> assetsRequest(@NotNull String version) {
        return assets.getAssets(version);
    }
}
