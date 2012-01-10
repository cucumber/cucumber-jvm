package cucumber.formatter;

public class TestFormatterWithAppendable extends TestFormatter {
    public Appendable appendable;

    public TestFormatterWithAppendable(Appendable appendable) {
        this.appendable = appendable;
    }
}