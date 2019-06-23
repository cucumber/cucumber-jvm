package io.cucumber.junit;

import cucumber.api.Plugin;
import cucumber.api.SnippetType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configure Cucumbers options.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CucumberOptions {

    /**
     * Skip execution of glue code.
     */
    boolean dryRun() default false;

    /**
     * Treat undefined and pending steps as errors.
     */
    boolean strict() default false;

    /**
     * Either a URI or path to a directory of features or a URI or path to a single
     * feature optionally followed by a colon and line numbers.
     * <p>
     * When no feature path is provided, Cucumber will use the package of the annotated
     * class. For example, if the annotated class is {@code com.example.RunCucumber}
     * then features are assumed to be located in {@code classpath:com/example}.
     *
     * @see io.cucumber.core.model.FeatureWithLines
     */
    String[] features() default {};

    /**
     * Package to load glue code (step definitions,
     * hooks and plugins) from. E.g: {@code com.example.app}
     * <p>
     * When no glue is provided, Cucumber will use the package of the annotated
     * class. For example, if the annotated class is {@code com.example.RunCucumber}
     * then glue is assumed to be located in {@code com.example}.
     *
     * @see io.cucumber.core.model.GluePath
     */
    String[] glue() default {};

    /**
     * Package to load additional glue code (step definitions, hooks and
     * plugins) from. E.g: {@code com.example.app}
     * <p>
     * These packages are used in addition to the default described in {@code #glue}.
     */
    String[] extraGlue() default {};

    /**
     * Only run scenarios tagged with tags matching {@code TAG_EXPRESSION}.
     * <p>
     * For example {@code "@smoke and not @fast"}.
     */
    String[] tags() default {};

    /**
     * Register plugins.
     * Built-in plugin types: {@code junit}, {@code html},
     * {@code pretty}, {@code progress}, {@code json}, {@code usage},
     * {@code unused}, {@code rerun}, {@code testng}.
     * <p>
     * Can also be a fully qualified class name, allowing
     * registration of 3rd party plugins.
     * <p>
     * Plugins can be provided with an argument. For example
     * {@code json:target/cucumber-report.json}
     *
     * @see Plugin
     */
    String[] plugin() default {};

    /**
     * Don't colour terminal output.
     */
    boolean monochrome() default false;

    /**
     * Only run scenarios whose names match provided regular expression.
     */
    String[] name() default {};

    /**
     * Format of the generated snippets.
     */
    SnippetType snippets() default SnippetType.UNDERSCORE;

    /**
     * Pass options to the JUnit runner.
     *
     * @deprecated use {@link #useFileNameCompatibleName()} or
     * {@link #stepNotifications()} instead.
     */
    @Deprecated
    String[] junit() default {};

    /**
     * Use filename compatible names.
     * <p>
     * Make sure that the names of the test cases only is made up of
     * [A-Za-Z0-9_] so that the names for certain can be used as file names.
     * <p>
     * Gradle for instance will use these names in the file names of
     * the JUnit xml report files.
     */
    boolean useFileNameCompatibleName() default false;

    /**
     * Provide step notifications.
     * <p>
     * By default steps are not included in notifications and descriptions.
     * This aligns test case in the Cucumber-JVM domain (Scenarios) with the
     * test case in the JUnit domain (the leafs in the description tree),
     * and works better with the report files of the notification listeners
     * like maven surefire or gradle.
     */
    boolean stepNotifications() default false;

    enum SnippetType {
        UNDERSCORE, CAMELCASE
    }
}
