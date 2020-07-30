package io.cucumber.core.plugin;

import io.cucumber.messages.Messages;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import static io.cucumber.core.options.Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME;
import static java.util.Arrays.asList;

public final class NoPublishFormatter implements ConcurrentEventListener, ColorAware {

    private boolean monochrome = false;

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
        Banner banner = new Banner(System.err, monochrome);
        banner.print(
            asList(
                new Banner.Line("Share your Cucumber Report with your team at"),
                new Banner.Line("https://reports.cucumber.io", AnsiEscapes.CYAN, AnsiEscapes.INTENSITY_BOLD),
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
                    new Banner.Span("true", AnsiEscapes.CYAN))),
            AnsiEscapes.GREEN, AnsiEscapes.INTENSITY_BOLD);
    }
}
