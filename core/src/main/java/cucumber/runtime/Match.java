package cucumber.runtime;

import java.util.Collections;
import java.util.List;

public class Match {
    private static final long serialVersionUID = 1L;

    private final List<Argument> arguments;
    private final String location;
    public static final Match UNDEFINED = new Match(Collections.<Argument>emptyList(), null);

    public Match(List<Argument> arguments, String location) {
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
