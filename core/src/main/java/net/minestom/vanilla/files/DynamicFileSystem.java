package net.minestom.vanilla.files;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DynamicFileSystem<F> implements FileSystem<F> {

    protected final Map<String, F> files = new ConcurrentHashMap<>();
    protected final Map<String, DynamicFileSystem<F>> folders = new ConcurrentHashMap<>();

    protected DynamicFileSystem() {}

    static <F> FileSystem<F> from(FileSystem<F> fileSystem) {
        DynamicFileSystem<F> dynamicFileSystem = new DynamicFileSystem<>();
        for (String folder : fileSystem.folders()) {
            FileSystem<F> subFileSystem = fileSystem.folder(folder);
            dynamicFileSystem.folders.put(folder, (DynamicFileSystem<F>) from(subFileSystem));
        }
        for (Map.Entry<String, F> entry : fileSystem.readAll().entrySet()) {
            dynamicFileSystem.processFile(entry.getKey(), entry.getValue());
        }
        return dynamicFileSystem;
    }

    public void processDirectory(String directoryName) {
        String folderName = directoryName.substring(0, directoryName.indexOf("/"));
        DynamicFileSystem<F> newFileSource = folder(folderName);
        String remaining = directoryName.substring(directoryName.indexOf("/") + 1);
        if (remaining.contains("/")) {
            newFileSource.processDirectory(remaining);
        }
    }

    public void processFile(String name, F contents) {
        if (!name.contains("/")) {
            files.put(name, contents);
            return;
        }

        String folderName = name.substring(0, name.indexOf("/"));
        DynamicFileSystem<F> newFileSource = folder(folderName);
        String remaining = name.substring(name.indexOf("/") + 1);
        newFileSource.processFile(remaining, contents);
    }

    @Override
    public Map<String, F> readAll() {
        return files.entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    @Override
    public Set<String> folders() {
        return folders.keySet();
    }

    @Override
    public DynamicFileSystem<F> folder(@NotNull String path) {
        return folders.computeIfAbsent(path, ignored -> new DynamicFileSystem<>());
    }

    @Override
    public String toString() {
        return FileSystem.toString(this);
    }
}