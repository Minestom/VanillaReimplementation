package net.minestom.vanilla.files;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A FileSystem that lazily loads its contents.
 * @param <F>
 */
public class LazyFileSystem<F> implements FileSystem<F> {

    private @Nullable Map<String, F> files = null;
    private @Nullable Map<String, FileSystem<F>> folders = null;
    private final FileSystem<F> original;

    protected LazyFileSystem(FileSystem<F> original) {
        this.original = original;
    }

    private Map<String, F> loadFiles() {
        if (files != null)
            return files;
        this.files = original.readAll();
        return files;
    }

    private Map<String, FileSystem<F>> loadFolders() {
        if (folders != null)
            return folders;
        this.folders = original.folders().stream()
                .collect(Collectors.toUnmodifiableMap(Function.identity(),
                        name -> original.folder(name).lazy()));
        return folders;
    }

    @Override
    public Map<String, F> readAll() {
        return loadFiles();
    }

    @Override
    public Set<String> folders() {
        return loadFolders().keySet();
    }

    @Override
    public FileSystem<F> folder(String path) {
        return loadFolders().get(path);
    }

    @Override
    public String toString() {
        return FileSystem.toString(this);
    }

    @Override
    public FileSystem<F> lazy() {
        return this;
    }
}
