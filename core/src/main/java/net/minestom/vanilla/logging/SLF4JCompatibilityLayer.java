package net.minestom.vanilla.logging;

import org.slf4j.Marker;
import org.slf4j.helpers.AbstractLogger;

class SLF4JCompatibilityLayer extends AbstractLogger implements org.slf4j.Logger {

    private final String name;
    public SLF4JCompatibilityLayer(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return "[vri] " + name;
    }

    @Override
    public boolean isTraceEnabled() {
        return Logger.logger().level().ordinal() <= Level.TRACE.ordinal();
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return Logger.logger().level().ordinal() <= Level.DEBUG.ordinal();
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return Logger.logger().level().ordinal() <= Level.INFO.ordinal();
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return Logger.logger().level().ordinal() <= Level.WARN.ordinal();
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return Logger.logger().level().ordinal() <= Level.ERROR.ordinal();
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isErrorEnabled();
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

    private Level fromSlf4jLevel(org.slf4j.event.Level level) {
        return switch (level) {
            case TRACE -> Level.TRACE;
            case DEBUG -> Level.DEBUG;
            case INFO -> Level.INFO;
            case WARN -> Level.WARN;
            case ERROR -> Level.ERROR;
        };
    }

    @Override
    protected void handleNormalizedLoggingCall(org.slf4j.event.Level level, Marker marker, String s, Object[] objects, Throwable throwable) {
        // TODO: Marker support?
        Level minestomLevel = fromSlf4jLevel(level);
        String message = org.slf4j.helpers.MessageFormatter.arrayFormat(s, objects).getMessage();
        Logger.logger().level(minestomLevel).println(message);
    }
}
