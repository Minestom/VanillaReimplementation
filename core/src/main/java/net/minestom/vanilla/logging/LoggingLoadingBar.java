package net.minestom.vanilla.logging;

import java.util.function.Consumer;

class LoggingLoadingBar implements LoadingBar {

    private static final double PROGRESS_BAR_WIDTH = Integer.parseInt(System.getProperty("vri.loadingBarWidth", "20"));
    private static final Color MESSAGE_COLOR = Color.valueOf(System.getProperty("vri.loadingBarMessageColor", "BLUE_BOLD"));

    private String message;
    private double progress;
    private final StatusUpdater updater;
    private final Consumer<String> out;
    private final int depth = 0;
    public LoggingLoadingBar(String initialMessage, Consumer<String> out) {
        this.message = initialMessage;
        this.progress = 0;
        this.updater = new UpdaterImpl();
        this.out = out;
        renderThis();
    }

    @Override
    public SubTaskLoadingBar subTask(String task) {
        return new SubTaskLoadingBar(this, task, depth + 1);
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
        sb.append(MESSAGE_COLOR);
        sb.append(message);
        sb.append(" ");
        accumulate(progress * PROGRESS_BAR_WIDTH, PROGRESS_BAR_WIDTH, sb);
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
        while (width > 1) {
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
            if (LoggingLoadingBar.this.progress != progress) {
                LoggingLoadingBar.this.progress = progress;
                renderThis();
            }
        }

        @Override
        public synchronized void message(String message) {
            if (!LoggingLoadingBar.this.message.equals(message)) {
                LoggingLoadingBar.this.message = message;
                renderThis();
            }
        }
    }

    public class SubTaskLoadingBar implements LoadingBar {

        private final LoadingBar parent;
        private String message;
        private double progress;
        private final UpdaterImpl updater;
        private final int depth;
        public SubTaskLoadingBar(LoadingBar parent, String message, int depth) {
            this.parent = parent;
            this.message = message;
            this.progress = 0;
            this.updater = new UpdaterImpl();
            this.depth = depth;
        }

        @Override
        public LoadingBar subTask(String task) {
            return new SubTaskLoadingBar(this, task, depth + 1);
        }

        private void printIndent(LoadingBar bar) {
            if (bar instanceof SubTaskLoadingBar sub) {
                printIndent(sub.parent);
            } else {
                out.accept(Color.YELLOW_BRIGHT.toString());
            }
            out.accept("|   ");
        }

        private void renderThis() {
            printIndent(parent);
            out.accept(MESSAGE_COLOR.toString());
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
                if (SubTaskLoadingBar.this.progress != progress) {
                    SubTaskLoadingBar.this.progress = progress;
                    renderThis();
                }
            }

            @Override
            public void message(String message) {
                if (!SubTaskLoadingBar.this.message.equals(message)) {
                    SubTaskLoadingBar.this.message = message;
                    renderThis();
                }
            }
        }
    }
}
