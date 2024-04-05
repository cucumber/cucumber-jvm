package io.cucumber.junit.platform.engine;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_CUSTOM_CLASS_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_FIXED_MAX_POOL_SIZE_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_FIXED_PARALLELISM_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_STRATEGY_PROPERTY_NAME;

@API(status = API.Status.STABLE)
public final class Constants {

    /**
     * Property name used to disable ansi colors in the output (not supported by
     * all terminals): {@value}
     * <p>
     * Valid values are {@code true}, {@code false}.
     * <p>
     * Ansi colors are enabled by default.
     */
    public static final String ANSI_COLORS_DISABLED_PROPERTY_NAME = io.cucumber.core.options.Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME;

    /**
     * Property name used to enable dry-run: {@value}
     * <p>
     * When using dry run Cucumber will skip execution of glue code.
     * <p>
     * Valid values are {@code true}, {@code false}.
     * <p>
     * By default, dry-run is disabled
     */
    public static final String EXECUTION_DRY_RUN_PROPERTY_NAME = io.cucumber.core.options.Constants.EXECUTION_DRY_RUN_PROPERTY_NAME;

    /**
     * Tag replacement pattern for the exclusive resource templates: {@value}
     *
     * @see #EXECUTION_EXCLUSIVE_RESOURCES_READ_WRITE_TEMPLATE
     */
    public static final String EXECUTION_EXCLUSIVE_RESOURCES_TAG_TEMPLATE_VARIABLE = "<tag-name>";

    /**
     * Property name used to select features: {@value}
     * <p>
     * A comma separated list of feature paths. A feature path is constructed as
     * {@code  [ PATH[.feature[:LINE]*] | URI[.feature[:LINE]*] }
     * <p>
     * Examples:
     * <ul>
     * <li>{@code src/test/resources/features} -- All features in the
     * {@code src/test/resources/features} directory</li>
     * <li>{@code classpath:com/example/application} -- All features in the
     * {@code com.example.application} package</li>
     * <li>{@code in-memory:/features} -- All features in the {@code /features}
     * directory on an in memory file system supported by
     * {@link java.nio.file.FileSystems}</li>
     * <li>{@code src/test/resources/features/example.feature:42} -- The
     * scenario or example at line 42 in the example feature file</li>
     * </ul>
     * <p>
     * Note: When used, any discovery selectors from the JUnit Platform will be
     * ignored. This may lead to multiple executions of Cucumber. For example
     * when used in combination with the JUnit Platform Suite Engine.
     * <p>
     * When using Cucumber through the JUnit Platform Launcher API or the JUnit
     * Platform Suite Engine, it is recommended to either use the
     * {@link org.junit.platform.engine.discovery.DiscoverySelectors} or
     * annotations from {@link org.junit.platform.suite.api} respectively.
     * <p>
     * Additionally, when this property is used, to work around limitations in
     * Maven Surefire and Gradle, the Cucumber Engine will report its
     * {@link org.junit.platform.engine.TestSource} as
     * {@link CucumberTestEngine}.
     *
     * @see io.cucumber.core.feature.FeatureWithLines
     */
    public static final String FEATURES_PROPERTY_NAME = io.cucumber.core.options.Constants.FEATURES_PROPERTY_NAME;

    /**
     * Property name used to set name filter: {@value}
     * <p>
     * Filter scenarios by name based on the provided regex pattern e.g:
     * {@code ^Hello (World|Cucumber)$}. Scenarios that do not match the
     * expression are not executed.
     * <p>
     * By default, all scenarios are executed
     * <p>
     * Note: To ensure consistent reports between Cucumber and JUnit 5 prefer
     * using JUnit 5 discovery request filters,
     * {@link org.junit.platform.suite.api.IncludeTags} or <a
     * href=https://junit.org/junit5/docs/current/user-guide/#running-tests-tag-expressions>JUnit
     * 5 tag expressions</a> instead.
     */
    public static final String FILTER_NAME_PROPERTY_NAME = io.cucumber.core.options.Constants.FILTER_NAME_PROPERTY_NAME;

    /**
     * Property name used to set tag filter: {@value}
     * <p>
     * Filter scenarios by tag based on the provided tag expression e.g:
     * {@code @Cucumber and not (@Gherkin or @Zucchini)}. Scenarios that do not
     * match the expression are not executed.
     * <p>
     * By default, all scenarios are executed
     * <p>
     * Note: To ensure consistent reports between Cucumber and JUnit 5 prefer
     * using JUnit 5 discovery request filters,
     * {@link org.junit.platform.suite.api.IncludeTags} or <a
     * href=https://junit.org/junit5/docs/current/user-guide/#running-tests-tag-expressions>JUnit
     * 5 tag expressions</a> instead.
     */
    public static final String FILTER_TAGS_PROPERTY_NAME = io.cucumber.core.options.Constants.FILTER_TAGS_PROPERTY_NAME;

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
     * Property name used to configure the naming strategy: {@value}
     * <p>
     * Value must be one of {@code long} or {@code short}. By default, short
     * names are used.
     * <p>
     * When long names are used the parent descriptor names are included into
     * each test descriptor name. So for example a single example would be
     * named:
     * {@code Feature Name - Rule Name - Scenario Name - Examples Name - Example #N }.
     * This is useful for tools that only report the test name such as Maven and
     * Gradle.
     */
    @API(status = Status.EXPERIMENTAL, since = "7.0.0")
    public static final String JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME = "cucumber.junit-platform.naming-strategy";

    /**
     * Property name used to configure the naming strategy of examples in case
     * of short naming strategy: {@value}
     * <p>
     * Value must be one of {@code number} or {@code pickle}. By default,
     * numbers are used.
     * <p>
     * When set to {@code pickle} the pickle name is used. So for scenario name
     * {@code Adding <a> and <b>} and example with params {@code a = 10} and
     * {@code b = 20} the following name would be produced:
     * {@code Adding 10 and 20}.
     * <p>
     * Using example numbers works well in all scenarios, but if parameterized
     * scenario names are used consistently, the pickle name provides more
     * clarity.
     */
    @API(status = Status.EXPERIMENTAL, since = "7.16.2")
    public static final String JUNIT_PLATFORM_SHORT_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME = "cucumber.junit-platform.naming-strategy.short.example-name";

    /**
     * Property name used to configure the naming strategy of examples in case
     * of long naming strategy: {@value}
     * <p>
     * Value must be one of {@code number} or {@code pickle}. By default,
     * numbers are used.
     * <p>
     * When set to {@code pickle} the pickle name is used. So for scenario name
     * {@code Adding <a> and <b>} and example with params {@code a = 10} and
     * {@code b = 20} the following name would be produced:
     * {@code Feature Name - Rule Name - Adding <a> and <b> - Examples Name - Adding 10 and 20}.
     * <p>
     * Using example numbers works well in all scenarios, but if parameterized
     * scenario names are used consistently, the pickle name provides more
     * clarity.
     */
    @API(status = Status.EXPERIMENTAL, since = "7.16.2")
    public static final String JUNIT_PLATFORM_LONG_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME = "cucumber.junit-platform.naming-strategy.long.example-name";

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
     * {@code PLUGIN} can also be a fully qualified class name, allowing
     * registration of 3rd party plugins.
     */
    public static final String PLUGIN_PROPERTY_NAME = io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;

    public static final String PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME = io.cucumber.core.options.Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME;

    /**
     * Property name to publish with bearer token: {@value}
     * <p>
     * Enabling this will publish authenticated test results online.
     * <p>
     */
    public static final String PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME = io.cucumber.core.options.Constants.PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME;

    /**
     * Property name to suppress publishing advertising banner: {@value}
     * <p>
     * Valid values are {@code true}, {@code false}.
     */
    public static final String PLUGIN_PUBLISH_QUIET_PROPERTY_NAME = io.cucumber.core.options.Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME;

    /**
     * Property name to select custom object factory implementation: {@value}
     * <p>
     * By default, if a single object factory is available on the class path
     * that object factory will be used.
     */
    public static final String OBJECT_FACTORY_PROPERTY_NAME = io.cucumber.core.options.Constants.OBJECT_FACTORY_PROPERTY_NAME;

    /**
     * Property name to select custom UUID generator implementation: {@value}
     * <p>
     * By default, if a single UUID generator is available on the class path
     * that object factory will be used, or more than one UUID generator and the
     * #RandomUuidGenerator are available on the classpath, the
     * #RandomUuidGenerator will be used.
     */
    public static final String UUID_GENERATOR_PROPERTY_NAME = io.cucumber.core.options.Constants.UUID_GENERATOR_PROPERTY_NAME;

    /**
     * Property name to control naming convention for generated snippets:
     * {@value}
     * <p>
     * Valid values are {@code underscore} or {@code camelcase}.
     * <p>
     * By defaults are generated using the under score naming convention.
     */
    public static final String SNIPPET_TYPE_PROPERTY_NAME = io.cucumber.core.options.Constants.SNIPPET_TYPE_PROPERTY_NAME;

    /**
     * Property name used to set the executing thread for all scenarios and
     * examples in a feature: {@value}
     * <p>
     * Valid values are {@code same_thread} or {@code concurrent}. Default value
     * is {@code concurrent}.
     * <p>
     * When parallel execution is enabled, scenarios are executed in parallel on
     * any available thread. setting this property to {@code same_thread}
     * executes scenarios sequentially in the same thread as the parent feature.
     *
     * @see #PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME
     */
    public static final String EXECUTION_MODE_FEATURE_PROPERTY_NAME = "cucumber.execution.execution-mode.feature";

    /**
     * Property name used to enable parallel test execution: {@value}
     * <p>
     * By default, tests are executed sequentially in a single thread.
     */
    public static final String PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME = "cucumber.execution.parallel.enabled";

    static final String EXECUTION_EXCLUSIVE_RESOURCES_PREFIX = "cucumber.execution.exclusive-resources.";

    static final String READ_WRITE_SUFFIX = ".read-write";

    /**
     * Property template used to describe a mapping of tags to exclusive
     * resources: {@value}
     * <p>
     * This maps a tag to a resource with a read-write lock.
     * <p>
     * For example given these properties:
     *
     * <pre>
     *  {@code
     * cucumber.execution.exclusive-resources.my-tag-ab-rw.read-write=resource-a,resource-b
     * cucumber.execution.exclusive-resources.my-tag-a-r.read=resource-a
     * }
     * </pre>
     * <p>
     * A scenario tagged with {@code @my-tag-ab-rw} will lock resource {@code a}
     * and {@code b} for reading and writing and will not be concurrently
     * executed with other scenarios tagged with {@code @my-tag-ab-rw} as well
     * as scenarios tagged with {@code @my-tag-a-r}. However a scenarios tagged
     * with {@code @my-tag-a-r} will be concurrently executed with other
     * scenarios with the same tag.
     *
     * @see <a href=
     *      "https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution-synchronization">Junit
     *      5 User Guide - Synchronization</a>
     */
    public static final String EXECUTION_EXCLUSIVE_RESOURCES_READ_WRITE_TEMPLATE = EXECUTION_EXCLUSIVE_RESOURCES_PREFIX
            + EXECUTION_EXCLUSIVE_RESOURCES_TAG_TEMPLATE_VARIABLE + READ_WRITE_SUFFIX;
    static final String READ_SUFFIX = ".read";

    /**
     * Property template used to describe a mapping of tags to exclusive
     * resources: {@value}
     * <p>
     * This maps a tag to a resource with a read lock.
     *
     * @see #EXECUTION_EXCLUSIVE_RESOURCES_READ_WRITE_TEMPLATE
     */
    public static final String EXECUTION_EXCLUSIVE_RESOURCES_READ_TEMPLATE = EXECUTION_EXCLUSIVE_RESOURCES_PREFIX
            + EXECUTION_EXCLUSIVE_RESOURCES_TAG_TEMPLATE_VARIABLE + READ_SUFFIX;

    static final String PARALLEL_CONFIG_PREFIX = "cucumber.execution.parallel.config.";

    /**
     * Property name used to determine the desired configuration strategy:
     * {@value}
     * <p>
     * Value must be one of {@code dynamic}, {@code fixed}, or {@code custom}.
     */
    public static final String PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
            + CONFIG_STRATEGY_PROPERTY_NAME;

    /**
     * Property name used to determine the desired parallelism for the
     * {@link DefaultParallelExecutionConfigurationStrategy#FIXED} configuration
     * strategy: {@value}
     * <p>
     * No default value; must be an integer.
     *
     * @see DefaultParallelExecutionConfigurationStrategy#FIXED
     */
    public static final String PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
            + CONFIG_FIXED_PARALLELISM_PROPERTY_NAME;
    /**
     * Property name used to determine the maximum pool size for the
     * {@link DefaultParallelExecutionConfigurationStrategy#FIXED} configuration
     * strategy: {@value}
     * <p>
     * Value must be an integer and greater than or equal to
     * {@value #PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME}; defaults to
     * {@code 256 + fixed.parallelism}.
     *
     * @see DefaultParallelExecutionConfigurationStrategy#FIXED
     */
    public static final String PARALLEL_CONFIG_FIXED_MAX_POOL_SIZE_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
            + CONFIG_FIXED_MAX_POOL_SIZE_PROPERTY_NAME;

    /**
     * Property name of the factor used to determine the desired parallelism for
     * the {@link DefaultParallelExecutionConfigurationStrategy#DYNAMIC}
     * configuration strategy: {@value}
     * <p>
     * Value must be a decimal number; defaults to {@code 1}.
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
