package io.cucumber.core.runner;

import io.cucumber.core.event.EmbedEvent;
import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import io.cucumber.core.event.TestCase;
import io.cucumber.core.event.WriteEvent;
import gherkin.pickles.PickleTag;
import io.cucumber.core.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.max;
import static java.util.Comparator.comparing;

class Scenario implements io.cucumber.core.api.Scenario {

    private final List<Result> stepResults = new ArrayList<>();
    private final EventBus bus;
    private final TestCase testCase;

    Scenario(EventBus bus, io.cucumber.core.event.TestCase testCase) {
        this.bus = bus;
        this.testCase = testCase;
    }

    void add(Result result) {
        stepResults.add(result);
    }

    @Override
    public Status getStatus() {
        if (stepResults.isEmpty()) {
            return Status.UNDEFINED;
        }

        return max(stepResults, comparing(Result::getStatus)).getStatus();
    }

    @Override
    public boolean isFailed() {
        return getStatus() == Status.FAILED;
    }

    @Override
    public void embed(byte[] data, String mimeType) {
        bus.send(new EmbedEvent(bus.getInstant(), testCase, data, mimeType));
    }

    @Override
    public void embed(byte[] data, String mimeType, String name) {
        bus.send(new EmbedEvent(bus.getInstant(), testCase, data, mimeType, name));
    }

    @Override
    public void write(String text) {
        bus.send(new WriteEvent(bus.getInstant(), testCase, text));
    }

    @Override
    public String getName() {
        return testCase.getName();
    }

    @Override
    public String getId() {
        return testCase.getUri() + ":" + getLine();
    }

    @Override
    public String getUri() {
        return testCase.getUri();
    }

    @Override
    public Integer getLine() {
        return testCase.getLine();
    }

    Throwable getError() {
        if (stepResults.isEmpty()) {
            return null;
        }

        return max(stepResults, comparing(Result::getStatus)).getError();
    }
}
