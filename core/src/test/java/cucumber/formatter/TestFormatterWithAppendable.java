package cucumber.formatter;

import org.junit.Ignore;

@Ignore
public class TestFormatterWithAppendable extends TestFormatter {
    public Appendable appendable;

    public TestFormatterWithAppendable(Appendable appendable) {
        this.appendable = appendable;
    }
}
