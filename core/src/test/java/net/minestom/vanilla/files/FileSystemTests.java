package net.minestom.vanilla.files;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FileSystemTests {
    @Test
    public void testDynamicWriteRead() {
        DynamicFileSystem<String> fs = new DynamicFileSystem<>();

        assertTrue(fs.folders().isEmpty());
        assertTrue(fs.files().isEmpty());

        fs.addFile("test.txt", "Hello, world!");
        fs.addFile("test2.txt", "Hello, world!");
        fs.addFile("test3.txt", "Lorem ipsum dolor sit amet, consectetur adipiscing elit.");

        assertEquals(3, fs.files().size());
        assertEquals("Hello, world!", fs.file("test.txt"));
        assertEquals("Hello, world!", fs.file("test2.txt"));
        assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", fs.file("test3.txt"));

        fs.folder("testDir");

        assertEquals(0, fs.folders().size());

        fs.addFolder("testDir").addFile("test4.txt", "Hello, world!");

        assertEquals(1, fs.folders().size());
        assertEquals(3, fs.files().size());
        assertEquals(1, fs.folder("testDir").files().size());
        assertEquals("Hello, world!", fs.folder("testDir").file("test4.txt"));
    }
}
