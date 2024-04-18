package io.cucumber.core.backend;

import java.util.Optional;

public class StubLocation implements Located {

    private final String location;
    private final SourceReference sourceReference;

    public StubLocation(String location) {
        this.location = location;
        this.sourceReference = null;
    }

    public StubLocation(SourceReference location) {
        this.location = null;
        this.sourceReference = location;
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public Optional<SourceReference> getSourceReference() {
        return Optional.ofNullable(sourceReference);
    }

    @Override
    public String getLocation() {
        return location;
    }

}
