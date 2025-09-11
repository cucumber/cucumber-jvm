package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Snippet;
import io.cucumber.messages.types.Suggestion;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.SnippetsSuggestedEvent;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.query.Query;
import io.cucumber.query.Repository;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_GHERKIN_DOCUMENT;
import static io.cucumber.query.Repository.RepositoryFeature.INCLUDE_SUGGESTIONS;

public final class DefaultSummaryPrinter implements ColorAware, ConcurrentEventListener {

    private final Repository repository = Repository.builder()
            .feature(INCLUDE_GHERKIN_DOCUMENT, true)
            .feature(INCLUDE_SUGGESTIONS, true)
            .build();
    private final Query query = new Query(repository);

    private final Stats stats;
    private final PrintStream out;

    public DefaultSummaryPrinter() {
        this(System.out, Locale.getDefault());
    }

    DefaultSummaryPrinter(OutputStream out, Locale locale) {
        this.out = new PrintStream(out);
        this.stats = new Stats(query, locale);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, repository::update);
        publisher.registerHandlerFor(TestRunFinished.class, event -> print());
    }

    private void print() {
        out.println();
        printStats();
        printErrors();
        printSnippets();
        out.println();
    }

    private void printStats() {
        stats.printStats(out);
        out.println();
    }

    private void printErrors() {
        List<Throwable> errors = stats.getErrors();
        if (errors.isEmpty()) {
            return;
        }
        out.println();
        for (Throwable error : errors) {
            error.printStackTrace(out);
            out.println();
        }
    }

    private void printSnippets() {
        Set<Snippet> snippets = query.findAllTestCaseFinished().stream()
                .map(query::findPickleBy)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(query::findSuggestionsBy)
                .flatMap(Collection::stream)
                .map(Suggestion::getSnippets)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (snippets.isEmpty()) {
            return;
        }

        out.println();
        out.println("You can implement missing steps with the snippets below:");
        out.println();
        for (Snippet snippet : snippets) {
            out.println(snippet.getCode());
            out.println();
        }
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        stats.setMonochrome(monochrome);
    }

}
