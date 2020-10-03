package io.cucumber.junit.platform.engine;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.WriteEvent;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.reporting.ReportEntry;

class ReportingEntryPublisher implements AutoCloseable {

    private final EventBus bus;

    private final EventHandler<WriteEvent> writeEvent = this::handleWriteEvent;
    private final EventHandler<EmbedEvent> embedEvent = this::handleEmbedEvent;
    private final EngineExecutionListener listener;
    private final TestDescriptor testDescriptor;

    static ReportingEntryPublisher report(TestDescriptor testDescriptor, EventBus bus, EngineExecutionListener listener) {
        return new ReportingEntryPublisher(testDescriptor, bus, listener);
    }

    public ReportingEntryPublisher(TestDescriptor testDescriptor, EventBus bus, EngineExecutionListener listener) {
        this.testDescriptor = testDescriptor;
        this.bus = bus;
        this.listener = listener;

        bus.registerHandlerFor(WriteEvent.class, writeEvent);
        bus.registerHandlerFor(EmbedEvent.class, embedEvent);
    }

    private void handleWriteEvent(WriteEvent event) {
        listener.reportingEntryPublished(testDescriptor, ReportEntry.from("Hello", "world"));
    }

    private void handleEmbedEvent(EmbedEvent event) {
        listener.reportingEntryPublished(testDescriptor, ReportEntry.from("World", "hello"));
    }

    @Override
    public void close() {
        bus.removeHandlerFor(WriteEvent.class, writeEvent);
        bus.removeHandlerFor(EmbedEvent.class, embedEvent);
    }

}
