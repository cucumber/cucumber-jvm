package cucumber.examples.java.paxexam.test;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import cucumber.api.event.EventHandler;
import cucumber.api.event.TestStepFinished;
import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.FeaturePathFeatureSupplier;
import cucumber.runtime.FeatureSupplier;
import cucumber.runtime.RunnerSupplier;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.filter.RerunFilters;
import cucumber.runtime.ThreadLocalRunnerSupplier;
import cucumber.runtime.RuntimeGlueSupplier;
import cucumber.runtime.Supplier;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import io.cucumber.stepexpression.TypeRegistry;
import cucumber.api.java.ObjectFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.util.Injector;
import org.osgi.framework.BundleContext;

import cucumber.api.CucumberOptions;
import cucumber.java.runtime.osgi.OsgiClassFinder;
import cucumber.java.runtime.osgi.PaxExamObjectFactory;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.FileResourceLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
@CucumberOptions(features = "features")
public class CalculatorTest {

    @Inject
    private Injector injector;

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] config() {

        return options(
            mavenBundle("io.cucumber", "pax-exam-calculator-api"),
            mavenBundle("io.cucumber", "pax-exam-calculator-service"),

            mavenBundle("io.cucumber", "gherkin"),
            mavenBundle("io.cucumber", "tag-expressions"),
            mavenBundle("io.cucumber", "datatable"),
            mavenBundle("io.cucumber", "datatable-dependencies"),
            mavenBundle("io.cucumber", "cucumber-expressions"),
            mavenBundle("io.cucumber", "cucumber-core"),
            mavenBundle("io.cucumber", "cucumber-java"),
            mavenBundle("io.cucumber", "cucumber-osgi"),

            mavenBundle("org.slf4j", "slf4j-api"),
            mavenBundle("ch.qos.logback", "logback-core"),
            mavenBundle("ch.qos.logback", "logback-classic"),

            junitBundles()
        );
    }

    @Test
    public void cucumber() {
        assertNotNull(injector);
        assertNotNull(bundleContext);
        final ResourceLoader resourceLoader = new FileResourceLoader();
        final ClassLoader classLoader = Runtime.class.getClassLoader();
        final ObjectFactory objectFactory = new PaxExamObjectFactory(injector);
        final ClassFinder classFinder = new OsgiClassFinder(bundleContext);
        final TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);
        final Backend backend = new JavaBackend(objectFactory, classFinder, typeRegistry);

        final RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(getClass());
        final RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        final EventBus bus = new EventBus(TimeService.SYSTEM);
        final Plugins plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        final BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                return Collections.singleton(backend);
            }
        };
        final RuntimeGlueSupplier glueSupplier = new RuntimeGlueSupplier();
        final RunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, glueSupplier);
        final FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        final FeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);
        final RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        final Filters filters = new Filters(runtimeOptions, rerunFilters);
        final Runtime runtime = new Runtime(plugins, runtimeOptions, bus, filters, runnerSupplier, featureSupplier);
        final List<Throwable> errors = new ArrayList<Throwable>();


        bus.registerHandlerFor(TestStepFinished.class, new EventHandler<TestStepFinished>() {
            @Override
            public void receive(TestStepFinished event) {
                Throwable error = event.result.getError();
                if(error != null)
                errors.add(error);
            }
        });

        runtime.run();

        if (!errors.isEmpty()) {
            throw new CucumberException(errors.get(0));
        } else if (runtime.exitStatus() != 0x00) {
            throw new CucumberException("There are pending or undefined steps.");
        }
    }
}
