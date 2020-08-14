package io.cucumber.core.plugin;

import io.cucumber.messages.Messages;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.PrintStream;

import static io.cucumber.core.options.Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME;
import static java.util.Arrays.asList;

public final class NoPublishFormatter implements ConcurrentEventListener, ColorAware {

    private final PrintStream out;
    private boolean monochrome = false;

    public NoPublishFormatter() {
        this(System.err);
    }

    NoPublishFormatter(PrintStream out) {
        this.out = out;
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Messages.Envelope.class, this::writeMessage);
    }

    private void writeMessage(Messages.Envelope envelope) {
        if (envelope.hasTestRunFinished()) {
            printBanner();
        }
    }

    void printBanner() {
        Banner banner = new Banner(out, monochrome);
        banner.print(
            asList(
                new Banner.Line(
                    new Banner.Span("Share your Cucumber Report with your team at "),
                    new Banner.Span("https://reports.cucumber.io", AnsiEscapes.CYAN, AnsiEscapes.INTENSITY_BOLD)),
                new Banner.Line(""),
                new Banner.Line(
                    new Banner.Span("Code:                   "),
                    new Banner.Span("@CucumberOptions", AnsiEscapes.CYAN),
                    new Banner.Span("(publish = "),
                    new Banner.Span("true", AnsiEscapes.CYAN),
                    new Banner.Span(")")),
                new Banner.Line(
                    new Banner.Span("Environment variable:   "),
                    new Banner.Span(PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME.toUpperCase().replace('.', '_'),
                        AnsiEscapes.CYAN),
                    new Banner.Span("="),
                    new Banner.Span("true", AnsiEscapes.CYAN)),
                new Banner.Line(
                    new Banner.Span("System property:        "),
                    new Banner.Span("-D" + PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, AnsiEscapes.CYAN),
                    new Banner.Span("="),
                    new Banner.Span("true", AnsiEscapes.CYAN)),
                new Banner.Line(""),
                new Banner.Line(
                    new Banner.Span("More information at "),
                    new Banner.Span("https://reports.cucumber.io/docs/cucumber-jvm", AnsiEscapes.CYAN)),
                new Banner.Line(""),
                new Banner.Line(
                    new Banner.Span("To disable this message, add "),
                    new Banner.Span(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, AnsiEscapes.INTENSITY_BOLD),
                    new Banner.Span("=", AnsiEscapes.INTENSITY_BOLD),
                    new Banner.Span("true", AnsiEscapes.INTENSITY_BOLD),
                    new Banner.Span(" to")),
                new Banner.Line(
                    new Banner.Span("src/test/resources/cucumber.properties", AnsiEscapes.INTENSITY_BOLD),
                    new Banner.Span(" or")),
                new Banner.Line(
                    new Banner.Span("src/test/resources/junit-platform.properties", AnsiEscapes.INTENSITY_BOLD),
                    new Banner.Span(" (cucumber-junit-platform-engine)"))),
            AnsiEscapes.GREEN, AnsiEscapes.INTENSITY_BOLD);
    }
}
