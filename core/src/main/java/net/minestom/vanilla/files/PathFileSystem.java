package net.minestom.vanilla.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class PathFileSystem implements FileSystem<ByteArray> {
    private final Path path;

    protected PathFileSystem(Path path) {
        this.path = path;
    }

    @Override
    public Map<String, ByteArray> readAll() {
        // Return all files in the path directory (Only this directory, not subdirectories)
        try {
            return Files.walk(this.path, 0)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toMap(
                            path -> path.getFileName().toString(),
                            path -> {
                                try {
                                    return ByteArray.wrap(Files.readAllBytes(path));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> folders() {
        // Return all folders in the path directory (Only this directory, not subdirectories)
        try {
            return Files.walk(this.path, 0)
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PathFileSystem folder(String path) {
        return new PathFileSystem(this.path.resolve(path));
    }
}