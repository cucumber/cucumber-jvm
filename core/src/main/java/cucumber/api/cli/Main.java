package cucumber.api.cli;

import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.util.log.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

import static java.util.Arrays.asList;

public class Main {

    public static void main(String[] argv) throws Throwable {
        byte exitstatus = run(argv, Thread.currentThread().getContextClassLoader());
        System.exit(exitstatus);
    }

    /**
     * Launches the Cucumber-JVM command line.
     *
     * @param argv        runtime options. See details in the {@code cucumber.api.cli.Usage.txt} resource.
     * @param classLoader classloader used to load the runtime
     * @return 0 if execution was successful, 1 if it was not (test failures)
     * @throws IOException if resources couldn't be loaded during the run.
     */
    public static byte run(String[] argv, ClassLoader classLoader) throws IOException {
        RuntimeOptions runtimeOptions = new RuntimeOptions(new ArrayList<String>(asList(argv)));
        LoggerFactory.setVerbose(runtimeOptions.isVerbose());

        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        Runtime runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
        runtime.run();
        return runtime.exitStatus();
    }
}
