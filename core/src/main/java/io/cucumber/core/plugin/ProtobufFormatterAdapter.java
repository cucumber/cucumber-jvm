package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

public class ProtobufFormatterAdapter implements ConcurrentEventListener {
    private static final String PROTOBUF_FORMATTER = "io.cucumber.core.gherkin8.ProtobufFormatter";
    private final EventListener delegate;

    public ProtobufFormatterAdapter(File file) {
        try {
            Class<EventListener> delegateClass = (Class<EventListener>) getClass().getClassLoader().loadClass(PROTOBUF_FORMATTER);
            ProtobufFormat format = file.getPath().endsWith(".ndjson") ? ProtobufFormat.NDJSON : ProtobufFormat.PROTOBUF;
            OutputStream out = new FileOutputStream(file);
            this.delegate = delegateClass.getDeclaredConstructor(OutputStream.class, ProtobufFormat.class).newInstance(out, format);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new CucumberException("Please add cucumber-gherkin8 to your classpath", e);
        } catch (FileNotFoundException e) {
            throw new CucumberException(String.format("Could not write to %s", file.getAbsolutePath()), e);
        }
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        delegate.setEventPublisher(publisher);
    }
}
