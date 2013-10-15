package cucumber.runtime.android;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.os.Bundle;

@RunWith(RobolectricTestRunner.class)
public class InstrumentationArgumentsTest {
    @Test
    public void nullArguments() {
        InstrumentationArguments parser = new InstrumentationArguments(null);
        String cucumberOptions = parser.getCucumberOptionsString();
        assertThat(cucumberOptions, is(""));
    }

    @Test
    public void emptyArguments() {
        InstrumentationArguments parser = new InstrumentationArguments(new Bundle());
        String cucumberOptions = parser.getCucumberOptionsString();
        assertThat(cucumberOptions, is(""));
    }

    @Test
    public void booleanCucumberOptionArgument() {
        Bundle arguments = new Bundle();
        arguments.putString("dryRun", "true");
        InstrumentationArguments parser = new InstrumentationArguments(arguments);
        String cucumberOptions = parser.getCucumberOptionsString();
        assertThat(cucumberOptions, is("--dry-run"));
    }

    @Test
    public void stringCucumberOptionArgument() {
        Bundle arguments = new Bundle();
        arguments.putString("name", "SomeFeature");
        InstrumentationArguments parser = new InstrumentationArguments(arguments);
        String cucumberOptions = parser.getCucumberOptionsString();
        assertThat(cucumberOptions, is("--name SomeFeature"));
    }

    @Test
    public void multiCucumberOptionArgument() {
        Bundle arguments = new Bundle();
        arguments.putString("name", "Feature1--Feature2");
        InstrumentationArguments parser = new InstrumentationArguments(arguments);
        String cucumberOptions = parser.getCucumberOptionsString();
        assertThat(cucumberOptions, is("--name Feature1 --name Feature2"));
    }

    @Test
    public void cucumberOptionsSingleString() {
        String cucumberOptions = "--tags @mytag --monochrome --name MyFeature --dry-run --glue com.someglue.Glue --format pretty --snippets underscore --strict --dotcucumber test features";
        Bundle arguments = new Bundle();
        arguments.putString("cucumberOptions", cucumberOptions);
        InstrumentationArguments parser = new InstrumentationArguments(arguments);
        assertThat(parser.getCucumberOptionsString(), is(cucumberOptions));
    }

    @Test
    public void cucumberOptionsSingleStringPrecedence() {
        String cucumberOptions = "--tags @mytag1";
        Bundle arguments = new Bundle();
        arguments.putString("cucumberOptions", cucumberOptions);
        arguments.putString("tags", "@mytag2");
        InstrumentationArguments parser = new InstrumentationArguments(arguments);
        assertThat(parser.getCucumberOptionsString(), is(cucumberOptions));
    }

    @Test
    public void allArguments() {
        Bundle arguments = new Bundle();
        arguments.putString("glue", "com.package.Glue");
        arguments.putString("format", "pretty");
        arguments.putString("tags", "@mytag");
        arguments.putString("name", "MyFeature");
        arguments.putString("dryRun", "true");
        arguments.putString("monochrome", "true");
        arguments.putString("strict", "true");
        arguments.putString("snippets", "underscore");
        arguments.putString("dotcucumber", "test");
        arguments.putString("features", "features");
        InstrumentationArguments parser = new InstrumentationArguments(arguments);

        String cucumberOptions = parser.getCucumberOptionsString();

        assertThat(cucumberOptions, is("--tags @mytag --monochrome --name MyFeature --dry-run --glue com.package.Glue --format pretty --snippets underscore --strict --dotcucumber test features"));
    }

    @Test
    public void argumentValueWithSpaces() {
        Bundle arguments = new Bundle();
        arguments.putString("name", "'Name with spaces'");
        InstrumentationArguments parser = new InstrumentationArguments(arguments);
        String cucumberOptions = parser.getCucumberOptionsString();
        assertThat(cucumberOptions, is("--name 'Name with spaces'"));
    }
}
