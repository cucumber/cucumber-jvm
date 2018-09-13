package io.cucumber.core.api.event;

public final class WriteEvent extends TestCaseEvent {
    public final String text;

    public WriteEvent(Long timeStamp, TestCase testCase, String text) {
        super(timeStamp, testCase);
        this.text = text;
    }
}
