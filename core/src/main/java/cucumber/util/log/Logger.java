package cucumber.util.log;


import java.util.logging.Level;

import static java.util.logging.Level.INFO;

public final class Logger {

    private final java.util.logging.Logger logger;

    Logger(java.util.logging.Logger logger) {
        this.logger = logger;
    }

    private void log(Level level, String format, Object arg1, Object arg2) {
        // Avoid potentially unnecessary array creation
        if (!logger.isLoggable(level)) {
            return;
        }
        logger.log(level, format, new Object[]{arg1, arg2});
    }

    private void log(Level level, String format, Object[] argArray) {
        logger.log(level, format, argArray);
    }

    private void log(Level level, String message, Throwable t) {
        logger.log(level, message, t);
    }

    public void info(String msg) {
        logger.log(INFO, msg);
    }

    public void info(String format, Object arg) {
        log(INFO, format, arg, null);
    }

    public void info(String format, Object arg1, Object arg2) {
        log(INFO, format, arg1, arg2);
    }

    public void info(String format, Object[] argArray) {
        log(INFO, format, argArray);
    }

    public void info(String msg, Throwable t) {
        log(INFO, msg, t);
    }

}
