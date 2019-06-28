package io.cucumber.core.cli;

import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.options.*;
import io.cucumber.core.runtime.Runtime;

/**
 * Cucumber Main. Runs Cucumber as a CLI.
 * <p>
 * Options can be provided in order of precedence through:
 * <ol>
 * <li>command line arguments</li>
 * <li>{@code cucumber.options} property in {@link System#getProperties()} ()}</li>
 * <li>{@code cucumber.options} property in {@link System#getenv()}</li>
 * <li>{@code cucumber.options} property in {@code cucumber.properties}</li>
 * </ol>
 */
public class Main {

    public static void main(String[] argv) {
        byte exitStatus = run(argv, Thread.currentThread().getContextClassLoader());
        System.exit(exitStatus);
    }

    /**
     * Launches the Cucumber-JVM command line.
     *
     * @param argv        runtime options. See details in the {@code cucumber.api.cli.Usage.txt} resource.
     * @param classLoader classloader used to load the runtime
     * @return 0 if execution was successful, 1 if it was not (test failures)
     */
    public static byte run(String[] argv, ClassLoader classLoader) {
        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        RuntimeOptions bundleOptions = new CucumberPropertiesParser(resourceLoader)
            .parse(CucumberProperties.fromPropertiesFile())
            .build();

        RuntimeOptions environmentOptions = new CucumberPropertiesParser(resourceLoader)
            .parse(CucumberProperties.fromEnvironment())
            .build(bundleOptions);

        RuntimeOptions systemOptions = new CucumberPropertiesParser(resourceLoader)
            .parse(CucumberProperties.fromSystemProperties())
            .build(environmentOptions);

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(argv)
            .build(systemOptions);


        final Runtime runtime = Runtime.builder()
            .withRuntimeOptions(runtimeOptions)
            .withClassLoader(classLoader)
            .build();

        runtime.run();
        return runtime.exitStatus();
    }
}
