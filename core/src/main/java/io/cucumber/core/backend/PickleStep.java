package io.cucumber.core.backend;

import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL)
public interface PickleStep extends Step {

    enum Keyword {
        GIVEN,
        WHEN,
        THEN
    }

    /**
     * @return the {@link Keyword} for this step
     */
    Keyword getKeyword();

    /**
     * @return the arguments passed into this step
     */
    Object[] getArguments();

}
