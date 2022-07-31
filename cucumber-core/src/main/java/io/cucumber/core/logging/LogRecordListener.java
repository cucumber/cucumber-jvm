package io.cucumber.core.logging;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.LogRecord;

public final class LogRecordListener {

    private final ConcurrentLinkedDeque<LogRecord> logRecords = new ConcurrentLinkedDeque<>();

    void logRecordSubmitted(LogRecord logRecord) {
        logRecords.add(logRecord);
    }

    public List<LogRecord> getLogRecords() {
        return Arrays.asList(logRecords.toArray(new LogRecord[0]));
    }

}
