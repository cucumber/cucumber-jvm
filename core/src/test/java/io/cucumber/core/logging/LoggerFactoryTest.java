package io.cucumber.core.logging;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.Assert.assertThat;


public class LoggerFactoryTest {

    private final Exception exception = new Exception();
    private LogRecord logged;
    private Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);

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

    @Before
    public void setup() {
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
    public void error() {
        logger.error("Error");
        assertThat(logged, logRecord("Error", Level.SEVERE, null));
        logger.error("Error", exception);
        assertThat(logged, logRecord("Error", Level.SEVERE, exception));
    }

    @Test
    public void warn() {
        logger.warn("Warn");
        assertThat(logged, logRecord("Warn", Level.WARNING, null));
        logger.warn("Warn", exception);
        assertThat(logged, logRecord("Warn", Level.WARNING, exception));
    }

    @Test
    public void info() {
        logger.info("Info");
        assertThat(logged, logRecord("Info", Level.INFO, null));
        logger.info("Info", exception);
        assertThat(logged, logRecord("Info", Level.INFO, exception));
    }

    @Test
    public void config() {
        logger.config("Config");
        assertThat(logged, logRecord("Config", Level.CONFIG, null));
        logger.config("Config", exception);
        assertThat(logged, logRecord("Config", Level.CONFIG, exception));
    }


    @Test
    public void debug() {
        logger.debug("Debug");
        assertThat(logged, logRecord("Debug", Level.FINE, null));
        logger.debug("Debug", exception);
        assertThat(logged, logRecord("Debug", Level.FINE, exception));
    }

    @Test
    public void trace() {
        logger.trace("Trace");
        assertThat(logged, logRecord("Trace", Level.FINER, null));
        logger.trace("Trace", exception);
        assertThat(logged, logRecord("Trace", Level.FINER, exception));
    }

}
