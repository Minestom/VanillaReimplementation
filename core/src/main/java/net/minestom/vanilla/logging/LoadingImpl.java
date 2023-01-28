package net.minestom.vanilla.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class LoadingImpl implements Loading {
    public static @NotNull LoadingImpl CURRENT = new LoadingImpl(null, null, Level.INFO);

    private final @Nullable LoadingImpl parent;
    private final @Nullable LoadingBar loadingBar;
    private final long started = System.currentTimeMillis();
    private double progress = 0;
    public Level level;
    private LoadingImpl(@Nullable LoadingImpl parent, @Nullable LoadingBar loadingBar, Level level) {
        this.parent = parent;
        this.loadingBar = loadingBar;
        this.level = level;
    }

    public synchronized void waitTask(String name) {
        LoadingImpl loading;
        if (loadingBar == null) {
            loading = new LoadingImpl(this, LoadingBar.logger(name, Logger.logger().level(level)), level);
        } else {
            loading = new LoadingImpl(this, loadingBar.subTask(name), level);
        }
        CURRENT = loading;
    }

    public synchronized void finishTask() {
        if (loadingBar == null) {
            throw new IllegalStateException("Cannot finish root task");
        }
        loadingBar.updater().progress(1);
        assert parent != null;
        Logger.logger().level(parent.level).printf("took %dms%n", System.currentTimeMillis() - started);
        CURRENT = this.parent;
    }

    public synchronized StatusUpdater getUpdater() {
        if (loadingBar == null)
            throw new IllegalStateException("Cannot get updater for root task");
        return loadingBar.updater();
    }
}
