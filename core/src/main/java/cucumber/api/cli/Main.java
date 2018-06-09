package cucumber.api.cli;

import cucumber.runtime.Runtime;

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

        final Runtime runtime = Runtime.builder()
            .withArgs(argv)
            .withClassLoader(classLoader)
            .build();

        runtime.run();
        return runtime.exitStatus();
    }
}
