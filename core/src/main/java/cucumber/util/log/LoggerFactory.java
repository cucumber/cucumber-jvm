package cucumber.util.log;

import java.io.PrintStream;

public final class LoggerFactory {

    private static final long startTime = System.currentTimeMillis();

    private static ThreadLocal<Boolean> verbose = new ThreadLocal<Boolean>();

    static {
        verbose.set(true);
    }

    private static final Handler handler = new Handler() {
        private final String format = "%06d [%s] %s - %s%n";
        private PrintStream out = System.out;

        @Override
        public void publish(cucumber.util.log.LogRecord record) {
            if (!canPublish()) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            out.format(format,
                currentTime - startTime,
                Thread.currentThread().getName(),
                record.getLevel(),
                java.text.MessageFormat.format(record.getFormat(), record.getArguments()));

            Throwable t = record.getThrowable();
            if (t != null) {
                t.printStackTrace(out);
            }
        }

        @Override
        public boolean canPublish() {
            return verbose.get();
        }
    };

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz, handler);
    }

    public static void setVerbose(boolean verbose) {
        LoggerFactory.verbose.set(verbose);
    }
}
