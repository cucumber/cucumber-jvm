package io.cucumber.core.plugin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class NoPublishFormatterTest {
    @Test
    public void should_print_banner() throws UnsupportedEncodingException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes);
        NoPublishFormatter noPublishFormatter = new NoPublishFormatter(out);
        noPublishFormatter.setMonochrome(true);
        noPublishFormatter.printBanner();
        assertThat(bytes.toString("UTF-8"), is("" +
                "┌───────────────────────────────────────────────────────────────────────────────┐\n" +
                "│ Share your Cucumber Report with your team at https://reports.cucumber.io      │\n" +
                "│                                                                               │\n" +
                "│ Code:                   @CucumberOptions(publish = true)                      │\n" +
                "│ Environment variable:   CUCUMBER_PLUGIN_PUBLISH_ENABLED=true                  │\n" +
                "│ System property:        -Dcucumber.plugin.publish.enabled=true                │\n" +
                "│                                                                               │\n" +
                "│ More information at https://reports.cucumber.io/docs/cucumber-jvm             │\n" +
                "│                                                                               │\n" +
                "│ To disable this message, add cucumber.plugin.publish.quiet=true to            │\n" +
                "│ src/test/resources/cucumber.properties or                                     │\n" +
                "│ src/test/resources/junit-platform.properties (cucumber-junit-platform-engine) │\n" +
                "└───────────────────────────────────────────────────────────────────────────────┘\n"));
    }

}
