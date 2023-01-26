package io.github.pesto.files;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DynamicFileSystem<F> implements FileSystem<F> {

    protected final Map<String, F> files = new ConcurrentHashMap<>();
    protected final Map<String, DynamicFileSystem<F>> folders = new ConcurrentHashMap<>();

    protected DynamicFileSystem() {}

    public void processDirectory(String directoryName) {
        String folderName = directoryName.substring(0, directoryName.indexOf("/"));
        DynamicFileSystem<F> newFileSource = folder(folderName);
        String remaining = directoryName.substring(directoryName.indexOf("/") + 1);
        if (remaining.contains("/")) {
            newFileSource.processDirectory(remaining);
        }
    }

    public void processFile(String name, F contents) {
        if (name.contains("/")) {
            String folderName = name.substring(0, name.indexOf("/"));
            DynamicFileSystem<F> newFileSource = folder(folderName);
            String remaining = name.substring(name.indexOf("/") + 1);
            newFileSource.processFile(remaining, contents);
        } else {
            files.put(name, contents);
        }
    }

    @Override
    public Map<String, F> readAll() {
        return files.entrySet()
                .stream()
                .collect(Collectors.toMap(
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
}