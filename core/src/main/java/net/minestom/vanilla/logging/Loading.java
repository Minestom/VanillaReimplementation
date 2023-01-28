package net.minestom.vanilla.logging;

public interface Loading {

    static void start(String name) {
        LoadingImpl.CURRENT.waitTask(name);
    }
    static StatusUpdater updater() {
        return LoadingImpl.CURRENT.getUpdater();
    }
    static void finish() {
        LoadingImpl.CURRENT.finishTask();
    }
    static void level(Level level) {
        LoadingImpl.CURRENT.level = level;
    }
}
