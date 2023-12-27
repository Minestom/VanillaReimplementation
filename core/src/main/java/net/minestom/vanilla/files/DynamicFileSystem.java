package net.minestom.vanilla.files;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DynamicFileSystem<F> implements FileSystemImpl<F> {

    protected final Map<String, F> files = new ConcurrentHashMap<>();
    protected final Map<String, DynamicFileSystem<F>> folders = new ConcurrentHashMap<>();

    protected DynamicFileSystem() {}

    static <F> FileSystem<F> from(FileSystemImpl<F> fileSystem) {
        DynamicFileSystem<F> dynamicFileSystem = new DynamicFileSystem<>();
        for (String folder : fileSystem.folders()) {
            FileSystemImpl<F> subFileSystem = (FileSystemImpl<F>) fileSystem.folder(folder);
            dynamicFileSystem.folders.put(folder, (DynamicFileSystem<F>) from(subFileSystem));
        }
        for (Map.Entry<String, F> entry : fileSystem.files().stream()
                .collect(Collectors.toUnmodifiableMap(Function.identity(), fileSystem::file)).entrySet()) {
            dynamicFileSystem.addFile(entry.getKey(), entry.getValue());
        }
        return dynamicFileSystem;
    }

    public DynamicFileSystem<F> addFolder(String directoryName) {
        int split = directoryName.contains("/") ? directoryName.indexOf('/') : directoryName.length();
        String folderName = directoryName.substring(0, split);
        DynamicFileSystem<F> fileSource = folders.computeIfAbsent(folderName, s -> new DynamicFileSystem<>());
        String remaining = directoryName.substring(directoryName.indexOf("/") + 1);
        if (remaining.contains("/")) {
            fileSource.addFolder(remaining);
        }
        return fileSource;
    }

    public void addFile(String name, F contents) {
        if (!name.contains("/")) {
            files.put(name, contents);
            return;
        }

        String folderName = name.substring(0, name.indexOf("/"));
        DynamicFileSystem<F> newFileSource = addFolder(folderName);
        String remaining = name.substring(name.indexOf("/") + 1);
        newFileSource.addFile(remaining, contents);
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
    public FileSystem<F> folder(@NotNull String path) {
        var fs = folders.get(path);
        return fs == null ? FileSystem.empty() : fs;
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
    public FileSystem<F> inMemory() {
        return this;
    }
}