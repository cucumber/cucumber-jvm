package io.cucumber.core.backend;

import org.apiguardian.api.API;

import java.net.URI;
import java.util.List;
import java.util.Set;

@API(status = API.Status.STABLE)
public interface Backend {

    /**
     * Invoked once before all features. This is where steps and hooks should be
     * loaded.
     *
     * @param glue      Glue that provides the steps to be executed.
     * @param gluePaths The locations for the glue to be loaded.
     */
    default void loadGlue(Glue glue, List<URI> gluePaths) {

    }

    /**
     * Invoked once before all features. This is where steps and hooks should be
     * loaded.
     *
     * @param glue           Glue that provides the steps to be executed.
     * @param glueClassNames The classes of glue to be loaded.
     */
    default void loadGlueClasses(Glue glue, Set<String> glueClassNames) {
        // TODO: Refactor out a request object.
    }

    /**
     * Invoked before a new scenario starts. Implementations should do any
     * necessary setup of new, isolated state here. Additional scenario scoped
     * step definitions can be loaded here. These step definitions should
     * implement {@link ScenarioScoped}
     */
    void buildWorld();

    /**
     * Invoked at the end of a scenario, after hooks
     */
    void disposeWorld();

    Snippet getSnippet();

}
