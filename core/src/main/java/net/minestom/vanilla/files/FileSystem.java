package net.minestom.vanilla.files;

import it.unimi.dsi.fastutil.objects.ObjectIterable;
import it.unimi.dsi.fastutil.objects.ObjectIterables;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    /**
     * Recursively reads all files in the given directory and its subdirectories.
     * @return an iterable of all files in the directory
     */
    default Iterable<FileEntry<F>> allFiles() {
        return () -> {
            Stream<FileEntry<F>> files = files().stream()
                    .map(file -> new FileEntry<>(Path.of(file), file(file)));

            Stream<FileEntry<F>> dirs = folders().stream()
                    .flatMap(folder -> {
                        FileSystem<F> subFolder = folder(folder);
                        return StreamSupport.stream(subFolder.allFiles().spliterator(), false)
                                .map(entry -> entry.withPath(Path.of(folder).resolve(entry.path)));
                    });

            return Stream.concat(files, dirs).iterator();
        };
    }

    record FileEntry<F>(Path path, F file) {
        private FileEntry<F> withPath(Path path) {
            return new FileEntry<>(path, file);
        }
    }

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