package cucumber.util.log;

import static java.util.logging.Level.INFO;

public final class Logger {

    private final Class<?> clazz;
    private final Handler handler;

    public Logger(Class<?> clazz, Handler handler) {
        this.clazz = clazz;
        this.handler = handler;
    }

    public void info(String msg) {
        if (!handler.canPublish()) {
            return;
        }
        handler.publish(new LogRecord(INFO, msg));
    }

    public void info(String format, Object arg) {
        if (!handler.canPublish()) {
            return;
        }
        handler.publish(new LogRecord(INFO, format, arg));
    }

    public void info(String format, Object arg1, Object arg2) {
        if (!handler.canPublish()) {
            return;
        }
        handler.publish(new LogRecord(INFO, format, new Object[]{arg1, arg2}));
    }

    public void info(String format, Object[] argArray) {
        if (!handler.canPublish()) {
            return;
        }
        handler.publish(new LogRecord(INFO, format, argArray));
    }

    public void info(String msg, Throwable t) {
        if (!handler.canPublish()) {
            return;
        }
        handler.publish(new LogRecord(INFO, msg, t));
    }

}
