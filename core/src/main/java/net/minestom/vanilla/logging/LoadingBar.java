package net.minestom.vanilla.logging;

import java.security.Provider;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

interface LoadingBar {

    static LoadingBar console(String initialMessage) {
        return new LoggingLoadingBar(initialMessage, System.out::print);
    }
    static LoadingBar logger(String initialMessage, Logger logger) {
        return new LoggingLoadingBar(initialMessage, logger::print);
    }
    LoadingBar subTask(String task);
    StatusUpdater updater();

    String message();
}
