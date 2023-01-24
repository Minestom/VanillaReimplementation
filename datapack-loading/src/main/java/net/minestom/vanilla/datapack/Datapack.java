package net.minestom.vanilla.datapack;

import net.minestom.vanilla.datapack.files.FileSystem;

import java.io.InputStream;

public interface Datapack {

    static Datapack load(FileSystem<InputStream> source) {
        return new DatapackLoader(source).load();
    }
}
