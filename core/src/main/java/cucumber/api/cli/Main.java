package cucumber.api.cli;

import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;

import java.io.IOException;

public class Main {

    public static void main(String[] argv) throws Throwable {
        run(argv, Thread.currentThread().getContextClassLoader());
    }

    public static void run(String[] argv, ClassLoader classLoader) throws IOException {
        RuntimeOptions runtimeOptions = new RuntimeOptions(System.getProperties(), argv);

        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        Runtime runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
        runtime.writeStepdefsJson();
        runtime.run();
        System.exit(runtime.exitStatus());
    }
}
