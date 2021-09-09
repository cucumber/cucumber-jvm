package io.cucumber.core.logging;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.hamcrest.MatcherAssert.assertThat;

class LoggerFactoryTest {

    private final Exception exception = new Exception();
    private final Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);
    private LogRecord logged;

    @BeforeEach
    void setup() {
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                logged = record;
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        };
        handler.setLevel(Level.ALL);

        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(LoggerFactoryTest.class.getName());
        julLogger.setLevel(Level.ALL);
        julLogger.addHandler(handler);
        // Suppress out put
        julLogger.setUseParentHandlers(false);
    }

    @Test
    void error() {
        logger.error(() -> "Error");
        assertThat(logged, logRecord("Error", Level.SEVERE, null));
        logger.error(exception, () -> "Error");
        assertThat(logged, logRecord("Error", Level.SEVERE, exception));
    }

    private static Matcher<LogRecord> logRecord(final String message, final Level level, final Throwable throwable) {
        return new TypeSafeDiagnosingMatcher<LogRecord>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("error=");
                description.appendValue(message);
                description.appendText(" level=");
                description.appendValue(level);
                description.appendText(" throwable=");
                description.appendValue(throwable);
            }

            @Override
            protected boolean matchesSafely(LogRecord logRecord, Description description) {
                description.appendText("error=");
                description.appendValue(logRecord.getMessage());
                description.appendText(" level=");
                description.appendValue(logRecord.getLevel());
                description.appendText(" throwable=");
                description.appendValue(logRecord.getThrown());

                return Objects.equals(logRecord.getMessage(), message)
                        && Objects.equals(logRecord.getLevel(), level)
                        && Objects.equals(logRecord.getThrown(), throwable);
            }
        };
    }

    @Test
    void warn() {
        logger.warn(() -> "Warn");
        assertThat(logged, logRecord("Warn", Level.WARNING, null));
        logger.warn(exception, () -> "Warn");
        assertThat(logged, logRecord("Warn", Level.WARNING, exception));
    }

    @Test
    void info() {
        logger.info(() -> "Info");
        assertThat(logged, logRecord("Info", Level.INFO, null));
        logger.info(exception, () -> "Info");
        assertThat(logged, logRecord("Info", Level.INFO, exception));
    }

    @Test
    void config() {
        logger.config(() -> "Config");
        assertThat(logged, logRecord("Config", Level.CONFIG, null));
        logger.config(exception, () -> "Config");
        assertThat(logged, logRecord("Config", Level.CONFIG, exception));
    }

    @Test
    void debug() {
        logger.debug(() -> "Debug");
        assertThat(logged, logRecord("Debug", Level.FINE, null));
        logger.debug(exception, () -> "Debug");
        assertThat(logged, logRecord("Debug", Level.FINE, exception));
    }

    @Test
    void trace() {
        logger.trace(() -> "Trace");
        assertThat(logged, logRecord("Trace", Level.FINER, null));
        logger.trace(exception, () -> "Trace");
        assertThat(logged, logRecord("Trace", Level.FINER, exception));
    }

}
