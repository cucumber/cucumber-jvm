package cucumber.runtime.formatter;

public class FormatterBuilder {

    public static JSONFormatter jsonFormatter(Appendable out) {
        return new JSONFormatter(out);
    }
}
