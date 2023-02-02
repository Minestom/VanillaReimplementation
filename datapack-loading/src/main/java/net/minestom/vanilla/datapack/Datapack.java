package net.minestom.vanilla.datapack;

import io.github.pesto.files.ByteArray;
import io.github.pesto.files.FileSystem;

import java.io.IOException;
import java.io.InputStream;

public interface Datapack {

    static Datapack loadPrimitiveByteArray(FileSystem<byte[]> source) {
        return loadByteArray(source.map(ByteArray::wrap));
    }

    static Datapack loadInputStream(FileSystem<InputStream> source) {
        return loadPrimitiveByteArray(source.map(stream -> {
            try {
                return stream.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    static Datapack loadByteArray(FileSystem<ByteArray> source) {
        return new DatapackLoader(source.cache()).load();
    }
}
