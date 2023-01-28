package net.minestom.vanilla.logging;

public interface Logger {
    static void setLogLevel(Level level) {
        LoggerImpl.LOG_LEVEL = level;
    }

    static Logger logger() {
        return LoggerImpl.DEFAULT;
    }

    /**
     * Debug log entries contain common debug information.
     */
    default Logger debug() {
        return level(Level.DEBUG);
    }
    static Logger debug(String message, Object... args) {
        if (args.length == 0) return logger().debug().println(message);
        return logger().debug().printf(message, args).println();
    }
    static Logger debug(Throwable throwable, Object... args) {
        return logger().debug().throwable(throwable, args);
    }

    /**
     * Setup log entries contain information about the setup of the application.
     */
    default Logger setup() {
        return level(Level.SETUP);
    }
    static Logger setup(String message, Object... args) {
        if (args.length == 0) return logger().setup().println(message);
        return logger().setup().printf(message, args).println();
    }
    static Logger setup(Throwable throwable, Object... args) {
        return logger().setup().throwable(throwable, args);
    }

    /**
     * Info log entries contain important relevant information.
     */
    default Logger info() {
        return level(Level.INFO);
    }
    static Logger info(String message, Object... args) {
        if (args.length == 0) return logger().info().println(message);
        return logger().info().printf(message, args);
    }
    static Logger info(Throwable throwable, Object... args) {
        return logger().info().throwable(throwable, args);
    }

    /**
     * Warn log entries contain technical warnings. Typically, warnings do not prevent the application from continuing.
     */
    default Logger warn() {
        return level(Level.WARN);
    }
    static Logger warn(String message, Object... args) {
        if (args.length == 0) return logger().warn().println(message);
        return logger().warn().printf(message, args).println();
    }
    static Logger warn(Throwable throwable, Object... args) {
        return logger().warn().throwable(throwable, args);
    }

    /**
     * Error log entries contain technical errors. Errors WILL stop the application from continuing.
     */
    default Logger error() {
        return level(Level.ERROR);
    }
    static Logger error(String message, Object... args) {
        if (args.length == 0) return logger().error().println(message);
        return logger().error().printf(message, args).println();
    }
    static Logger error(Throwable throwable, Object... args) {
        return logger().error().throwable(throwable, args);
    }

    /**
     * Set the level of the logger
     * @param level the level
     * @return the logger
     */
    Logger level(Level level);

    Logger print(String message);
    default Logger println(String message) {
        return print(message).println();
    }
    default Logger println() {
        return print(System.lineSeparator());
    }
    Logger printf(String message, Object... args);
    Logger throwable(Throwable throwable, Object... args);
}
