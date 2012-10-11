package cucumber.runtime.formatter;

import gherkin.formatter.PrettyFormatter;

class CucumberPrettyFormatter extends PrettyFormatter implements ColorAware {
    public CucumberPrettyFormatter(Appendable out) {
        super(out, false, true);
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        super.setMonochrome(monochrome);
    }
}
