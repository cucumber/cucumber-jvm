package io.cucumber.core.options;

import io.cucumber.core.runtime.ObjectFactoryServiceLoader;

public final class Constants {

    /**
     * Property name used to disable ansi colors in the output (not supported
     * by all terminals): {@value}
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
     * Property name used to enable dry-run: {@value}
     * <p>
     * Limits the number of scenarios to be executed to a specific amount.
     * <p>
     * By default scenarios are executed.
     */
    public static final String EXECUTION_LIMIT_PROPERTY_NAME = "cucumber.execution.limit";

    /**
     * Property name used to set execution order: {@value}
     * <p>
     * Valid values are {@code lexical}, {@code reverse}, {@code random} or {@code random:[seed]}.
     * <p>
     * By default features are executed in lexical file name order
     */
    public static final String EXECUTION_ORDER_PROPERTY_NAME = "cucumber.execution.order";

    /**
     * Property name used to enable strict execution: {@value}
     * <p>
     * When using strict execution Cucumber will treat undefined and pending
     * steps as errors.
     * <p>
     * By default, strict execution is disabled
     */
    public static final String EXECUTION_STRICT_PROPERTY_NAME = "cucumber.execution.strict";

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
     * Property name used to set feature location: {@value}
     * <p>
     * A comma separated list of:
     * <ul>
     * <li>{@code path/to/dir}  - Load the files with the extension ".feature" for the
     * directory {@code path} and its sub directories.
     * <li>{@code path/name.feature} - Load the feature file {@code path/name.feature}
     * from the file system.</li>
     * <li>{@code classpath:path/name.feature} - Load the feature file
     * {@code path/name.feature} from the classpath.</li>
     * <li>{@code path/name.feature:3:9} - Load the scenarios on line 3 and line 9 in
     * the file {@code path/name.feature}.</li>
     * <li>{@code @path/file} - Load {@code path/file} from the file system and parse
     * feature paths.</li>
     * <li>{@code @classpath:path/file} - Load {@code path/file} from the classpath and
     * parse feature paths.</li>
     * </ul>
     *
     * @see io.cucumber.core.feature.FeatureWithLines
     */
    public static final String FEATURES_PROPERTY_NAME = "cucumber.features";

    /**
     * Property name used to set name filter: {@value}
     * <p>
     * Filters features based on the provided regex pattern.
     * <p>
     * By default no features are filtered
     */
    public static final String FILTER_NAME_PROPERTY_NAME = "cucumber.filter.name";
    /**
     * Property name used to set tag filter: {@value}
     * <p>
     * Filters scenarios based on the provided tag expression e.g:
     * {@code @Integration and not @Ignored}. Scenarios that do not
     * match the expression are not executed.
     * <p>
     * By default all scenarios are executed
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
     * Property name used to pass command line options: {@value}
     * <p>
     * When available it is recommended to use a property based alternative.
     *
     * @see RuntimeOptionsParser
     */
    public static final String OPTIONS_PROPERTY_NAME = "cucumber.options";

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
    public static final String PLUGIN_PROPERTY_NAME = "cucumber.plugin";

    /**
     * Property name to control naming convention for generated snippets: {@value}
     * <p>
     * Valid values are {@code underscore} or {@code camelcase}.
     * <p>
     * By defaults are generated using the under score naming convention.
     */
    public static final String SNIPPET_TYPE_PROPERTY_NAME = "cucumber.snippet-type";

    private Constants() {

    }
}
