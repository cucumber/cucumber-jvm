package cucumber.runtime.model;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import cucumber.runtime.Runtime;

public interface FeatureElement {
    public void run(Runtime runtime, Formatter formatter, Reporter reporter);
}
