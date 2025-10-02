package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.query.Query;
import io.cucumber.query.Repository;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Function;

import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_GHERKIN_DOCUMENTS;
import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_STEP_DEFINITIONS;

/**
 * Formatter to measure performance of steps. Includes average and median step
 * duration.
 */
public final class UsageFormatter implements Plugin, ConcurrentEventListener {

    // TODO: Make uri formatter configurable.
    public final Function<String, String> uriFormatter = s -> s;
    private final Repository repository = Repository.builder()
            .feature(INCLUDE_GHERKIN_DOCUMENTS, true)
            .feature(INCLUDE_STEP_DEFINITIONS, true)
            .build();
    private final Query query = new Query(repository);
    private final UTF8OutputStreamWriter out;
    
    /**
     * Constructor
     *
     * @param out {@link Appendable} to print the result
     */
    @SuppressWarnings("WeakerAccess") // Used by PluginFactory
    public UsageFormatter(OutputStream out) {
        this.out = new UTF8OutputStreamWriter(out);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, envelope -> {
            repository.update(envelope);
            envelope.getTestRunFinished().ifPresent(testRunFinished -> {
                List<UsageReportWriter.StepDefinitionUsage> usageReport = new UsageReportWriter(query, uriFormatter).createUsageReport();

                try {
                    Jackson.OBJECT_MAPPER.writeValue(out, usageReport);
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                
            });
        });

    }

}
