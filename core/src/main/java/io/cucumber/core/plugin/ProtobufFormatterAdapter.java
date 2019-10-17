package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

public class ProtobufFormatterAdapter implements ConcurrentEventListener {
    public static final String PROTOBUF_FORMATTER = "io.cucumber.core.gherkin8.formatter.ProtobufFormatter";
    private final EventListener delegate;

    public ProtobufFormatterAdapter(OutputStream out) {
        try {
            Class<EventListener> delegateClass = (Class<EventListener>) getClass().getClassLoader().loadClass(PROTOBUF_FORMATTER);
            this.delegate = delegateClass.getDeclaredConstructor(OutputStream.class).newInstance(out);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new CucumberException("Please add cucumber-gherkin8 to your classpath");
        }
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        delegate.setEventPublisher(publisher);
    }
}
