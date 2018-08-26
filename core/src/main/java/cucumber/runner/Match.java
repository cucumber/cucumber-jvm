package cucumber.runner;

import io.cucumber.stepexpression.Argument;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

class Match {

    private final List<Argument> arguments;
    private final String location;
    public static final Match UNDEFINED = new Match(Collections.<Argument>emptyList(), null);

    Match(List<Argument> arguments, String location) {
        requireNonNull(arguments, "argument may not be null");
        this.arguments = arguments;
        this.location = location;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public String getLocation() {
        return location;
    }

}
