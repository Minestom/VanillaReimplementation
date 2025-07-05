package io.github.pesto;

import net.kyori.adventure.key.Key;
import net.minestom.vanilla.VanillaReimplementation;
import net.minestom.vanilla.files.ByteArray;
import net.minestom.vanilla.files.FileSystem;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class MojangDataFeature implements VanillaReimplementation.Feature {

    private static final String LATEST = "1.21.5";
    private final MojangAssets assets = new MojangAssets();
    private final CompletableFuture<FileSystem<ByteArray>> latest = new CompletableFuture<>();

    @Override
    public void hook(@NotNull HookContext context) {
        assetsRequest(LATEST)
                .thenAccept(latest::complete)
                .join();
    }

    @Override
    public @NotNull Key key() {
        return Key.key("io_github_pesto:mojang_data");
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
