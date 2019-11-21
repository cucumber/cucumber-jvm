package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_CUSTOM_CLASS_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_FIXED_PARALLELISM_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_STRATEGY_PROPERTY_NAME;

public final class Constants {

    /**
     * Property name used to disable ansi colors in the output (not supported
     * by all terminals): {@value}
     * <p>
     * Ansi colors are enabled by default.
     */
    public static final String ANSI_COLORS_DISABLED_PROPERTY_NAME = io.cucumber.core.options.Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME;

    /**
     * Property name used to enable dry-run: {@value}
     * <p>
     * When using dry run Cucumber will skip execution of glue code.
     * <p>
     * By default, dry-run is disabled
     */
    public static final String EXECUTION_DRY_RUN_PROPERTY_NAME = io.cucumber.core.options.Constants.EXECUTION_DRY_RUN_PROPERTY_NAME;

    /**
     * Property name to set the glue path: {@value}
     * <p>
     * A comma separated list of a classpath uri or package name e.g.:
     * {@code com.example.app.steps}.
     *
     * @see io.cucumber.core.feature.GluePath
     */
    public static final String GLUE_PROPERTY_NAME = io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;

    /**
     * Property name to enable plugins: {@value}
     * <p>
     * A comma separated list of {@code [PLUGIN[:PATH_OR_URL]]} e.g:
     * {@code json:target/cucumber.json}.
     * <p>
     * Built-in formatter PLUGIN types:
     * <ul>
     * <li>html</li>
     * <li>pretty</li>
     * <li>progress</li>
     * <li>summary</li>
     * <li>json</li>
     * <li>usage</li>
     * <li>rerun</li>
     * <li>junit</li>
     * <li>testng</li>
     * </ul>
     * <p>
     * {@code PLUGIN} can also be a fully qualified class name, allowing registration
     * of 3rd party plugins.
     */
    public static final String PLUGIN_PROPERTY_NAME = io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;

    /**
     * Property name to select custom object factory implementation: {@value}
     * <p>
     * By default, if a single object factory is available on the class path
     * that object factory will be used.
     */
    public static final String OBJECT_FACTORY_PROPERTY_NAME = io.cucumber.core.options.Constants.OBJECT_FACTORY_PROPERTY_NAME;

    /**
     * Property name to control naming convention for generated snippets: {@value}
     * <p>
     * Valid values are {@code underscore} or {@code camelcase}.
     * <p>
     * By defaults are generated using the under score naming convention.
     */
    public static final String SNIPPET_TYPE_PROPERTY_NAME = io.cucumber.core.options.Constants.SNIPPET_TYPE_PROPERTY_NAME;

    /**
     * Property name used to enable parallel test execution: {@value}
     * <p>
     * By default, tests are executed sequentially in a single thread.
     */
    public static final String PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME = "cucumber.execution.parallel.enabled";

    static final String PARALLEL_CONFIG_PREFIX = "cucumber.execution.parallel.config.";
    /**
     * Property name used to determine the desired configuration strategy: {@value}
     *
     * <p>Value must be one of {@code dynamic}, {@code fixed}, or
     * {@code custom}.
     */
    public static final String PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
        + CONFIG_STRATEGY_PROPERTY_NAME;

    /**
     * Property name used to determine the desired parallelism for the
     * {@link DefaultParallelExecutionConfigurationStrategy#FIXED}
     * configuration strategy: {@value}
     *
     * <p>No default value; must be an integer.
     *
     * @see DefaultParallelExecutionConfigurationStrategy#FIXED
     */
    public static final String PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
        + CONFIG_FIXED_PARALLELISM_PROPERTY_NAME;

    /**
     * Property name of the factor used to determine the desired parallelism
     * for the {@link DefaultParallelExecutionConfigurationStrategy#DYNAMIC}
     * configuration strategy: {@value}
     *
     * <p>Value must be a decimal number; defaults to {@code 1}.
     *
     * @see DefaultParallelExecutionConfigurationStrategy#DYNAMIC
     */
    public static final String PARALLEL_CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
        + CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME;

    /**
     * Property name used to specify the fully qualified class name of the
     * {@link ParallelExecutionConfigurationStrategy} to be used by the
     * {@link DefaultParallelExecutionConfigurationStrategy#CUSTOM}
     * configuration strategy: {@value}
     *
     * @see DefaultParallelExecutionConfigurationStrategy#CUSTOM
     */
    public static final String PARALLEL_CONFIG_CUSTOM_CLASS_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
        + CONFIG_CUSTOM_CLASS_PROPERTY_NAME;

    private Constants() {

    }

}
