package io.cucumber.core.plugin;

public class FormatterBuilder {

    public static JSONFormatter jsonFormatter(Appendable out) {
        return new JSONFormatter(out);
    }
}
