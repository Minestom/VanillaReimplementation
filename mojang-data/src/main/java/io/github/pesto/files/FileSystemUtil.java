package io.github.pesto.files;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

import java.io.*;
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

    public static FileSystem<byte[]> unzipIntoFileSource(@NotNull File file) {
        DynamicFileSystem<byte[]> source = new DynamicFileSystem<>();
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                String name = entry.getName();
                if (!name.startsWith("data/minecraft/"))
                    continue;

                if (entry.isDirectory() || name.endsWith("\\")) {
                    source.processDirectory(name);
                } else {
                    source.processFile(name, in.readAllBytes());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return source;
    }
}