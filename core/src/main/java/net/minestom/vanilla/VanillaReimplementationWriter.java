package net.minestom.vanilla;

import org.fusesource.jansi.Ansi;
import org.tinylog.Level;
import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.writers.Writer;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.fusesource.jansi.Ansi.ansi;

public class VanillaReimplementationWriter implements Writer {

    @Override
    public Collection<LogEntryValue> getRequiredLogEntryValues() {
        return Set.of(
                LogEntryValue.LEVEL, LogEntryValue.MESSAGE,
                LogEntryValue.CLASS, LogEntryValue.METHOD,
                LogEntryValue.THREAD, LogEntryValue.DATE
        );
    }

    private Ansi formatLog(Level log, Ansi ansi) {
        var name = log.name().toLowerCase();
        String append = " ".repeat(Arrays.stream(Level.values())
                .map(Level::name)
                .mapToInt(String::length)
                .sorted()
                .findFirst()
                .orElse(0) + 1);
        var paddedName = name + append;

        return switch (log) {
            case INFO -> ansi.fgBlue().a(paddedName).reset();
            case WARN -> ansi.fgYellow().a(paddedName).reset();
            case ERROR -> ansi.fgRed().a(paddedName).reset();
            case TRACE -> ansi.fgCyan().a(paddedName).reset();
            case DEBUG -> ansi.fgGreen().a(paddedName).reset();
            case OFF -> ansi;
        };
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    public void write(LogEntry logEntry) {
        if (logEntry.getLevel().ordinal() < Level.INFO.ordinal()) return;

        System.out.println(formatLog(logEntry.getLevel(), ansi().cursorToColumn(0).eraseLine(Ansi.Erase.ALL))
                .bg(Ansi.Color.BLACK).a(" " + dateFormat.format(logEntry.getTimestamp().toDate()) + " ").reset()
                .fgBlack().a(" [").reset()
                .a(logEntry.getThread().getName())
                .fgBlack().a("]").reset());
    }

    @Override
    public void flush() {
        System.out.flush();
    }

    @Override
    public void close() {
        System.out.println();
    }
}
