package net.minestom.vanilla.logging;

import net.minestom.server.network.packet.client.login.LoginStartPacket;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public interface Loading {

    static void start(String name) {
        LoadingImpl.CURRENT.waitTask(name);
    }
    static void process(String name, CompletableFuture<?>... futures) {
        start(name);
        double count = futures.length;
        AtomicInteger completed = new AtomicInteger(0);
        for (int i = 0; i < futures.length; i++) {
            CompletableFuture<?> future = futures[i];

            if (future == null) {
                futures[i] = CompletableFuture.completedFuture(null);
                completed.incrementAndGet();
                continue;
            }
            future.whenComplete((o, throwable) -> {
                if (throwable != null) {
                    throw new RuntimeException(throwable);
                }
                double finishedCount = completed.incrementAndGet();
                updater().progress(finishedCount / count);
                updater().update();
            });
        }
        CompletableFuture.allOf(futures).join();
        finish();
    }
    static StatusUpdater updater() {
        return LoadingImpl.CURRENT.getUpdater();
    }
    static void finish() {
        LoadingImpl.CURRENT.finishTask();
    }
    static void level(Level level) {
        LoadingImpl.CURRENT.setLevel(level);
    }
}
