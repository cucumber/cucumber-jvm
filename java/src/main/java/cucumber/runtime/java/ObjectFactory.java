package cucumber.runtime.java;

public interface ObjectFactory {

    /**
     * Setup factory <b>before</b> scenario execution. Called once per scenario.
     */
    void start();

    /**
     * Clean up factory <b>after</b> scenario execution. Called once per scenario.
     */
    void stop();

    /**
     * Collects Glue classes in the classpath. Called once on init.
     * 
     * @param glueClass
     *            Glue class containing cucumber.api annotations (Before, Given, When, ...)
     */
    void addClass(Class<?> glueClass);

    /**
     * Provides the Glue instances used to execute the current scenario. The instance can be prepared in
     * {@link #start()}.
     * 
     * @param glueClass
     *            type of instance to be created.
     * @param <T>
     *            type of Glue class
     * @return new Glue instance of type <T>
     */
    <T> T getInstance(Class<T> glueClass);
}
