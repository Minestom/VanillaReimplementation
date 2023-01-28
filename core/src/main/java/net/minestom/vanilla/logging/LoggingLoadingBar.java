package net.minestom.vanilla.logging;

import java.util.function.Consumer;

class LoggingLoadingBar implements LoadingBar {

    private String message;
    private double progress;
    private final StatusUpdater updater;
    private final Consumer<String> out;
    public LoggingLoadingBar(String initialMessage, Consumer<String> out) {
        this.message = initialMessage;
        this.progress = 0;
        this.updater = new UpdaterImpl();
        this.out = out;
        renderThis();
    }

    @Override
    public SubTaskLoadingBar subTask(String task) {
        return new SubTaskLoadingBar(this, task);
    }

    public StatusUpdater updater() {
        return updater;
    }

    @Override
    public String message() {
        return message;
    }

    private void renderThis() {
        out.accept("\r");
        render(message, progress, out);
    }

    private static void render(String message, double progress, Consumer<String> out) {
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append(" ");
        accumulate(progress * 32.0, 32, sb);
        sb.append(" ");
        out.accept(sb.toString());
    }

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    private static void accumulate(double width, double total, StringBuilder out) {
        out.append(Color.RESET);
        out.append(Color.BLUE_BOLD);
        out.append("|");
        out.append(Color.RESET);
        out.append(Color.CYAN);
        double remaining = total - width;
        while (width >= 1) {
            width -= 1;
            out.append("=");
        }
        out.append(">");
        out.append(Color.RESET);
        out.append(Color.WHITE_UNDERLINED);
        while (remaining > 1) {
            remaining -= 1;
            out.append(" ");
        }
        out.append(Color.RESET);
        out.append(Color.BLUE_BOLD);
        out.append("|");
        out.append(Color.RESET);
    }

    private class UpdaterImpl implements StatusUpdater {
        @Override
        public synchronized void progress(double progress) {
            LoggingLoadingBar.this.progress = progress;
        }

        @Override
        public synchronized void message(String message) {
            LoggingLoadingBar.this.message = message;
        }

        @Override
        public void update() {
            renderThis();
        }
    }

    public class SubTaskLoadingBar implements LoadingBar {

        private final LoadingBar parent;
        private String message;
        private double progress;
        private final UpdaterImpl updater;
        public SubTaskLoadingBar(LoadingBar parent, String message) {
            this.parent = parent;
            this.message = message;
            this.progress = 0;
            this.updater = new UpdaterImpl();
        }

        @Override
        public LoadingBar subTask(String task) {
            return new SubTaskLoadingBar(this, task);
        }

        private void renderThis() {
            if (parent instanceof LoggingLoadingBar root) {
                root.renderThis();
            } else if (parent instanceof SubTaskLoadingBar sub) {
                sub.renderThis();
            }
            render(message, progress, out);
        }

        @Override
        public StatusUpdater updater() {
            return updater;
        }

        @Override
        public String message() {
            return message;
        }

        private class UpdaterImpl implements StatusUpdater {
            @Override
            public void progress(double progress) {
                SubTaskLoadingBar.this.progress = progress;
            }

            @Override
            public void message(String message) {
                SubTaskLoadingBar.this.message = message;
            }

            @Override
            public void update() {
                renderThis();
            }
        }
    }
}
