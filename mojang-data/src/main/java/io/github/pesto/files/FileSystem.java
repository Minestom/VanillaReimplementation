package io.github.pesto.files;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public interface FileSystem<F> extends FileSystemMappers {

    /**
     * @return a map of file names (no directory prefix) to F objects
     */
    Map<String, F> readAll();

    /**
     * Queries all the folders in the given directory and returns a set of folder names.
     *
     * @return a set of folder names (no directory prefix)
     */
    Set<String> folders();

    /**
     * Navigates to the given subdirectory and returns a new FileSource for that directory.
     *
     * @param path the path to the subdirectory
     * @return a new FileSource for the subdirectory
     */
    FileSystem<F> folder(String path);

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

    default FileSystem<F> folder(@NotNull String path, @NotNull String separator) {
        return folder(path.split(separator));
    }

    default F file(String path) {
        return readAll().get(path);
    }

    default <T> FileSystem<T> map(Function<F, T> mapper) {
        return new MappedFileSystem<>(this, mapper);
    }

    default FileSystem<F> cache() {
        if (this instanceof CacheFileSystem) return this;
        return new CacheFileSystem<>(this);
    }

    static FileSystem<ByteArray> empty() {
        return new DynamicFileSystem<>();
    }

    static FileSystem<ByteArray> fileSystem(Path path) {
        return new PathFileSystem(path);
    }

    static FileSystem<ByteArray> fromZipFile(File file) {
        return fromZipFile(file, p -> true);
    }

    static FileSystem<ByteArray> fromZipFile(File file, Predicate<String> pathFilter) {
        return FileSystemUtil.unzipIntoFileSystem(file, pathFilter);
    }

    default boolean hasFile(String path) {
        return readAll().containsKey(path);
    }
    default boolean hasFolder(String path) {
        return folders().contains(path);
    }

}