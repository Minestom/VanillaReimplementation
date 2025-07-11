package net.minestom.vanilla.common.tag;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * This file contains code ported from Kotlin to Java, adapted from the Blocks and Stuff project.
 * Original source: https://github.com/everbuild-org/blocks-and-stuff
 * <p>
 * Original authors: ChrisB, AEinNico, CreepyX
 * <p>
 * Ported from Kotlin to Java and adapted for use in this project with modifications.
 * <p>
 * Utility class for loading tag data from JSON files
 */
public class TagRegistryLoader {
    private static final Gson gson = new Gson();

    /**
     * Iterates through resources in a directory and applies an action to each path
     * @param resourceDir The resource directory to iterate through
     * @param module The class to use for resource loading
     * @param action The action to apply to each path
     */
    private static void iterateResources(String resourceDir, Class<?> module, Consumer<Path> action) {
        try {
            URL resource = module.getResource(resourceDir);
            if (resource == null) {
                throw new IllegalArgumentException("Cannot find resources: " + resourceDir);
            }

            URI uri = resource.toURI();
            Path myPath;

            if ("jar".equals(uri.getScheme())) {
                Map<String, String> env = new HashMap<>();
                FileSystem fileSystem = FileSystems.newFileSystem(uri, env);
                myPath = fileSystem.getPath(resourceDir);
            } else {
                myPath = Paths.get(uri);
            }

            try (Stream<Path> walk = Files.walk(myPath)) {
                walk.forEach(action);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads tags from JSON files in a specific folder
     * @param folder The folder to load from
     * @param type The type of tags to load
     * @param module The class to use for resource loading
     * @return A map of tag names to sets of values
     */
    public static Map<String, Set<String>> loadTags(String folder, String type, Class<?> module) {
        Map<String, Set<String>> map = new HashMap<>();

        iterateResources("/" + folder + "/" + type + "/", module, path -> {
            String fileName = path.getFileName().toString();
            if (!fileName.endsWith(".json")) return;

            try {
                URL pathSpec = module.getResource(path.toString());
                if (pathSpec == null) return;

                File file = new File(pathSpec.getFile());
                if (!"json".equalsIgnoreCase(getFileExtension(file))) return;

                try (InputStreamReader reader = new InputStreamReader(pathSpec.openStream())) {
                    JsonObject value = gson.fromJson(reader, JsonObject.class);
                    String name = value.get("tag").getAsString();
                    JsonArray contentsArray = value.getAsJsonArray("contents");

                    Set<String> values = StreamSupport.stream(contentsArray.spliterator(), false)
                        .map(JsonElement::getAsString)
                        .collect(Collectors.toSet());

                    map.computeIfAbsent(name, k -> new HashSet<>()).addAll(values);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return map;
    }

    /**
     * Gets the file extension from a file
     * @param file The file to get the extension from
     * @return The file extension
     */
    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf + 1);
    }
}
