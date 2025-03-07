package io.cucumber.core.options;

import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.UuidGeneratorServiceLoader;

public final class Constants {

    /**
     * Property name used to disable ansi colors in the output (not supported by
     * all terminals): {@value}
     * <p>
     * Ansi colors are enabled by default.
     */
    public static final String ANSI_COLORS_DISABLED_PROPERTY_NAME = "cucumber.ansi-colors.disabled";

    /**
     * File name of cucumber properties file: {@value}
     */
    public static final String CUCUMBER_PROPERTIES_FILE_NAME = "cucumber.properties";

    /**
     * Property name used to enable dry-run: {@value}
     * <p>
     * When using dry run Cucumber will skip execution of glue code.
     * <p>
     * By default, dry-run is disabled
     */
    public static final String EXECUTION_DRY_RUN_PROPERTY_NAME = "cucumber.execution.dry-run";

    /**
     * Property name used to limit the number of executed scenarios : {@value}
     * <p>
     * Limits the number of scenarios to be executed to a specific amount.
     * <p>
     * By default scenarios are executed.
     */
    public static final String EXECUTION_LIMIT_PROPERTY_NAME = "cucumber.execution.limit";

    /**
     * Property name used to set execution order: {@value}
     * <p>
     * Valid values are {@code lexical}, {@code reverse}, {@code random} or
     * {@code random:[seed]}.
     * <p>
     * By default, features are executed in lexical file name order and
     * scenarios in a feature from top to bottom.
     */
    public static final String EXECUTION_ORDER_PROPERTY_NAME = "cucumber.execution.order";

    /**
     * Property name used to enable wip execution: {@value}
     * <p>
     * When using wip execution Cucumber will fail if there are any passing
     * scenarios.
     * <p>
     * By default, wip execution is disabled
     */
    public static final String WIP_PROPERTY_NAME = "cucumber.execution.wip";

    /**
     * Property name used to select features: {@value}
     * <p>
     * A comma separated list of feature paths. A feature path is constructed as
     * {@code  [ PATH[.feature[:LINE]*] | URI[.feature[:LINE]*] | @PATH ] }
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
     * <li>{@code @target/rerun} -- All the scenarios in the files in the rerun
     * directory</li>
     * <li>{@code @target/rerun/RunCucumber.txt} -- All the scenarios in
     * RunCucumber.txt file</li>
     * </ul>
     *
     * @see io.cucumber.core.feature.FeatureWithLines
     */
    public static final String FEATURES_PROPERTY_NAME = "cucumber.features";

    /**
     * Property name used to set name filter: {@value}
     * <p>
     * Filters scenarios by name based on the provided regex pattern e.g:
     * {@code ^Hello (World|Cucumber)$}. Scenarios that do not match the
     * expression are not executed. Combined with
     * {@value FILTER_TAGS_PROPERTY_NAME} using "and" semantics.
     * <p>
     * By default, all scenarios are executed
     */
    public static final String FILTER_NAME_PROPERTY_NAME = "cucumber.filter.name";

    /**
     * Property name used to set tag filter: {@value}
     * <p>
     * Filters scenarios by tag based on the provided tag expression e.g:
     * {@code @Cucumber and not (@Gherkin or @Zucchini)}. Scenarios that do not
     * match the expression are not executed. Combined with
     * {@value FILTER_NAME_PROPERTY_NAME} using "and" semantics.
     * <p>
     * By default, all scenarios are executed
     */
    public static final String FILTER_TAGS_PROPERTY_NAME = "cucumber.filter.tags";

    /**
     * Property name to set the glue path: {@value}
     * <p>
     * A comma separated list of a classpath uri or package name e.g.:
     * {@code com.example.app.steps}.
     *
     * @see io.cucumber.core.feature.GluePath
     */
    public static final String GLUE_PROPERTY_NAME = "cucumber.glue";

    /**
     * Property name used to select a specific object factory implementation:
     * {@value}
     *
     * @see ObjectFactoryServiceLoader
     */
    public static final String OBJECT_FACTORY_PROPERTY_NAME = "cucumber.object-factory";

    /**
     * Property name used to select a specific UUID generator implementation:
     * {@value}
     *
     * @see UuidGeneratorServiceLoader
     */
    public static final String UUID_GENERATOR_PROPERTY_NAME = "cucumber.uuid-generator";

    /**
     * Property name formerly used to pass command line options to Cucumber:
     * {@value} This property is no longer read by Cucumber. Please use any of
     * the individual properties instead.
     */
    static final String OPTIONS_PROPERTY_NAME = "cucumber.options";

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
    public static final String PLUGIN_PROPERTY_NAME = "cucumber.plugin";

    /**
     * Property name to enable publishing cucumber reports: {@value}
     * <p>
     * Enabling this will publish test results online.
     * <p>
     * Valid values are {@code true}, {@code false}.
     */
    public static final String PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME = "cucumber.publish.enabled";

    /**
     * Property name to publish cucumber reports with bearer token: {@value}
     * <p>
     * Enabling this will publish authenticated test results online.
     * <p>
     */
    public static final String PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME = "cucumber.publish.token";

    /**
     * Property name to override the cucumber reports publish uri: {@value}
     * <p>
     * Note that setting this property is not sufficient to activate publishing.
     */
    public static final String PLUGIN_PUBLISH_URL_PROPERTY_NAME = "cucumber.publish.url";

    /**
     * Property name to set the proxy used to publish cucumber reports .
     * <p>
     * Note that setting this property is not sufficient to activate publishing.
     */
    public static final String PLUGIN_PUBLISH_PROXY_PROPERTY_NAME = "cucumber.publish.proxy";

    /**
     * Property name to suppress publishing advertising banner: {@value}
     * <p>
     * Valid values are {@code true}, {@code false}.
     */
    public static final String PLUGIN_PUBLISH_QUIET_PROPERTY_NAME = "cucumber.publish.quiet";

    /**
     * Property name to control naming convention for generated snippets:
     * {@value}
     * <p>
     * Valid values are {@code underscore} or {@code camelcase}.
     * <p>
     * By defaults are generated using the under score naming convention.
     */
    public static final String SNIPPET_TYPE_PROPERTY_NAME = "cucumber.snippet-type";

    private Constants() {

    }

}
