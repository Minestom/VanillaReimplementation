package net.minestom.vanilla.files;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public interface FileSystem<F> extends FileSystemMappers {

    /**
     * Queries all the folders in the given directory and returns a set of folder names.
     *
     * @return a set of folder names (no directory prefix)
     */
    Set<String> folders();

    /**
     * Queries all the files in the given directory and returns a set of file names.
     *
     * @return a set of file names (no directory prefix)
     */
    Set<String> files();

    /**
     * Navigates to the given subdirectory and returns a new FileSource for that directory.
     *
     * @param path the path to the subdirectory
     * @return a new FileSource for the subdirectory
     */
    FileSystem<F> folder(String path);

    /**
     * Reads the file at the given path and returns its contents.
     * @param path the path to the file
     * @return the contents of the file
     */
    F file(String path);

    FileSystem<F> folder(@NotNull String... paths);

    <T> FileSystem<T> map(Function<F, T> mapper);

    <T> FileSystem<T> map(BiFunction<String, F, T> mapper);

    FileSystem<F> cache();

    FileSystem<F> lazy();

    FileSystem<F> inMemory();

    static <T> FileSystem<T> empty() {
        //noinspection unchecked
        return (FileSystem<T>) CacheFileSystem.EMPTY;
    }

    static FileSystem<ByteArray> fromZipFile(File file, Predicate<String> pathFilter) {
        return FileSystemUtil.unzipIntoFileSystem(file, pathFilter);
    }

    default boolean hasFile(String file) {
        return files().contains(file);
    }

    default boolean hasFolder(String path) {
        return folders().contains(path);
    }
}