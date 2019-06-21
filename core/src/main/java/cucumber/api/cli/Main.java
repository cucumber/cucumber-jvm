package cucumber.api.cli;

import cucumber.runtime.CommandlineOptionsParser;
import cucumber.runtime.Env;
import cucumber.runtime.EnvironmentOptionsParser;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;

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
        ResourceLoader multiLoader = new MultiLoader(classLoader);

        RuntimeOptions runtimeOptions = new CommandlineOptionsParser(multiLoader)
            .parse(argv)
            .addDefaultFormatterIfNotPresent()
            .addDefaultSummaryPrinterIfNotPresent()
            .build();

        new EnvironmentOptionsParser(multiLoader)
            .parse(Env.INSTANCE)
            .build(runtimeOptions);

        final Runtime runtime = Runtime.builder()
            .withRuntimeOptions(runtimeOptions)
            .withClassLoader(classLoader)
            .build();

        runtime.run();
        return runtime.exitStatus();
    }
}
