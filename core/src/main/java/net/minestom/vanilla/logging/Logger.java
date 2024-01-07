package net.minestom.vanilla.logging;

public interface Logger {
    static Logger logger() {
        return LoggerImpl.DEFAULT;
    }

    /**
     * Debug log entries contain common debug information.
     */
    static Logger debug() {
        return logger().level(Level.DEBUG);
    }
    static Logger debug(String message, Object... args) {
        if (args.length == 0) return debug().println(message);
        return debug().printf(message, args).println();
    }
    static Logger debug(Throwable throwable, Object... args) {
        return debug().throwable(throwable, args);
    }

    /**
     * Setup log entries contain information about the setup of the application.
     */
    static Logger setup() {
        return logger().level(Level.SETUP);
    }
    static Logger setup(String message, Object... args) {
        if (args.length == 0) return setup().println(message);
        return setup().printf(message, args);
    }
    static Logger setup(Throwable throwable, Object... args) {
        return setup().throwable(throwable, args);
    }

    /**
     * Info log entries contain important relevant information.
     */
    static Logger info() {
        return logger().level(Level.INFO);
    }
    static Logger info(String message, Object... args) {
        if (args.length == 0) return info().println(message);
        return info().printf(message, args).println();
    }
    static Logger info(Throwable throwable, Object... args) {
        return info().throwable(throwable, args);
    }

    /**
     * Warn log entries contain technical warnings. Typically, warnings do not prevent the application from continuing.
     */
    static Logger warn() {
        return logger().level(Level.WARN);
    }
    static Logger warn(String message, Object... args) {
        if (args.length == 0) return warn().println(message);
        return warn().printf(message, args);
    }
    static Logger warn(Throwable throwable, Object... args) {
        return warn().throwable(throwable, args);
    }

    /**
     * Error log entries contain technical errors. Errors WILL stop the application from continuing.
     */
    static Logger error() {
        return logger().level(Level.ERROR);
    }
    static Logger error(String message, Object... args) {
        if (args.length == 0) return error().println(message);
        return error().printf(message, args);
    }
    static Logger error(Throwable throwable, Object... args) {
        return error().throwable(throwable, args);
    }

    /**
     * Set the level of the logger
     * @param level the level
     * @return the logger
     */
    Logger level(Level level);

    /**
     * Gets the current level of the logger
     */
    Level level();

    Logger print(String message);
    default Logger println(String message) {
        return print(message).println();
    }
    default Logger println() {
        return print(System.lineSeparator());
    }
    Logger printf(String message, Object... args);
    Logger throwable(Throwable throwable, Object... args);

    /**
     * Ensures that this logger is ready to print to a blank fresh line.
     * @return the logger
     */
    Logger nextLine();
}
