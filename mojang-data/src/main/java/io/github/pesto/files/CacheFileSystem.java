package io.github.pesto.files;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CacheFileSystem<F> implements FileSystem<F> {

    private final FileSystem<F> original;
    private Map<String, F> allFiles = null;
    private Map<String, FileSystem<F>> subSources = null;

    protected CacheFileSystem(FileSystem<F> original) {
        this.original = original;
    }

    @Override
    public Map<String, F> readAll() {
        if (allFiles == null) {
            allFiles = original.readAll();
        }
        return allFiles;
    }

    @Override
    public Set<String> folders() {
        if (subSources == null) {
            subSources = original.folders().stream()
                    .collect(Collectors.toConcurrentMap(Function.identity(),
                            name -> new CacheFileSystem<>(original.folder(name))));
        }
        return subSources.keySet();
    }

    @Override
    public FileSystem<F> folder(String path) {
        if (subSources == null) {
            folders();
        }
        return subSources.get(path);
    }
}