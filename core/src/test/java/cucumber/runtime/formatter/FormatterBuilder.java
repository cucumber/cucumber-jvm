package cucumber.runtime.formatter;

import cucumber.util.TimeUtils;

public class FormatterBuilder {

    public static JSONFormatter jsonFormatter(Appendable out) {
        return new JSONFormatter(out);
    }
    
    public static JSONFormatter jsonFormatter(Appendable out, TimeUtils timeUtils) {
        return new JSONFormatter(out, timeUtils);
    }
}
