package cucumber.runtime;

import io.cucumber.stepexpression.Argument;

import java.util.Collections;
import java.util.List;

// public for testing with mockito.
public class Match {

    private final List<Argument> arguments;
    private final String location;
    public static final Match UNDEFINED = new Match(Collections.<Argument>emptyList(), null);

    Match(List<Argument> arguments, String location) {
        if(arguments == null) throw new NullPointerException("argument may not be null");
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
