package io.cucumber.core.backend;

public class StubLocation implements Located {

    private final String location;

    public StubLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public String getLocation() {
        return location;
    }

}
