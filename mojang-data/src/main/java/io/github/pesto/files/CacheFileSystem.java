package io.github.pesto.files;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class CacheFileSystem<F> implements FileSystem<F> {

    private final Map<String, F> allFiles;
    private final Map<String, FileSystem<F>> subSources;

    protected CacheFileSystem(FileSystem<F> original) {
        this.allFiles = Map.copyOf(original.readAll());
        this.subSources = original.folders().stream()
                .collect(Collectors.toUnmodifiableMap(Function.identity(),
                        name -> new CacheFileSystem<>(original.folder(name))));
    }

    @Override
    public Map<String, F> readAll() {
        return allFiles;
    }

    @Override
    public Set<String> folders() {
        return subSources.keySet();
    }

    @Override
    public FileSystem<F> folder(String path) {
        return subSources.get(path);
    }

    @Override
    public String toString() {
        return FileSystem.toString(this);
    }
}