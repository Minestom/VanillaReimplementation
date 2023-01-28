package net.minestom.vanilla.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class LoadingImpl implements Loading {
    public static @NotNull LoadingImpl CURRENT = new LoadingImpl(null, LoadingBar.console(""), Level.INFO);

    private final @Nullable LoadingImpl parent;
    private final LoadingBar loadingBar;
    private double progress = 0;
    private Level level;
    private LoadingImpl(@Nullable LoadingImpl parent, @NotNull LoadingBar loadingBar, Level level) {
        this.parent = parent;
        this.loadingBar = loadingBar;
        this.level = level;
    }

    public synchronized void setLevel(Level level) {
        this.level = level;
    }

    public synchronized void waitTask(String name) {
        LoadingImpl loading;
        if (parent == null) {
            loading = new LoadingImpl(this, LoadingBar.logger(name, Logger.logger().level(level)), level);
        } else {
            loading = new LoadingImpl(this, loadingBar.subTask(name), level);
        }
        loading.loadingBar.updater().update();
        CURRENT = loading;
    }

    public synchronized void finishTask() {
        if (parent == null) {
            throw new IllegalStateException("Cannot finish root task");
        }
        parent.loadingBar.updater().progress(parent.progress);
        CURRENT = this.parent;
    }

    public synchronized StatusUpdater getUpdater() {
        return loadingBar.updater();
    }
}
