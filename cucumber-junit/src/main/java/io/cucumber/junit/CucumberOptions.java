package io.cucumber.junit;

import io.cucumber.plugin.Plugin;
import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configure Cucumbers options.
 * 
 * @deprecated JUnit 4 is in maintenance mode. Upgrade to JUnit 5 and switch to
 *             the {@code cucumber-junit-platform-engine}.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@API(status = API.Status.STABLE)
public @interface CucumberOptions {

    /**
     * @return true if glue code execution should be skipped.
     */
    boolean dryRun() default false;

    /**
     * A list of features paths.
     * <p>
     * A feature path is constructed as
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
     * <p>
     * When no feature path is provided, Cucumber will use the package of the
     * annotated class. For example, if the annotated class is
     * {@code com.example.RunCucumber} then features are assumed to be located
     * in {@code classpath:com/example}.
     *
     * @return list of files or directories
     * @see    io.cucumber.core.feature.FeatureWithLines
     */
    String[] features() default {};

    /**
     * Package to load glue code (step definitions, hooks and plugins) from.
     * E.g: {@code com.example.app}
     * <p>
     * When no glue is provided, Cucumber will use the package of the annotated
     * class. For example, if the annotated class is
     * {@code com.example.RunCucumber} then glue is assumed to be located in
     * {@code com.example}.
     *
     * @return list of package names
     * @see    io.cucumber.core.feature.GluePath
     */
    String[] glue() default {};

    /**
     * Package to load additional glue code (step definitions, hooks and
     * plugins) from. E.g: {@code com.example.app}
     * <p>
     * These packages are used in addition to the default described in
     * {@code #glue}.
     *
     * @return list of package names
     */
    String[] extraGlue() default {};

    /**
     * Only run scenarios tagged with tags matching
     * <a href="https://github.com/cucumber/tag-expressions">Tag Expression</a>.
     * <p>
     * For example {@code "@smoke and not @fast"}.
     *
     * @return a tag expression
     */
    String tags() default "";

    /**
     * Register plugins. Built-in plugin types: {@code junit}, {@code html},
     * {@code pretty}, {@code progress}, {@code json}, {@code usage},
     * {@code unused}, {@code rerun}, {@code testng}.
     * <p>
     * Can also be a fully qualified class name, allowing registration of 3rd
     * party plugins.
     * <p>
     * Plugins can be provided with an argument. For example
     * {@code json:target/cucumber-report.json}
     *
     * @return list of plugins
     * @see    Plugin
     */
    String[] plugin() default {};

    /**
     * Publish report to https://reports.cucumber.io.
     * <p>
     * 
     * @return true if reports should be published on the web.
     */
    boolean publish() default false;

    /**
     * @return true if terminal output should be without colours.
     */
    boolean monochrome() default false;

    /**
     * Only run scenarios whose names match one of the provided regular
     * expressions.
     *
     * @return a list of regular expressions
     */
    String[] name() default {};

    /**
     * @return the format of the generated snippets.
     */
    SnippetType snippets() default SnippetType.UNDERSCORE;

    /**
     * Use filename compatible names.
     * <p>
     * Make sure that the names of the test cases only is made up of
     * [A-Za-Z0-9_] so that the names for certain can be used as file names.
     * <p>
     * Gradle for instance will use these names in the file names of the JUnit
     * xml report files.
     *
     * @return true to enforce the use of well-formed file names
     */
    boolean useFileNameCompatibleName() default false;

    /**
     * Provide step notifications.
     * <p>
     * By default steps are not included in notifications and descriptions. This
     * aligns test case in the Cucumber-JVM domain (Scenarios) with the test
     * case in the JUnit domain (the leafs in the description tree), and works
     * better with the report files of the notification listeners like maven
     * surefire or gradle.
     *
     * @return true to include steps should be included in notifications
     */
    boolean stepNotifications() default false;

    /**
     * Specify a custom ObjectFactory.
     * <p>
     * In case a custom ObjectFactory is needed, the class can be specified
     * here. A custom ObjectFactory might be needed when more granular control
     * is needed over the dependency injection mechanism.
     *
     * @return an {@link io.cucumber.core.backend.ObjectFactory} implementation
     */
    Class<? extends io.cucumber.core.backend.ObjectFactory> objectFactory() default NoObjectFactory.class;

    /**
     * Specify a custom ObjectFactory.
     * <p>
     * In case a custom ObjectFactory is needed, the class can be specified
     * here. A custom ObjectFactory might be needed when more granular control
     * is needed over the dependency injection mechanism.
     *
     * @return an {@link io.cucumber.core.backend.ObjectFactory} implementation
     */
    Class<? extends io.cucumber.core.eventbus.UuidGenerator> uuidGenerator() default NoUuidGenerator.class;

    enum SnippetType {
        UNDERSCORE, CAMELCASE
    }

}
