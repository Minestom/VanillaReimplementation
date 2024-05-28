package net.minestom.vanilla.files;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class CacheFileSystem<F> implements FileSystemImpl<F> {

    static final CacheFileSystem<?> EMPTY = new CacheFileSystem<>(new DynamicFileSystem<>());

    private final Map<String, F> files;
    private final Map<String, FileSystem<F>> folders;

    protected CacheFileSystem(FileSystem<F> original) {
        this.files = Map.copyOf(original.files().stream()
                .collect(Collectors.toUnmodifiableMap(Function.identity(), path -> {
                    try {
                        return original.file(path);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to cache file " + path, e);
                    }
                })));
        this.folders = original.folders().stream()
                .collect(Collectors.toUnmodifiableMap(Function.identity(),
                        name -> original.folder(name).cache()));
    }

    @Override
    public Set<String> folders() {
        return folders.keySet();
    }

    @Override
    public Set<String> files() {
        return files.keySet();
    }

    @Override
    public FileSystem<F> folder(String path) {
        return folders.getOrDefault(path, FileSystem.empty());
    }

    @Override
    public F file(String path) {
        return files.get(path);
    }

    @Override
    public String toString() {
        return FileSystemImpl.toString(this);
    }

    @Override
    public FileSystem<F> cache() {
        return this;
    }
}
