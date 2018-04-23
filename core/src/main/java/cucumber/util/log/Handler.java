package cucumber.util.log;

interface Handler {

    void publish(LogRecord record);

    boolean canPublish();
}
