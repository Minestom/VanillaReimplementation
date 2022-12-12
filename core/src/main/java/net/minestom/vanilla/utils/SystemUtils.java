package net.minestom.vanilla.utils;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class SystemUtils {
    public static @NotNull String captureSystemOut(@NotNull Runnable runnable) {
        PrintStream oldOut = System.out;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        runnable.run();

        System.out.flush();
        System.setOut(oldOut);

        return baos.toString();
    }
}
