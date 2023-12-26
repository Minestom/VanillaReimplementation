package net.minestom.vanilla.files;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class MappedFileSystem<F, T> implements FileSystem<T> {

    private final FileSystem<F> original;
    private final Function<F, T> mapper;

    protected MappedFileSystem(FileSystem<F> original, Function<F, T> mapper) {
        this.original = original;
        this.mapper = mapper;
    }

    @Override
    public Map<String, T> readAll() {
        return original.readAll().entrySet().stream()
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, e -> mapper.apply(e.getValue())));
    }

    @Override
    public Set<String> folders() {
        return original.folders();
    }

    @Override
    public FileSystem<T> folder(String path) {
        return new MappedFileSystem<>(original.folder(path), mapper);
    }
}