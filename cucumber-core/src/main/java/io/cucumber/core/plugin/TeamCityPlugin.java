package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.teamcityformatter.MessagesToTeamCityWriter;

import java.io.IOException;
import java.io.PrintStream;

import static io.cucumber.teamcityformatter.MessagesToTeamCityWriter.TeamCityFeature.PRINT_TEST_CASES_AFTER_TEST_RUN;

/**
 * Outputs Teamcity services messages to std out.
 *
 * @see <a
 *      href=https://www.jetbrains.com/help/teamcity/service-messages.html>TeamCity
 *      - Service Messages</a>
 */
public class TeamCityPlugin implements ConcurrentEventListener {

    private final PrintStream out;
    private MessagesToTeamCityWriter writer;

    @SuppressWarnings("unused") // Used by PluginFactory
    public TeamCityPlugin() {
        // This plugin prints markers for Team City and IntelliJ IDEA that
        // allows them to associate the output to specific test cases. Printing
        // to system out - and potentially mixing with other formatters - is
        // intentional.
        this(System.out);
    }

    TeamCityPlugin(PrintStream out) {
        this.out = out;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        setEventPublisher(publisher, true);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher, boolean isMultiThreaded) {
        this.writer = MessagesToTeamCityWriter.builder()
                .feature(PRINT_TEST_CASES_AFTER_TEST_RUN, isMultiThreaded)
                .build(out);
        publisher.registerHandlerFor(Envelope.class, this::write);
    }

    private void write(Envelope event) {
        try {
            writer.write(event);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
