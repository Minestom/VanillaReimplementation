package io.github.pesto.files;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface FileSystem<F> {

    /**
     * @return a map of file names (no directory prefix) to F objects
     */
    Map<String, F> readAll();

    /**
     * Queries all the folders in the given directory and returns a set of folder names.
     * @return a set of folder names (no directory prefix)
     */
    Set<String> folders();

    /**
     * Navigates to the given subdirectory and returns a new FileSource for that directory.
     * @param path the path to the subdirectory
     * @return a new FileSource for the subdirectory
     */
    FileSystem<F> folder(String path);

    default F file(String path) {
        return readAll().get(path);
    }

    default <T> FileSystem<T> map(Function<F, T> mapper) {
        return new MappedFileSystem<>(this, mapper);
    }

    static FileSystem<InputStream> fileSystem(Path path) {
        return new PathFileSystem(path);
    }

    static FileSystem<byte[]> fromZipFile(File file) {
        return FileSystemUtil.unzipIntoFileSource(file);
    }
}