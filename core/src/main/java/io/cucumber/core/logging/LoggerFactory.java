package io.cucumber.core.logging;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static java.util.Objects.requireNonNull;

/**
 * Cucumber uses the Java Logging APIs from {@link java.util.logging} (JUL).
 * <p>
 * See the {@link java.util.logging.LogManager} for configuration options or use
 * the <a href="https://www.slf4j.org/legacy.html#jul-to-slf4j">JUL to SLF4J
 * Bridge</a>
 */
public final class LoggerFactory {

    private static final ConcurrentLinkedDeque<LogRecordListener> listeners = new ConcurrentLinkedDeque<>();

    private LoggerFactory() {

    }

    public static void addListener(LogRecordListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(LogRecordListener listener) {
        listeners.remove(listener);
    }

    /**
     * Get a {@link Logger}
     *
     * @param  clazz the class for which to get the logger
     * @return       the logger
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
        public void error(Supplier<String> message) {
            log(Level.SEVERE, null, message);
        }

        @Override
        public void error(Throwable throwable, Supplier<String> message) {
            log(Level.SEVERE, throwable, message);
        }

        @Override
        public void warn(Supplier<String> message) {
            log(Level.WARNING, null, message);
        }

        @Override
        public void warn(Throwable throwable, Supplier<String> message) {
            log(Level.WARNING, throwable, message);
        }

        @Override
        public void info(Supplier<String> message) {
            log(Level.INFO, null, message);
        }

        @Override
        public void info(Throwable throwable, Supplier<String> message) {
            log(Level.INFO, throwable, message);
        }

        @Override
        public void config(Supplier<String> message) {
            log(Level.CONFIG, null, message);
        }

        @Override
        public void config(Throwable throwable, Supplier<String> message) {
            log(Level.CONFIG, throwable, message);
        }

        @Override
        public void debug(Supplier<String> message) {
            log(Level.FINE, null, message);
        }

        @Override
        public void debug(Throwable throwable, Supplier<String> message) {
            log(Level.FINE, throwable, message);
        }

        @Override
        public void trace(Supplier<String> message) {
            log(Level.FINER, null, message);
        }

        @Override
        public void trace(Throwable throwable, Supplier<String> message) {
            log(Level.FINER, throwable, message);
        }

        private void log(Level level, Throwable throwable, Supplier<String> message) {
            boolean loggable = julLogger.isLoggable(level);
            if (loggable || !listeners.isEmpty()) {
                LogRecord logRecord = createLogRecord(level, throwable, message);
                julLogger.log(logRecord);
                for (LogRecordListener listener : listeners) {
                    listener.logRecordSubmitted(logRecord);
                }
            }
        }

        private LogRecord createLogRecord(Level level, Throwable throwable, Supplier<String> message) {
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

            LogRecord logRecord = new LogRecord(level, message == null ? null : message.get());
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
