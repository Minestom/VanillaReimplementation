package net.minestom.vanilla.logging;

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
