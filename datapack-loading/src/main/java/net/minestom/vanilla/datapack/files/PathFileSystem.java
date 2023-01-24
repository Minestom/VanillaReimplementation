package net.minestom.vanilla.datapack.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record PathFileSystem(Path path) implements FileSystem<InputStream> {

    @Override
    public Map<String, InputStream> readAll() {
        // Return all files in the path directory (Only this directory, not subdirectories)
        try {
            return Files.walk(this.path, 0)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toMap(
                            path -> path.getFileName().toString(),
                            path -> {
                                try {
                                    return Files.newInputStream(path);
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
