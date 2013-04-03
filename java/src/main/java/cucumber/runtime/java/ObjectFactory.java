package cucumber.runtime.java;

public interface ObjectFactory {

    /**
     * Setup factory <b>before</b> feature execution. Called once per feature.
     */
    void start();

    /**
     * Clean up factory <b>after</b>  feature execution. Called once per feature.
     */
    void stop();

    /**
     * Collects all Steps defintions classes in classpath. Called once on init.
     *
     * @param stepDefinitionType class containing cucumber.api annotations (Given, When, ...)
     */
    void addClass(Class<?> stepDefintionType);

    /**
     * Provides the Steps definition instance used to execute the current feature. The instance can be prepared in {@link #start()}.
     * 
     * @param stepDefinitionType type of instance to be created.
     * @param <T>
     * @return new Step Definition instance of type <T>
     */
    <T> T getInstance(Class<T> stepDefinitionType);
}
