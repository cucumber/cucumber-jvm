package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A node in a source file.
 * <p>
 * A node has a location, a name and optionally a keyword. The name may be
 * the the empty string if the node is unnamed.
 * <p>
 * Nodes are organized in a tree like structure where {@link Container} nodes
 * contain yet more nodes.
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
            //TODO: Fix this.
            Deque<Node> path = new ArrayDeque<>();
            path.add(this);

            List<T> elements = new ArrayList<>(elements());
            Collections.reverse(elements);
            Deque<Node> toSearch = new ArrayDeque<>(elements);

            while (!toSearch.isEmpty()) {
                Node element = toSearch.removeLast();
                path.addLast(element);
                if (predicate.test(element)) {
                    return Optional.of(new ArrayList<>(path));
                } else if (element instanceof Container) {
                    Container<?> container = (Container<?>) element;
                    toSearch.addAll(container.elements());
                } else {
                    path.removeLast();
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
     * An example has no keyword but always a name.
     */
    interface Example extends Node {

    }
}
