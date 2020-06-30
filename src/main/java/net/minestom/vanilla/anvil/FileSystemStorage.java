package net.minestom.vanilla.anvil;

import net.minestom.server.storage.StorageSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemStorage implements StorageSystem {
    @Override
    public void open(String folderPath) {}

    @Override
    public void close() {}

    @Override
    public boolean exists(String folderPath) {
        return Files.exists(Path.of(folderPath));
    }

    @Override
    public byte[] get(String key) {
        try {
            return Files.readAllBytes(getPath(key));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Path getPath(String key) {
        return Path.of(key+".dat");
    }

    @Override
    public void set(String key, byte[] data) {
        try {
            Files.write(getPath(key), data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.delete(getPath(key));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
