package net.minestom.vanilla.datapack.json;

import net.minestom.vanilla.datapack.worldgen.ConfiguredFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FileLoaded<T> {
    protected @Nullable T value = null;
    protected final @NotNull String file;

    public FileLoaded(@NotNull String file) {
        this.file = file;
    }

    protected T get() {
        if (value == null) {
            throw new IllegalStateException("File not loaded yet");
        }
        return value;
    }

    public String file() {
        return file;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        //noinspection rawtypes
        var that = (FileLoaded) obj;
        return Objects.equals(this.file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public String toString() {
        return "Reference[" +
                "file=" + file + ']';
    }
}
