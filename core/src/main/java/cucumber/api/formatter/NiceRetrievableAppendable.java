package cucumber.api.formatter;

public class NiceRetrievableAppendable extends NiceAppendable {

    private final StringBuilder builder;

    public NiceRetrievableAppendable(StringBuilder out) {
        super(out);
        this.builder = out;
    }

    public String printAll() {
        return builder.toString();
    }
}
