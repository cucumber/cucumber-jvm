package io.cucumber.core.options;

import io.cucumber.core.snippets.SnippetType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation provides the same options as the cucumber command line, {@link io.cucumber.core.cli.Main}.
 *
 * @deprecated use either {@code io.cucumber.junit.CucumberOptions} or {@code io.cucumber.testng.CucumberOptions}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CucumberOptions {

    /**
     * Skip execution of glue code.
     *
     * @return True when dry run, false otherwise.
     */
    boolean dryRun() default false;

    /**
     * Treat undefined and pending steps as errors.
     *
     * @return True when strict, false otherwise.
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
     * @see io.cucumber.core.feature.FeatureWithLines
     * @return The location(s) of the features.
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
     * @see io.cucumber.core.feature.GluePath
     * @return The package(s) that contain glue code.
     */
    String[] glue() default {};

    /**
     * Package to load additional glue code (step definitions, hooks and
     * plugins) from. E.g: {@code com.example.app}
     * <p>
     * These packages are used in addition to the default described in {@code #glue}.
     *
     * @return The package(s) that contain the extra glue code.
     */
    String[] extraGlue() default {};

    /**
     * Only run scenarios tagged with tags matching {@code TAG_EXPRESSION}.
     * <p>
     * For example {@code "@smoke and not @fast"}.
     *
     * @return The tags that should be matched.
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
     * @see io.cucumber.core.plugin.Plugin
     * @return The plugins that should be added.
     */
    String[] plugin() default {};

    /**
     * Don't colour terminal output.
     *
     * @return True when no color should be present in the terminal output, false when color is allowed.
     */
    boolean monochrome() default false;

    /**
     * Only run scenarios whose names match provided regular expression.
     *
     * @return The name(s) that should be matched via regular expressions.
     */
    String[] name() default {};

    /**
     * Format of the generated snippets.
     *
     * @see SnippetType
     * @return The snippet type to be generated for missing steps.
     */
    SnippetType snippets() default SnippetType.UNDERSCORE;

    /**
     * A custom ObjectFactory.
     *
     * @return The class of the custim ObjectFactory to use.
     */
    Class<? extends io.cucumber.core.backend.ObjectFactory> objectFactory() default NoObjectFactory.class;

    /**
     * Pass options to the JUnit runner.
     *
     * @return The JUnit options to pass on.
     */
    String[] junit() default {};

}
