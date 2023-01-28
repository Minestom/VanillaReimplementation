package net.minestom.vanilla.logging;

import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.regex.Pattern;

record LoggerImpl(Level level) implements Logger {

    static final LoggerImpl DEFAULT = new LoggerImpl(Level.INFO);
    public static Level LOG_LEVEL = Level.INFO;
    private static boolean isNewLine = true;
    private static boolean isFreshLine = false;
    private static LoggerImpl lastLogger = DEFAULT;

    @Override
    public Logger level(Level level) {
        if (this.level == level) return this;
        return new LoggerImpl(level);
    }

    private void consolePrint(String str) {
        System.out.print(str);
    }

    private void resetLine() {
        consolePrint("\r" + preparePrefix());
        isFreshLine = true;
        isNewLine = false;
        lastLogger = this;
    }

    @Override
    public Logger print(String message) {
        if (LOG_LEVEL.ordinal() < level.ordinal()) return this;
        if (message.isEmpty()) return this;
        if ((!isNewLine) && (!lastLogger.equals(this))) {
            consolePrint(System.lineSeparator());
            isNewLine = true;
            isFreshLine = false;
        }
        if (isNewLine) resetLine();
        String[] lines = message.split(Pattern.quote(System.lineSeparator()), -1);
        if (lines.length == 1) {
            if (message.contains("\r")) {
                int lastIndex = message.lastIndexOf("\r");
                message = message.substring(lastIndex + 1);
                resetLine();
            }
            consolePrint(message);
            isFreshLine = false;
            return this;
        }
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (i != 0) {
                consolePrint(System.lineSeparator());
                isFreshLine = false;
                isNewLine = true;
                continue;
            }
            print(line);
        }
        return this;
    }

    private String preparePrefix() {
        Calendar date = Calendar.getInstance();
        int seconds = date.get(Calendar.SECOND);
        int minutes = date.get(Calendar.MINUTE);
        int hours = date.get(Calendar.HOUR_OF_DAY);
        int day = date.get(Calendar.DAY_OF_MONTH);
        int month = date.get(Calendar.MONTH) + 1;
        int year = date.get(Calendar.YEAR);
        return String.format("%s[%s/%s/%s %s:%s:%02d]%s %s -> ",
                Color.GREEN,
                day, month, year,
                hours, minutes, seconds,
                Color.RESET,
                prepareLevelPrefix());
    }

    @Override
    public Logger nextLine() {
        if (LOG_LEVEL.ordinal() > level.ordinal()) return this;
        if (isFreshLine) return this;
        if (!isNewLine) consolePrint(System.lineSeparator());
        resetLine();
        return this;
    }

    private String prepareLevelPrefix() {
        Color color = switch (level) {
            case DEBUG -> Color.BLUE;
            case SETUP -> Color.MAGENTA;
            case INFO -> Color.CYAN;
            case WARN -> Color.YELLOW;
            case ERROR -> Color.RED_BOLD_BRIGHT;
        };
        return String.format("%s(%s)%s", color, level, Color.RESET);
    }

    @Override
    public Logger printf(String message, Object... args) {
        return print(String.format(message, args));
    }

    @Override
    public Logger throwable(Throwable throwable, Object... args) {
        String info = "";
        if (args.length == 1) {
            info = " -> (" + args[0] + ")";
        } else if (args.length > 1) {
            info = " -> (" + String.format(args[0].toString(), args[1]) + ")";
        }
        return print(throwable.getMessage() + info);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof LoggerImpl other)) return false;
        return level == other.level;
    }
}
