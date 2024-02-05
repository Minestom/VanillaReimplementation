package net.minestom.vanilla.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {

    private static final Comparator<String> COMPARATOR_STRING_DESC = (str1, str2) -> str1 == null ? 1 : str2 == null ? -1 : -str1.compareTo(str2);

    public static Map<String, byte[]> unzip(InputStream is) throws IOException {
        try (ZipInputStream in = new ZipInputStream(is)) {
            ZipEntry entry;
            Map<String, byte[]> content = new HashMap<>();
            Set<String> dirs = new TreeSet<>(COMPARATOR_STRING_DESC);

            while ((entry = in.getNextEntry()) != null) {
                String path = removeDirectoryMarker(replaceIncorrectFileSeparators(entry.getName()));

                if (isDirectory(entry)) {
                    dirs.add(path);
                } else {
                    content.put(path, in.readAllBytes());
                }
            }

            addOnlyEmptyDirectories(dirs, content);

            return content.isEmpty() ? Collections.emptyMap() : content;
        }
    }

    private static boolean isDirectory(ZipEntry entry) {
        return entry.isDirectory() || entry.getName().endsWith(ILLEGAL_DIR_MARKER);
    }

    private static void addOnlyEmptyDirectories(Set<String> dirs, Map<String, byte[]> content) {
        if (dirs.isEmpty()) {
            return;
        }

        Set<String> paths = new HashSet<>(content.keySet());

        for (String dir : dirs) {
            boolean empty = true;

            for (String path : paths) {
                if (path.startsWith(dir)) {
                    empty = false;
                    break;
                }
            }

            if (empty) {
                content.put(dir, null);
            }
        }
    }

    private static final String DIR_MARKER = "/";
    private static final String ILLEGAL_DIR_MARKER = "\\";
    private static final Pattern BACK_SLASH = Pattern.compile("\\\\");

    private static String removeDirectoryMarker(String path) {
        return path.endsWith(DIR_MARKER) || path.endsWith(ILLEGAL_DIR_MARKER) ? path.substring(0, path.length() - 1) : path;
    }

    private static String replaceIncorrectFileSeparators(String path) {
        return BACK_SLASH.matcher(path).replaceAll(DIR_MARKER);
    }
}
