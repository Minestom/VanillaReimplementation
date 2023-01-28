package net.minestom.vanilla.logging;

import java.util.Calendar;
import java.util.regex.Pattern;

record LoggerImpl(Level level) implements Logger {
    public static Level LOG_LEVEL = Level.INFO;
    static final LoggerImpl DEFAULT = new LoggerImpl(Level.INFO);
    private static boolean isNewLine = true;
    private static LoggerImpl lastLogger = DEFAULT;

    @Override
    public Logger level(Level level) {
        if (this.level == level) return this;
        return new LoggerImpl(level);
    }

    @Override
    public Logger print(String message) {
        if (LOG_LEVEL.ordinal() > level.ordinal()) return this;
        if ((!isNewLine) && (!lastLogger.equals(this))) {
            System.out.println();
            isNewLine = true;
        }
        if (isNewLine) {
            System.out.print(preparePrefix());
            isNewLine = false;
            lastLogger = this;
        }
        String[] lines = message.split(Pattern.quote(System.lineSeparator()), -1);
        if (lines.length == 1) {
            System.out.print(message);
            return this;
        }
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (i != 0) {
                System.out.println();
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
        return String.format("%s[%s/%s/%s %s:%s:%s]%s %s -> ",
                Color.GREEN,
                day, month, year,
                hours, minutes, seconds,
                Color.RESET,
                prepareLevelPrefix());
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

    enum Color {
        //Color end string, color reset
        RESET("\033[0m"),

        // Regular Colors. Normal color, no bold, background color etc.
        BLACK("\033[0;30m"),    // BLACK
        RED("\033[0;31m"),      // RED
        GREEN("\033[0;32m"),    // GREEN
        YELLOW("\033[0;33m"),   // YELLOW
        BLUE("\033[0;34m"),     // BLUE
        MAGENTA("\033[0;35m"),  // MAGENTA
        CYAN("\033[0;36m"),     // CYAN
        WHITE("\033[0;37m"),    // WHITE

        // Bold
        BLACK_BOLD("\033[1;30m"),   // BLACK
        RED_BOLD("\033[1;31m"),     // RED
        GREEN_BOLD("\033[1;32m"),   // GREEN
        YELLOW_BOLD("\033[1;33m"),  // YELLOW
        BLUE_BOLD("\033[1;34m"),    // BLUE
        MAGENTA_BOLD("\033[1;35m"), // MAGENTA
        CYAN_BOLD("\033[1;36m"),    // CYAN
        WHITE_BOLD("\033[1;37m"),   // WHITE

        // Underline
        BLACK_UNDERLINED("\033[4;30m"),     // BLACK
        RED_UNDERLINED("\033[4;31m"),       // RED
        GREEN_UNDERLINED("\033[4;32m"),     // GREEN
        YELLOW_UNDERLINED("\033[4;33m"),    // YELLOW
        BLUE_UNDERLINED("\033[4;34m"),      // BLUE
        MAGENTA_UNDERLINED("\033[4;35m"),   // MAGENTA
        CYAN_UNDERLINED("\033[4;36m"),      // CYAN
        WHITE_UNDERLINED("\033[4;37m"),     // WHITE

        // Background
        BLACK_BACKGROUND("\033[40m"),   // BLACK
        RED_BACKGROUND("\033[41m"),     // RED
        GREEN_BACKGROUND("\033[42m"),   // GREEN
        YELLOW_BACKGROUND("\033[43m"),  // YELLOW
        BLUE_BACKGROUND("\033[44m"),    // BLUE
        MAGENTA_BACKGROUND("\033[45m"), // MAGENTA
        CYAN_BACKGROUND("\033[46m"),    // CYAN
        WHITE_BACKGROUND("\033[47m"),   // WHITE

        // High Intensity
        BLACK_BRIGHT("\033[0;90m"),     // BLACK
        RED_BRIGHT("\033[0;91m"),       // RED
        GREEN_BRIGHT("\033[0;92m"),     // GREEN
        YELLOW_BRIGHT("\033[0;93m"),    // YELLOW
        BLUE_BRIGHT("\033[0;94m"),      // BLUE
        MAGENTA_BRIGHT("\033[0;95m"),   // MAGENTA
        CYAN_BRIGHT("\033[0;96m"),      // CYAN
        WHITE_BRIGHT("\033[0;97m"),     // WHITE

        // Bold High Intensity
        BLACK_BOLD_BRIGHT("\033[1;90m"),    // BLACK
        RED_BOLD_BRIGHT("\033[1;91m"),      // RED
        GREEN_BOLD_BRIGHT("\033[1;92m"),    // GREEN
        YELLOW_BOLD_BRIGHT("\033[1;93m"),   // YELLOW
        BLUE_BOLD_BRIGHT("\033[1;94m"),     // BLUE
        MAGENTA_BOLD_BRIGHT("\033[1;95m"),  // MAGENTA
        CYAN_BOLD_BRIGHT("\033[1;96m"),     // CYAN
        WHITE_BOLD_BRIGHT("\033[1;97m"),    // WHITE

        // High Intensity backgrounds
        BLACK_BACKGROUND_BRIGHT("\033[0;100m"),     // BLACK
        RED_BACKGROUND_BRIGHT("\033[0;101m"),       // RED
        GREEN_BACKGROUND_BRIGHT("\033[0;102m"),     // GREEN
        YELLOW_BACKGROUND_BRIGHT("\033[0;103m"),    // YELLOW
        BLUE_BACKGROUND_BRIGHT("\033[0;104m"),      // BLUE
        MAGENTA_BACKGROUND_BRIGHT("\033[0;105m"),   // MAGENTA
        CYAN_BACKGROUND_BRIGHT("\033[0;106m"),      // CYAN
        WHITE_BACKGROUND_BRIGHT("\033[0;107m");     // WHITE

        private final String code;

        Color(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }
}
