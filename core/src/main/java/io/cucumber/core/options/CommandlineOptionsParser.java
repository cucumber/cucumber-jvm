package io.cucumber.core.options;

import java.util.Arrays;
import java.util.List;

public final class CommandlineOptionsParser {

    public RuntimeOptionsBuilder parse(List<String> args) {
        RuntimeOptionsParser parser = new RuntimeOptionsParser();
        return parser.parse(args);
    }

    public RuntimeOptionsBuilder parse(String... args) {
        return parse(Arrays.asList(args));
    }
}
