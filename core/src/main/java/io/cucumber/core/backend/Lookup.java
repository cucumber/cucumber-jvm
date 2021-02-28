package io.cucumber.core.backend;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface Lookup {

    /**
     * Provides an instance of a glue class.
     *
     * @param  glueClass type of instance to be created.
     * @param  <T>       type of Glue class
     * @return           new instance of type T
     */
    <T> T getInstance(Class<T> glueClass);

}
