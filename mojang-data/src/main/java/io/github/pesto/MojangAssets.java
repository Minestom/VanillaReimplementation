package io.github.pesto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.pesto.files.ByteArray;
import io.github.pesto.files.FileSystem;
import net.minestom.vanilla.logging.Loading;
import net.minestom.vanilla.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.StandardOpenOption.*;

public final class MojangAssets {
    private static final File ROOT = new File(".", "mojang-data");
    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private final Map<String, CompletableFuture<FileSystem<ByteArray>>> versions = new ConcurrentHashMap<>();

    public CompletableFuture<FileSystem<ByteArray>> getAssets(@NotNull String version) {
        versions.putIfAbsent(version, CompletableFuture.supplyAsync(() -> downloadResources(version)));
        return versions.get(version);
    }

    private FileSystem<ByteArray> downloadResources(@NotNull String version) {
        try {
            Loading.start("Downloading vanilla jar...");
            // Check if source files already exist
            File jar = new File(ROOT, version + File.separator + "resources.jar");
            if (!jar.exists()) {

                // Get version info
                String versionInfoUrl = findVersionInfoUrl(version);
                JsonObject versionInfo = downloadJson(versionInfoUrl);

                // Download jar
                downloadJar(versionInfo, jar);
            }

            return FileSystem.fromZipFile(jar, path -> path.startsWith("data/minecraft/"))
                    .folder("data", "minecraft");

        } catch (IOException e) {
            exitError(e.getMessage());
        } finally {
            Loading.finish();
        }
        return FileSystem.empty();
    }

    /**
     * Gets the version info from the version manifest
     *
     * @param version The release version or latest version if requested
     * @return The url to the version info
     * @throws IOException If the version info url could not be found
     */
    private String findVersionInfoUrl(@NotNull String version) throws IOException {
        // Get manifest
        JsonObject manifest = downloadJson(VERSION_MANIFEST_URL);

        // Get the latest version if requested
        if (version.equals("latest")) {
            JsonObject latest = manifest.getAsJsonObject("latest");
            version = latest.get("release").getAsString();
        }

        // Find the url for the version's info
        for (JsonElement elem : manifest.getAsJsonArray("versions")) {
            JsonObject info = elem.getAsJsonObject();
            String id = info.get("id").getAsString();

            if (!id.equals(version))
                continue;

            return info.get("url").getAsString();
        }

        throw new IOException("Failed to find version info url for version " + version);
    }

    /**
     * Downloads the vanilla jar to be used for extracting
     *
     * @param versionInfo The version info
     */
    private void downloadJar(JsonObject versionInfo, @NotNull File destination) throws IOException {
        JsonObject downloads = versionInfo.getAsJsonObject("downloads");
        JsonObject client = downloads.getAsJsonObject("client");
        String url = client.get("url").getAsString();

        // Create if it doesn't exist
        if (!destination.exists()) {
            destination.getParentFile().mkdirs();
            destination.createNewFile();
        }

        try (
                ReadableByteChannel in = Channels.newChannel(new URL(url).openStream());
                FileChannel channel = FileChannel.open(destination.toPath(), CREATE, WRITE, TRUNCATE_EXISTING)
        ) {
            channel.transferFrom(in, 0, Long.MAX_VALUE);

            boolean success = destination.exists() && destination.length() > 0;
            if (!success)
                throw new IOException("Failed to download client JAR");
        }
    }

//    private boolean extractJarAssets(@NotNull File jarFile, File root) {
//        File output = new File(root, jarFile.getParentFile().getName());
//
//        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile))) {
//            ZipEntry entry;
//            while ((entry = zip.getNextEntry()) != null) {
//
//                // Skip unrelated entries
//                if (!entry.getName().startsWith("data"))
//                    continue;
//
//                // Validate that the file exists
//                File file = checkAndCreateFile(output, entry);
//                if (file.exists()) continue;
//
//                if (entry.isDirectory()) {
//                    if (!file.isDirectory() && !file.mkdirs())
//                        throw new IOException("Failed to create directory " + file);
//                } else {
//                    File parent = file.getParentFile();
//                    if (!parent.isDirectory() && !parent.mkdirs())
//                        throw new IOException("Failed to create directory " + parent);
//
//                    Files.copy(zip, file.toPath());
//                }
//            }
//            zip.closeEntry();
//            jarFile.delete();
//
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    private JsonObject downloadJson(String url) throws IOException {
        return JsonParser.parseReader(new InputStreamReader(getDownloadStream(url))).getAsJsonObject();
    }

    private InputStream getDownloadStream(String url) throws IOException {
        return new URL(url).openStream();
    }

    private void exitError(String message) {
        System.err.println(message);
        System.exit(1);
    }
}