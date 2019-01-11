package io.cucumber.core.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static java.util.Objects.requireNonNull;

/**
 * Cucumber uses the Java Logging APIs from {@link java.util.logging} (JUL).
 * <p>
 * See the {@link java.util.logging.LogManager} for configuration options or use
 * the <a href="https://www.slf4j.org/legacy.html#jul-to-slf4j">JUL to SLF4J Bridge</a>
 */
public final class LoggerFactory {

    private LoggerFactory() {

    }

    /**
     * Get a {@link Logger}
     *
     * @param clazz the class for which to get the logger
     * @return the logger
     */
    public static Logger getLogger(Class<?> clazz) {
        requireNonNull(clazz, "Class must not be null");
        return new DelegatingLogger(clazz.getName());
    }

    private static final class DelegatingLogger implements Logger {

        private static final String THIS_LOGGER_CLASS = DelegatingLogger.class.getName();

        private final String name;

        private final java.util.logging.Logger julLogger;

        DelegatingLogger(String name) {
            this.name = name;
            this.julLogger = java.util.logging.Logger.getLogger(name);
        }

        @Override
        public void error(String message) {
            log(Level.SEVERE, null, message);
        }

        @Override
        public void error(String message, Throwable throwable) {
            log(Level.SEVERE, throwable, message);
        }

        @Override
        public void warn(String message) {
            log(Level.WARNING, null, message);
        }

        @Override
        public void warn(String message, Throwable throwable) {
            log(Level.WARNING, throwable, message);
        }

        @Override
        public void info(String message) {
            log(Level.INFO, null, message);
        }

        @Override
        public void info(String message, Throwable throwable) {
            log(Level.INFO, throwable, message);
        }

        @Override
        public void config(String message) {
            log(Level.CONFIG, null, message);
        }

        @Override
        public void config(String message, Throwable throwable) {
            log(Level.CONFIG, throwable, message);
        }

        @Override
        public void debug(String message) {
            log(Level.FINE, null, message);
        }

        @Override
        public void debug(String message, Throwable throwable) {
            log(Level.FINE, throwable, message);
        }

        @Override
        public void trace(String message) {
            log(Level.FINER, null, message);
        }

        @Override
        public void trace(String message, Throwable throwable) {
            log(Level.FINER, throwable, message);
        }

        private void log(Level level, Throwable throwable, String message) {
            boolean loggable = julLogger.isLoggable(level);
            if (loggable) {
                LogRecord logRecord = createLogRecord(level, throwable, message);
                julLogger.log(logRecord);
            }
        }

        private LogRecord createLogRecord(Level level, Throwable throwable, String message) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            String sourceClassName = null;
            String sourceMethodName = null;
            boolean found = false;
            for (StackTraceElement element : stack) {
                String className = element.getClassName();
                if (THIS_LOGGER_CLASS.equals(className)) {
                    found = true; // Next element is calling this logger
                } else if (found) {
                    sourceClassName = className;
                    sourceMethodName = element.getMethodName();
                    break;
                }
            }

            LogRecord logRecord = new LogRecord(level, message);
            logRecord.setLoggerName(name);
            logRecord.setThrown(throwable);
            logRecord.setSourceClassName(sourceClassName);
            logRecord.setSourceMethodName(sourceMethodName);
            logRecord.setResourceBundleName(julLogger.getResourceBundleName());
            logRecord.setResourceBundle(julLogger.getResourceBundle());

            return logRecord;
        }
    }
}
