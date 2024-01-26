package net.minestom.vanilla.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PathFileSystem implements FileSystemImpl<ByteArray> {
    private final Path path;

    protected PathFileSystem(Path path) {
        this.path = path;
    }

    @Override
    public Set<String> folders() {
        // Return all folders in the path directory (Only this directory, not subdirectories)
        try (Stream<Path> paths = Files.walk(this.path, 0)) {
            return paths
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> files() {
        // Return all files in the path directory (Only this directory, not subdirectories)
        try (Stream<Path> paths = Files.walk(this.path, 0)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PathFileSystem folder(String path) {
        return new PathFileSystem(this.path.resolve(path));
    }

    @Override
    public ByteArray file(String path) {
        try {
            return ByteArray.wrap(Files.readAllBytes(this.path.resolve(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return FileSystemImpl.toString(this);
    }
}