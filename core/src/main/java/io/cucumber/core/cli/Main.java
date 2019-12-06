package io.cucumber.core.cli;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.options.Constants;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.Runtime;
import org.apiguardian.api.API;

/**
 * Cucumber Main. Runs Cucumber as a CLI.
 * <p>
 * Options can be provided in by (order of precedence):
 * <ol>
 * <li>Command line arguments</li>
 * <li>Properties from {@link System#getProperties()}</li>
 * <li>Properties from in {@link System#getenv()}</li>
 * <li>Properties from {@value Constants#CUCUMBER_PROPERTIES_FILE_NAME}</li>
 * </ol>
 * For available properties see {@link Constants}.
 */
@API(status = API.Status.STABLE)
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String... argv) {
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
        RuntimeOptions propertiesFileOptions = new CucumberPropertiesParser()
            .parse(CucumberProperties.fromPropertiesFile())
            .build();

        RuntimeOptions environmentOptions = new CucumberPropertiesParser()
            .parse(CucumberProperties.fromEnvironment())
            .build(propertiesFileOptions);

        RuntimeOptions systemOptions = new CucumberPropertiesParser()
            .parse(CucumberProperties.fromSystemProperties())
            .build(environmentOptions);

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser()
            .parse(argv)
            .addDefaultFormatterIfAbsent()
            .addDefaultSummaryPrinterIfAbsent()
            .build(systemOptions);

        if (!runtimeOptions.isStrict()) {
            log.warn(() -> "By default Cucumber is running in --non-strict mode.\n" +
                "This default will change to --strict and --non-strict will be removed.\n" +
                "You can use --strict to suppress this warning"
            );
        }

        final Runtime runtime = Runtime.builder()
            .withRuntimeOptions(runtimeOptions)
            .withClassLoader(() -> classLoader)
            .build();

        runtime.run();
        return runtime.exitStatus();
    }
}
