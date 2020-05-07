package io.cucumber.core.backend;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface Container {

    /**
     * Add a glue class to the test context.
     * <p>
     * Invoked after creation but before {@link ObjectFactory#start()}.
     *
     * @param  glueClass glue class to add to the text context.
     * @return           should always return true, should be ignored.
     */
    boolean addClass(Class<?> glueClass);

}
