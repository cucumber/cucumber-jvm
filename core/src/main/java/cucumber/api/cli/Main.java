package cucumber.api.cli;

import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import cucumber.runtime.BackendModuleBackendSupplier;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.ExitStatus;
import cucumber.runtime.FeaturePathFeatureSupplier;
import cucumber.runtime.FeatureSupplier;
import cucumber.runtime.GlueSupplier;
import cucumber.runtime.RunnerSupplier;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.filter.RerunFilters;
import cucumber.runtime.ThreadLocalRunnerSupplier;
import cucumber.runtime.RuntimeGlueSupplier;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.FeatureLoader;

import static java.util.Arrays.asList;

public class Main {

    public static void main(String[] argv) {
        byte exitstatus = run(argv, Thread.currentThread().getContextClassLoader());
        System.exit(exitstatus);
    }

    /**
     * Launches the Cucumber-JVM command line.
     *
     * @param argv        runtime options. See details in the {@code cucumber.api.cli.Usage.txt} resource.
     * @param classLoader classloader used to load the runtime
     * @return 0 if execution was successful, 1 if it was not (test failures)
     */
    public static byte run(String[] argv, ClassLoader classLoader) {
        RuntimeOptions runtimeOptions = new RuntimeOptions(asList(argv));

        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendModuleBackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        EventBus bus = new EventBus(TimeService.SYSTEM);
        Plugins plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        ExitStatus exitStatus = new ExitStatus(runtimeOptions);
        exitStatus.setEventPublisher(bus);
        GlueSupplier glueSupplier = new RuntimeGlueSupplier();
        RunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, glueSupplier);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        FeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        Filters filters = new Filters(runtimeOptions, rerunFilters);
        Runtime runtime = new Runtime(plugins, bus, filters, runnerSupplier, featureSupplier);
        runtime.run();
        return exitStatus.exitStatus();
    }
}
