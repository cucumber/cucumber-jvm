package io.cucumber.java8;

import io.cucumber.core.backend.TestCaseState;
import org.apiguardian.api.API;

import java.util.Collection;

@API(status = API.Status.STABLE)
public final class Scenario {

    private final TestCaseState delegate;

    Scenario(TestCaseState delegate) {
        this.delegate = delegate;
    }

    public Collection<String> getSourceTagNames() {
        return delegate.getSourceTagNames();
    }

    public Status getStatus() {
        return Status.valueOf(delegate.getStatus().name());
    }

    public boolean isFailed() {
        return delegate.isFailed();
    }

    @Deprecated
    public void embed(byte[] data, String mimeType) {
        delegate.embed(data, mimeType);
    }

    public void embed(byte[] data, String mimeType, String name) {
        delegate.embed(data, mimeType, name);
    }

    public void write(String text) {
        delegate.write(text);
    }

    public String getName() {
        return delegate.getName();
    }

    public String getId() {
        return delegate.getId();
    }

    public String getUri() {
        return delegate.getUri();
    }

    public Integer getLine() {
        return delegate.getLine();
    }
}
