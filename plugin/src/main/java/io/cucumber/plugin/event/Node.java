package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;

/**
 * A node in a source file.
 * <p>
 * A node has a location, a name and optionally (null) a keyword. The name may be
 * the the empty string if the node is unnamed.
 * <p>
 * Nodes are organized in a tree like structure where {@link Container} nodes
 * contain yet more nodes.
 * <p>
 * A node can be linked to a {@link TestCase} by {@link #getLocation()}. The
 * {@link Node#findPathTo(Predicate)} method can be used to find a path from the
 * root node to a node with the same location as a test case.
 *
 * <pre>
 * {@code
 *       Location location = testCase.getLocation();
 *       Predicate<Node> withLocation = candidate ->
 *          location.equals(candidate.getLocation());
 *       Optional<List<Node>> path = node.findPathTo(withLocation);
 * }
 * </pre>
 */
@API(status = API.Status.EXPERIMENTAL)
public interface Node {

    Location getLocation();

    String getKeyword();

    String getName();

    /**
     * Finds a path down tree starting at this node to the first node that
     * matches the predicate using depth first search.
     *
     * @param predicate to match the target node.
     * @return a path to the first node or an empty optional if none was found.
     */
    default Optional<List<Node>> findPathTo(Predicate<Node> predicate) {
        if (predicate.test(this)) {
            List<Node> path = new ArrayList<>();
            path.add(this);
            return Optional.of(path);
        }
        return Optional.empty();
    }

    interface Container<T extends Node> extends Node {

        Collection<T> elements();

        default Optional<List<Node>> findPathTo(Predicate<Node> predicate) {
            List<Node> path = new ArrayList<>();

            Deque<Deque<Node>> toSearch = new ArrayDeque<>();
            toSearch.addLast(new ArrayDeque<>(singletonList(this)));

            while (!toSearch.isEmpty()) {
                Deque<Node> candidates = toSearch.peekLast();
                if (candidates.isEmpty()) {
                    if (!path.isEmpty()) {
                        path.remove(path.size() - 1);
                    }
                    toSearch.removeLast();
                    continue;
                }
                Node candidate = candidates.pop();
                if (predicate.test(candidate)) {
                    path.add(candidate);
                    return Optional.of(path);
                }
                if (candidate instanceof Container) {
                    path.add(candidate);
                    Container<?> container = (Container<?>) candidate;
                    toSearch.addLast(new ArrayDeque<>(container.elements()));
                }
            }
            return Optional.empty();
        }
    }

    /**
     * A feature has a keyword and name.
     */
    interface Feature extends Node, Container<Node> {

    }

    /**
     * A rule has a keyword and name.
     */
    interface Rule extends Node, Container<Node> {

    }

    /**
     * A scenario has a keyword and name.
     */
    interface Scenario extends Node {

    }

    /**
     * A scenario outline has a keyword and name.
     */
    interface ScenarioOutline extends Node, Container<Examples> {

    }

    /**
     * An examples section has a keyword and name.
     */
    interface Examples extends Node, Container<Example> {

    }

    /**
     * An example has no keyword (null) but always a name.
     */
    interface Example extends Node {

    }
}
