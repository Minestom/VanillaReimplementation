package net.minestom.vanilla.datapack;

import com.google.gson.JsonParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.vanilla.logging.Loading;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Utilities for datapack loading. Primary functions are {@link Datapacks#ensureCurrentJarExists()} and {@link
 * Datapacks#forEachFileInJar(Path, Path)}
 */
@SuppressWarnings("UnstableApiUsage")
public class Datapacks {

    public static final @NotNull URI VERSION_MANIFEST_URI = URI.create("https://launchermeta.mojang.com/mc/game/version_manifest.json");

    public static final @NotNull Path MOJANG_DATA_DIRECTORY = Path.of(".", "mojang-data");

    /**
     * Version manfest data, from {@link Datapacks#VERSION_MANIFEST_URI}.
     */
    public record VersionManifest(@NotNull Latest latest, @NotNull List<Version> versions) {
        public static final @NotNull StructCodec<VersionManifest> CODEC = StructCodec.struct(
                "latest", Latest.CODEC, VersionManifest::latest,
                "versions", Version.CODEC.list(), VersionManifest::versions,
                VersionManifest::new
        );

        public record Latest(@NotNull String release, @NotNull String snapshot) {
            public static final @NotNull StructCodec<Latest> CODEC = StructCodec.struct(
                    "release", Codec.STRING, Latest::release,
                    "snapshot", Codec.STRING, Latest::snapshot,
                    Latest::new
            );
        }

        public record Version(@NotNull String id, @NotNull Type type, @NotNull String url, @NotNull String time, @NotNull String releaseTime) {
            public static final @NotNull StructCodec<Version> CODEC = StructCodec.struct(
                "id", Codec.STRING, Version::id,
                    "type", Type.CODEC, Version::type,
                    "url", Codec.STRING, Version::url,
                    "time", Codec.STRING, Version::url,
                    "releaseTime", Codec.STRING, Version::url,
                    Version::new
            );

            public enum Type {
                RELEASE, SNAPSHOT, OLD_BETA, OLD_ALPHA;

                public static final @NotNull Codec<Type> CODEC = Codec.Enum(Type.class);
            }
        }
    }

    /**
     * Version metadata. There's more information on the codec than this, but most of it is not useful here, so it is
     * not included.
     */
    public record VersionMetadata(@NotNull Downloads downloads) {
        public static final @NotNull StructCodec<VersionMetadata> CODEC = StructCodec.struct(
                "downloads", Downloads.CODEC, VersionMetadata::downloads,
                VersionMetadata::new
        );

        public record Downloads(@NotNull Download client, @Nullable Download clientMappings,
                                @NotNull Download server, @Nullable Download serverMappings) {
            public static final @NotNull StructCodec<Downloads> CODEC = StructCodec.struct(
                    "client", Download.CODEC, Downloads::client,
                    "client_mappings", Download.CODEC.optional(), Downloads::clientMappings,
                    "server", Download.CODEC, Downloads::server,
                    "server_mappings", Download.CODEC.optional(), Downloads::serverMappings,
                    Downloads::new
            );
        }

        public record Download(@NotNull String sha1, int size, @NotNull String url) {
            public static final @NotNull StructCodec<Download> CODEC = StructCodec.struct(
                    "sha1", Codec.STRING, Download::sha1,
                    "size", Codec.INT, Download::size,
                    "url", Codec.STRING, Download::url,
                    Download::new
            );
        }

    }

    /**
     * Extracts various metadata (including JAR URLs) about the specified version. Use "latest" for the latest release.
     */
    public static @NotNull URL getVersionMetadataURL(@NotNull String version) throws IOException {
        URL discoveryUrl = VERSION_MANIFEST_URI.toURL();

        VersionManifest manifest;

        try (InputStream source = discoveryUrl.openStream();
             InputStreamReader reader = new InputStreamReader(source)) {
            Result<VersionManifest> result = VersionManifest.CODEC.decode(Transcoder.JSON, JsonParser.parseReader(reader));
            manifest = result.orElseThrow("failed to parse version manifest at url " + VERSION_MANIFEST_URI);
        }

        if (version.equals("latest")) {
            version = manifest.latest().release();
        }

        for (VersionManifest.Version versionEntry : manifest.versions()) {
            if (versionEntry.id().equals(version)) {
                return URI.create(versionEntry.url()).toURL();
            }
        }

        throw new IllegalArgumentException("Version '" + version + "' not found!");
    }

    /**
     * Extracts the client JAR url from the version metadata.
     */
    public static @NotNull URL getClientJarURL(@NotNull URL versionMetadata) throws IOException {
        VersionMetadata metadata;

        try (InputStream source = versionMetadata.openStream();
             InputStreamReader reader = new InputStreamReader(source)) {
            Result<VersionMetadata> result = VersionMetadata.CODEC.decode(Transcoder.JSON, JsonParser.parseReader(reader));
            metadata = result.orElseThrow("failed to parse version metadata at url " + versionMetadata);
        }

        String url = metadata.downloads().client().url();
        return URI.create(url).toURL();
    }

    /**
     * Downloads the vanilla JAR from a specified source URL to the given sink file. Other than log messages, this is
     * entirely agnostic of the actual file content and simply performs a copy while logging.
     */
    public static void downloadJar(@NotNull URL sourceUrl, @NotNull Path sinkFile) throws IOException {
        URLConnection sourceConnection = sourceUrl.openConnection();
        sourceConnection.connect();

        final int parts = 8;

        int len = sourceConnection.getContentLength();

        double totalMB = (double) len / 1024 / 1024;
        Loading.start(String.format("Downloading vanilla jar (%.2f MB)...", totalMB));

        byte[] buf = new byte[4096];

        // Ensure necessary parent directories for the JAR exist
        Files.createDirectories(sinkFile.getParent());

        try (InputStream source = sourceConnection.getInputStream();
             OutputStream sink = Files.newOutputStream(sinkFile)) {

            for (int cur = 0, read; (read = source.read(buf)) > 0; cur += read) {
                sink.write(buf, 0, read);

                // If it passed a boundary (wraps around mod len/parts after reading), display progress
                if ((cur + read) % (len/parts) < cur % (len/parts)) {
                    double progress = cur / (double) len;
                    Loading.updater().progress(Math.round(progress * parts) / (double) parts);
                }
            }
        }

        Loading.finish();
    }

    /**
     * Downloads the client JAR for the specified version and places it in {@code client-VERSION.jar} within the
     * specified directory. No-op of the client JAR has been downloaded already.
     * @return the client JAR path
     */
    public static @NotNull Path discoverAndDownloadJar(@NotNull String version, @NotNull Path sinkDirectory) throws IOException {
        Path sinkFile = sinkDirectory.resolve("client-" + version + ".jar");
        if (Files.exists(sinkFile)) return sinkFile; // Cached

        URL jarUrl = Datapacks.getClientJarURL(Datapacks.getVersionMetadataURL(version));

        Datapacks.downloadJar(jarUrl, sinkFile);
        return sinkFile;
    }

    /**
     * Ensures that the client JAR exists and is downloaded, returning its path.
     */
    public static @NotNull Path ensureCurrentJarExists() throws IOException {
        return Datapacks.discoverAndDownloadJar(MinecraftServer.VERSION_NAME, MOJANG_DATA_DIRECTORY);
    }

    /**
     * Iterates through every file in the given {@code pathFilter} that exists within the provided JAR.
     */
    public static @NotNull Stream<Path> forEachFileInJar(@NotNull Path jarPath, @NotNull Path pathFilter) throws IOException {
        try (FileSystem fileSystem = FileSystems.newFileSystem(jarPath)) {
            for (Path root : fileSystem.getRootDirectories()) {
                Path relevantFiles = root.resolve(pathFilter.toString()); // Prevent provider mismatch

                return Files.walk(relevantFiles);
            }
        }

        throw new IOException("Could not iterate through JAR files!");
    }

    public static <T> @NotNull Map<Key, T> buildRegistryFromJar(@NotNull Path jarPath, @NotNull Path pathFilter, @NotNull ServerProcess process, @NotNull String fileSuffix, @NotNull Codec<T> codec) throws IOException {
        final Map<Key, T> map = new HashMap<>();
        final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, process);

        try (FileSystem fileSystem = FileSystems.newFileSystem(jarPath)) {
            for (Path root : fileSystem.getRootDirectories()) {
                Path relevantFiles = root.resolve(pathFilter.toString()); // Prevent provider mismatch

                List<Path> files = Files.walk(relevantFiles).toList();
                for (Path path : files) {
                    if (!Files.isRegularFile(path)) continue;
                    if (!path.toString().endsWith(".json")) continue;

                    String keyPath = pathFilter.relativize(Path.of(path.toString())).toString();
                    keyPath = keyPath.substring(0, keyPath.length() - fileSuffix.length());

                    BinaryTag tag;
                    try {
                        tag = MinestomAdventure.tagStringIO().asTag(Files.readString(path));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    map.put(
                            Key.key(keyPath),
                            codec.decode(coder, tag).orElseThrow("parsing " + path)
                    );
                }
            }
        }

        return map;
    }

}