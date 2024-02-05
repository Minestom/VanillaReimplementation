package net.minestom.vanilla.files;

import java.util.Set;
import java.util.function.BiFunction;

class MappedFileSystem<F, T> implements FileSystemImpl<T> {

    private final FileSystem<F> original;
    private final BiFunction<String, F, T> mapper;

    protected MappedFileSystem(FileSystem<F> original, BiFunction<String, F, T> mapper) {
        this.original = original;
        this.mapper = mapper;
    }

    @Override
    public Set<String> folders() {
        return original.folders();
    }

    @Override
    public Set<String> files() {
        return original.files();
    }

    @Override
    public FileSystemImpl<T> folder(String path) {
        return new MappedFileSystem<>(original.folder(path), mapper);
    }

    @Override
    public T file(String path) {
        return mapper.apply(path, original.file(path));
    }

    @Override
    public String toString() {
        return FileSystemImpl.toString(this);
    }
}