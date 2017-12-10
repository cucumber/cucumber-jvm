package cucumber.util.log;

import java.util.logging.Level;

class LogRecord {
    private final Level level;
    private final String format;
    private final Object[] args;
    private final Throwable t;

    LogRecord(Level level, String msg) {
        this(level, msg, new String[0]);
    }

    LogRecord(Level level, String format, Object[] args) {
        this(level, format, args, null);
    }

    LogRecord(Level level, String format, Object arg) {
        this(level, format, new Object[]{arg});

    }

    LogRecord(Level level, String msg, Throwable t) {
        this(level, msg, new Object[0], t);
    }

    LogRecord(Level level, String format, Object[] args, Throwable t) {
        this.level = level;
        this.format = format;
        this.args = args;
        this.t = t;
    }

    public Level getLevel() {
        return level;
    }

    public String getFormat() {
        return format;
    }

    public Object[] getArguments() {
        return args;
    }

    public Throwable getThrowable() {
        return t;
    }
}
