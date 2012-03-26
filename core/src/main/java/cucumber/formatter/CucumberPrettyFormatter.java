package cucumber.formatter;

import gherkin.formatter.PrettyFormatter;

public class CucumberPrettyFormatter extends PrettyFormatter implements ColorAware {
    public CucumberPrettyFormatter(Appendable out) {
        super(out, false, true);
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        super.setMonochrome(monochrome);
    }
}
