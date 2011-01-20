package cucumber.runtime;

import gherkin.formatter.Formatter;
import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.model.*;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;

public class ExecuteFormatterTest {
    @Test
    public void shouldPrintUndefinedSteps() {
        StringWriter out = new StringWriter();
        PrettyFormatter pf = new PrettyFormatter(out, true, true);
        Backend backend = mock(Backend.class);
        Formatter ef = new ExecuteFormatter(Arrays.asList(backend), pf);

        Step step = new Step(Collections.<Comment>emptyList(), "Given ", "this is undefined", 10);
        Scenario scenario = new Scenario(Collections.<Comment>emptyList(), Collections.<Tag>emptyList(), "Scenario", "foo", "", 9);

        ef.uri("some_feature.feature");
        ef.feature(new Feature(Collections.<Comment>emptyList(), Collections.<Tag>emptyList(), "Feature", "Foo", "", 1));
        ef.scenario(scenario);
        ef.step(step);

        System.out.println(out.getBuffer());
    }

}
