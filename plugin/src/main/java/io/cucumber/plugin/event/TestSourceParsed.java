package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Provides abstract representation of a parsed test source.
 * <p>
 * Cucumber scenarios and individual examples in a scenario outline are compiled
 * into pickles. These pickles are wrapped by a {@link TestCase}. As such
 * Cucumbers internal representation lacks any hierarchy. I.e. once compiled
 * into a a pickle a scenario is no longer associated with a feature file.
 * <p>
 * However consumers of Cucumbers output generally expect results to be reported
 * in hierarchical fashion. This event allows test cases to be associated with
 * with a {@link Node} in the hierarchy.
 * <p>
 * Note that this representation is intentionally abstract. To create more
 * detailed reports that recreate a facsimile of the feature file it is
 * recommended to use the Gherkin AST. This AST can be obtained by parsing the
 * source provided by {@link TestSourceRead} event using {@code gherkin.Parser}
 * or {@code io.cucumber.gherkin.Gherkin}.
 * <p>
 * Note that a test source may contain multiple root nodes. Though currently
 * there are no parsers that support this yet.
 * <p>
 */
@API(status = API.Status.EXPERIMENTAL)
public final class TestSourceParsed extends TimeStampedEvent {

    private final URI uri;
    private final List<Node> nodes;

    public TestSourceParsed(Instant timeInstant, URI uri, List<Node> nodes) {
        super(timeInstant);
        this.uri = Objects.requireNonNull(uri);
        this.nodes = Objects.requireNonNull(nodes);
    }

    /**
     * The root nodes in the parsed test source.
     *
     * @return root nodes in the parsed test source.
     */
    public Collection<Node> getNodes() {
        return nodes;
    }

    public URI getUri() {
        return uri;
    }

}
