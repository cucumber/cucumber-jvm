package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.util.Collection;
import java.util.Optional;

/**
 * A hierarchy of pickles.
 *
 *
 *
 *
 */
@API(status = API.Status.EXPERIMENTAL)
public interface Node {

    Location getLocation();

    String getKeyword();

    String getName();

    interface Container<T extends Node> {

        Collection<T> elements();

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
