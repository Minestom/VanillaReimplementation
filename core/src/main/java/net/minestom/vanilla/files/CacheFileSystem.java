package net.minestom.vanilla.files;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class CacheFileSystem<F> implements FileSystem<F> {

    static final CacheFileSystem<?> EMPTY = new CacheFileSystem<>(new DynamicFileSystem<>());

    private final Map<String, F> files;
    private final Map<String, FileSystem<F>> folders;

    protected CacheFileSystem(FileSystem<F> original) {
        this.files = Map.copyOf(original.readAll());
        this.folders = original.folders().stream()
                .collect(Collectors.toUnmodifiableMap(Function.identity(),
                        name -> original.folder(name).cache()));
    }

    @Override
    public Map<String, F> readAll() {
        return files;
    }

    @Override
    public Set<String> folders() {
        return folders.keySet();
    }

    @Override
    public FileSystem<F> folder(String path) {
        return folders.get(path);
    }

    @Override
    public String toString() {
        return FileSystem.toString(this);
    }

    @Override
    public FileSystem<F> cache() {
        return this;
    }
}