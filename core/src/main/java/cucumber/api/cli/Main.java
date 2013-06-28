package cucumber.api.cli;

import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;

import java.io.IOException;

public class Main {

    public static void main(String[] argv) throws Throwable {
        run(argv, Thread.currentThread().getContextClassLoader());
    }

    public static void run(String[] argv, ClassLoader classLoader) throws IOException {
        RuntimeOptions runtimeOptions = new RuntimeOptions(System.getProperties(), argv);

        Runtime runtime = new Runtime(new MultiLoader(classLoader), classLoader, runtimeOptions);
        runtime.writeStepdefsJson();
        runtime.run();
        System.exit(runtime.exitStatus());
    }
}
