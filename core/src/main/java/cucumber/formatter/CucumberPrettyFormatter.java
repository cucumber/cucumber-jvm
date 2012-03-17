package cucumber.formatter;

import gherkin.formatter.PrettyFormatter;

public class CucumberPrettyFormatter extends PrettyFormatter {
    public CucumberPrettyFormatter(Appendable out) {
        super(out, true, true);
    }
}
