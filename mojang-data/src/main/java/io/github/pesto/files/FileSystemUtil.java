package io.github.pesto.files;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileSystemUtil {

    static <I extends InputStream> FileSystem<ByteArray> toBytes(FileSystem<I> source) {
        return source.map(inputStream -> {
            try {
                return ByteArray.of(inputStream.readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    static <I extends InputStream> FileSystem<String> toString(FileSystem<I> source) {
        return toBytes(source).map(ByteArray::toString);
    }

    static <T extends InputStream> FileSystem<JsonElement> toJson(FileSystem<T> source) {
        Gson gson = new Gson();
        return toString(source).map(str -> gson.fromJson(str, JsonElement.class));
    }

    static DynamicFileSystem<ByteArray> unzipIntoFileSystem(@NotNull File file, Predicate<String> filter) {
        DynamicFileSystem<ByteArray> source = new DynamicFileSystem<>();
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry;

            while ((entry = in.getNextEntry()) != null) {
                String name = entry.getName();
                if (!filter.test(name))
                    continue;

                if (entry.isDirectory() || name.endsWith("\\")) {
                    source.processDirectory(name);
                } else {
                    System.out.println(name);
                    source.processFile(name, ByteArray.of(in.readAllBytes()));
                }
            }
        } catch (IOException | PatternSyntaxException e) {
            throw new RuntimeException(e);
        }
        return source;
    }
}