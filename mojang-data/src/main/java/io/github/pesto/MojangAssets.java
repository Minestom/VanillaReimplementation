package io.github.pesto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.pesto.files.ByteArray;
import io.github.pesto.files.CacheFileSystem;
import io.github.pesto.files.FileSystem;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;

import static java.nio.file.StandardOpenOption.*;

public final class MojangAssets {
    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private CacheFileSystem<ByteArray> fs = null;

    public CacheFileSystem<ByteArray> getFileSystem() {
        return fs;
    }

    public void downloadResources(String version, @NotNull File root) {
        try {
            // Check if source files already exist
            File jar = new File(root, version + File.separator + "sources.jar");

            if (!jar.exists()) {

                // Get version info
                String versionInfoUrl = findVersionInfoUrl(version);
                JsonObject versionInfo = downloadJson(versionInfoUrl);

                // Download jar
                System.out.println("Downloading vanilla jar...");
                jar = downloadJar(versionInfo, root);
            }

            this.fs = new CacheFileSystem<>(FileSystem.fromZipFile(jar));

            // Extract files
            // System.out.println("Extracting assets from jar...");
            // this.completed = extractJarAssets(jar, root);

        } catch (IOException e) {
            exitError(e.getMessage());
        }

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
    private File downloadJar(JsonObject versionInfo, @NotNull File root) throws IOException {
        JsonObject downloads = versionInfo.getAsJsonObject("downloads");
        JsonObject client = downloads.getAsJsonObject("client");
        String url = client.get("url").getAsString();

        String version = versionInfo.get("id").getAsString();
        File destination = new File(root, version + File.separator + "resources.jar");
        if (checkOrCreate(destination))
            return destination;

        try (
                ReadableByteChannel in = Channels.newChannel(new URL(url).openStream());
                FileChannel channel = FileChannel.open(destination.toPath(), CREATE, WRITE, TRUNCATE_EXISTING)
        ) {
            channel.transferFrom(in, 0, Long.MAX_VALUE);

            boolean success = destination.exists() && destination.length() > 0;
            if (!success)
                throw new IOException("Failed to download client JAR");
        }
        return destination;
    }

    private boolean checkOrCreate(@NotNull File file) throws IOException {
        if (file.exists())
            return true;

        file.getParentFile().mkdirs();
        if (!file.exists())
            return file.createNewFile();

        return true;
    }

    private File checkAndCreateFile(File destination, ZipEntry entry) throws IOException {
        String path = entry.getName().replace("data/", "").replace("minecraft/", "");
        File destFile = new File(destination, path);

        String dirPath = destination.getCanonicalPath();
        String filePath = destFile.getCanonicalPath();

        if (!filePath.startsWith(dirPath + File.separator)) {
            throw new IOException("Entry outside target: " + entry.getName());
        }

        return destFile;
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