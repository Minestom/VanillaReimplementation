package net.minestom.vanilla.files;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A FileSystem that lazily loads its contents.
 * @param <F>
 */
public class LazyFileSystem<F> implements FileSystemImpl<F> {

    private final FileSystem<F> original;

    protected LazyFileSystem(FileSystem<F> original) {
        this.original = original;
    }

    private Set<String> folders = null;
    @Override
    public Set<String> folders() {
        if (folders == null) {
            folders = Set.copyOf(original.folders());
        }
        return folders;
    }

    private Set<String> files = null;
    @Override
    public Set<String> files() {
        if (files == null) {
            files = Set.copyOf(original.files());
        }
        return files;
    }


    private final Map<String, @Nullable FileSystem<F>> folderCache = new ConcurrentHashMap<>();
    @Override
    public FileSystem<F> folder(String path) {
        return folderCache.computeIfAbsent(path, original::folder);
    }

    private final Map<String, @Nullable F> fileCache = new ConcurrentHashMap<>();
    @Override
    public F file(String path) {
        return fileCache.computeIfAbsent(path, original::file);
    }

    @Override
    public String toString() {
        return FileSystemImpl.toString(this);
    }

    @Override
    public FileSystem<F> lazy() {
        return this;
    }
}
