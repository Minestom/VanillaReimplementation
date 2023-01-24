package net.minestom.vanilla.datapack.files;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileSystemUtil {

    public static <I extends InputStream> FileSystem<byte[]> toBytes(FileSystem<I> source) {
        return source.map(inputStream -> {
            try {
                return inputStream.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <I extends InputStream> FileSystem<String> toString(FileSystem<I> source) {
        return toBytes(source).map(String::new);
    }

    public static <T extends InputStream> FileSystem<JsonElement> toJson(FileSystem<T> source) {
        Gson gson = new Gson();
        return toString(source).map(str -> gson.fromJson(str, JsonElement.class));
    }

    public static FileSystem<byte[]> unzipIntoFileSource(InputStream zipContents) {
        DynamicFileSystem<byte[]> source = new DynamicFileSystem<>();
        try (ZipInputStream in = new ZipInputStream(zipContents)) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (entry.isDirectory() || entry.getName().endsWith("\\")) {
                    source.processDirectory(entry.getName());
                } else {
                    source.processFile(entry.getName(), in.readAllBytes());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return source;
    }
}
