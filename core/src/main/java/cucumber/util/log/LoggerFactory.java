package cucumber.util.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public final class LoggerFactory {

    private static final long startTime = System.currentTimeMillis();

    private static ThreadLocal<Boolean> verbose = new ThreadLocal<Boolean>();

    {
        verbose.set(true);
    }

    private static final Formatter logFormat = new SimpleFormatter() {

        private final String format = "%d [%s] %s %s - %s %s%n";

        @Override
        public String format(LogRecord record) {

            long currentTime = System.currentTimeMillis();
            Throwable t = record.getThrown();
            String throwable = "";
            if (t != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                record.getThrown().printStackTrace(pw);
                pw.close();
            }

            return String.format(format,
                currentTime - startTime,
                Thread.currentThread().getName(),
                record.getLevel(),
                record.getLoggerName(),
                formatMessage(record),
                throwable);
        }
    };

    private static final Handler verboseHandler = new ConsoleHandler() {
        {
            setOutputStream(System.out);
            setFormatter(logFormat);
        }

        @Override
        public boolean isLoggable(LogRecord record) {
            return verbose.get() && super.isLoggable(record);
        }
    };

    public static Logger getLogger(Class<?> clazz) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(clazz.getSimpleName());
        logger.addHandler(verboseHandler);
        return new Logger(logger);
    }

    public static void setVerbose(boolean verbose) {
        LoggerFactory.verbose.set(verbose);
    }
}
