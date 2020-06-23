package io.cucumber.core.api;

import org.apiguardian.api.API;

import java.util.Locale;

/**
 * The type registry configurer allows to configure a new type registry and the
 * locale.
 *
 * @deprecated Please use annotation based configuration. See <a href=
 *             "https://github.com/cucumber/cucumber-jvm/blob/main/examples/java-calculator/src/test/java/io/cucumber/examples/java/ShoppingSteps.java">Annotation
 *             based example</a> See <a href=
 *             "https://github.com/cucumber/cucumber-jvm/blob/main/examples/java8-calculator/src/test/java/io/cucumber/examples/java8/ShoppingSteps.java">Lambda
 *             based example</a>
 */
@API(status = API.Status.STABLE)
@Deprecated
public interface TypeRegistryConfigurer {

    /**
     * @return The locale to use, or null when language from feature file should
     *         be used.
     */
    default Locale locale() {
        return null;
    }

    /**
     * Configures the type registry.
     *
     * @param typeRegistry The new type registry.
     */
    void configureTypeRegistry(TypeRegistry typeRegistry);

}
