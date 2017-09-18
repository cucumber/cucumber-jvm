package cucumber.util.log;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public final class LoggerFactory {

    private transient static boolean verbose = false;

    private static final Handler verboseHandler = new ConsoleHandler() {
        {
            setOutputStream(System.out);
        }

        @Override
        public boolean isLoggable(LogRecord record) {
            return verbose && super.isLoggable(record);
        }
    };

    public static Logger getLogger(Class<?> clazz) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(clazz.getName());
        logger.addHandler(verboseHandler);
        return new Logger(logger);
    }

    public static void setVerbose(boolean verbose) {
        LoggerFactory.verbose = verbose;
    }
}
