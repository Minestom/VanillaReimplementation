package net.minestom.vanilla.files;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

interface FileSystemImpl<F> extends FileSystem<F> {

    default FileSystem<F> folder(@NotNull String... paths) {
        if (paths.length == 0)
            return this;

        Iterator<String> iter = Arrays.stream(paths).iterator();
        FileSystem<F> fs = folder(iter.next());

        while (iter.hasNext()) {
            fs = fs.folder(iter.next());
            if (fs.folders().isEmpty())
                return fs;
        }
        return fs;
    }

    default <T> FileSystem<T> map(Function<F, T> mapper) {
        return map((str, file) -> mapper.apply(file));
    }

    default <T> FileSystem<T> map(BiFunction<String, F, T> mapper) {
        return new MappedFileSystem<>(this, mapper);
    }

    default FileSystem<F> cache() {
        return new CacheFileSystem<>(this);
    }

    default FileSystem<F> lazy() {
        return new LazyFileSystem<>(this);
    }

    default FileSystem<F> inMemory() {
        return DynamicFileSystem.from(this);
    }

    static String toString(FileSystem<?> fs) {
        Stream.Builder<String> builder = Stream.builder();
        toString(fs, builder, 0);
        return builder.build().collect(Collectors.joining());
    }

    static void toString(FileSystem<?> fs, Stream.Builder<String> builder, int depth) {
        String prefix = "  ".repeat(depth);

        String folderChar = "\uD83D\uDDC0";
        String fileChar = "\uD83D\uDDCE";

        for (String folder : fs.folders()) {
            builder.add(prefix);
            builder.add(folderChar);
            builder.add(" ");
            builder.add(folder);
            builder.add("\n");
            toString(fs.folder(folder), builder, depth + 1);
        }

        for (String file : fs.files()) {
            builder.add(prefix);
            builder.add(fileChar);
            builder.add(" ");
            builder.add(file);
            builder.add("\n");
        }
    }
}