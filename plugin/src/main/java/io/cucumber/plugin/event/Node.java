package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;

/**
 * A node in a source file.
 * <p>
 * A node has a location, a keyword and name. The keyword and name are both
 * optional (e.g. {@link Example} and blank scenario names).
 * <p>
 * Nodes are organized in a tree like structure where {@link Container} nodes
 * contain yet more nodes.
 * <p>
 * A node can be linked to a {@link TestCase} by {@link #getLocation()}. The
 * {@link Node#findPathTo(Predicate)} method can be used to find a path from the
 * root node to a node with the same location as a test case. <code><pre>
 *
 * {@code Location location = testCase.getLocation();}
 * {@code Predicate<Node> withLocation = candidate -> location.equals(candidate.getLocation());}
 * {@code Optional<List<Node>> path = node.findPathTo(withLocation);}
 * </pre>
 * </code>
 */
@API(status = API.Status.EXPERIMENTAL)
public interface Node {

    Location getLocation();

    Optional<String> getKeyword();

    Optional<String> getName();

    /**
     * Recursively maps a node into another tree-like structure.
     *
     * @param  parent             the parent node of the target structure
     * @param  mapFeature         a function that takes a feature and a parent
     *                            node and returns a mapped feature
     * @param  mapRule            a function that takes a rule and a parent node
     *                            and returns a mapped rule
     * @param  mapScenario        a function that takes a scenario and a parent
     *                            node and returns a mapped scenario
     * @param  mapScenarioOutline a function that takes a scenario outline and a
     *                            parent node and returns a mapped scenario
     *                            outline
     * @param  mapExamples        a function that takes an examples and a parent
     *                            node and returns a mapped examples
     * @param  mapExample         a function that takes an example and a parent
     *                            node and returns a mapped example
     * @param  <T>                the type of the target structure
     * @return                    the mapped version of this instance
     */
    default <T> T map(
            T parent,
            BiFunction<Feature, T, T> mapFeature,
            BiFunction<Rule, T, T> mapRule, BiFunction<Scenario, T, T> mapScenario,
            BiFunction<ScenarioOutline, T, T> mapScenarioOutline,
            BiFunction<Examples, T, T> mapExamples,
            BiFunction<Example, T, T> mapExample
    ) {
        if (this instanceof Scenario) {
            return mapScenario.apply((Scenario) this, parent);
        } else if (this instanceof Example) {
            return mapExample.apply((Example) this, parent);
        } else if (this instanceof Container) {
            final T mapped;
            if (this instanceof Feature) {
                mapped = mapFeature.apply((Feature) this, parent);
            } else if (this instanceof Rule) {
                mapped = mapRule.apply((Rule) this, parent);
            } else if (this instanceof ScenarioOutline) {
                mapped = mapScenarioOutline.apply((ScenarioOutline) this, parent);
            } else if (this instanceof Examples) {
                mapped = mapExamples.apply((Examples) this, parent);
            } else {
                throw new IllegalArgumentException(this.getClass().getName());
            }
            Container<?> container = (Container<?>) this;
            container.elements().forEach(node -> node.map(mapped, mapFeature, mapRule, mapScenario, mapScenarioOutline,
                mapExamples, mapExample));
            return mapped;
        } else {
            throw new IllegalArgumentException(this.getClass().getName());
        }
    }

    /**
     * Finds a path down tree starting at this node to the first node that
     * matches the predicate using depth first search.
     *
     * @param  predicate to match the target node.
     * @return           a path to the first node or an empty optional if none
     *                   was found.
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

        @Override
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

        Collection<T> elements();

    }

    /**
     * A feature has a keyword and optionally a name.
     */
    interface Feature extends Node, Container<Node> {

    }

    /**
     * A rule has a keyword and optionally a name.
     */
    interface Rule extends Node, Container<Node> {

    }

    /**
     * A scenario has a keyword and optionally a name.
     */
    interface Scenario extends Node {

    }

    /**
     * A scenario outline has a keyword and optionally a name.
     */
    interface ScenarioOutline extends Node, Container<Examples> {

    }

    /**
     * An examples section has a keyword and optionally a name.
     */
    interface Examples extends Node, Container<Example> {

    }

    /**
     * An example has no keyword but always a name.
     */
    interface Example extends Node {

    }

}
