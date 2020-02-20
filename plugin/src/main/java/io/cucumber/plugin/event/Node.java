package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A hierarchy of pickles.
 */
@API(status = API.Status.EXPERIMENTAL)
public interface Node {

    Location getLocation();

    String getKeyword();

    String getName();

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
            Deque<Node> path = new ArrayDeque<>();
            path.add(this);

            Deque<Node> toSearch = new ArrayDeque<>(elements());

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

    interface Feature extends Node, Container<Node> {

    }

    interface Rule extends Node, Container<Node> {

    }

    interface Scenario extends Node {

    }

    interface ScenarioOutline extends Node, Container<Examples> {

    }

    interface Examples extends Node, Container<Example> {

    }

    interface Example extends Node {

    }
}
