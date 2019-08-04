package io.cucumber.core.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CompositeCucumberException extends CucumberException {

    private final List<Throwable> causes;

    public CompositeCucumberException(List<Throwable> causes) {
        super(String.format("There were %d exceptions:",
            Objects.isNull(causes) ? 0 : causes.size()
        ));
        this.causes = Objects.isNull(causes) ? new ArrayList<>() : causes;
    }

    public List<Throwable> getCauses() {
        return Collections.unmodifiableList(this.causes);
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        for (Throwable e : this.causes) {
            sb.append(String.format("\n  %s(%s)", e.getClass().getName(), e.getMessage()));
        }
        return sb.toString();
    }

}
