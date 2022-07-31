package cucumber.api.cli;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

/**
 * @deprecated use {@link io.cucumber.core.cli.Main} instead.
 */
@Deprecated
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] argv) {
        byte exitStatus = run(argv, Thread.currentThread().getContextClassLoader());
        System.exit(exitStatus);
    }

    /**
     * Launches the Cucumber-JVM command line.
     *
     * @param  argv        runtime options. See details in the
     *                     {@code io.cucumber.core.options.Usage.txt} resource.
     * @param  classLoader classloader used to load the runtime
     * @return             0 if execution was successful, 1 if it was not (test
     *                     failures)
     */
    public static byte run(String[] argv, ClassLoader classLoader) {
        log.warn(() -> "You are using deprecated Main class. Please use io.cucumber.core.cli.Main");
        return io.cucumber.core.cli.Main.run(argv, classLoader);
    }

}
