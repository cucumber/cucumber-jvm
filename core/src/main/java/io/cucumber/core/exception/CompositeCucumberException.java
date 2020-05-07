package io.cucumber.core.exception;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CompositeCucumberException extends CucumberException {

    private final List<Throwable> causes;

    public CompositeCucumberException(List<Throwable> causes) {
        super(String.format("There were %d exceptions:", causes.size()));
        this.causes = causes;
    }

    public List<Throwable> getCauses() {
        return Collections.unmodifiableList(this.causes);
    }

    public String getMessage() {
        return super.getMessage() + this.causes.stream()
                .map(e -> String.format("  %s(%s)", e.getClass().getName(), e.getMessage()))
                .collect(Collectors.joining("\n", "\n", ""));
    }

}
