package cucumber.api.junit;

import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class CucumberRule implements TestRule {
    private final RuntimeOptions runtimeOptions;

    public CucumberRule() {
        this(new RuntimeOptionsFactory(CucumberRule.class /* no conf */).create());
    }

    public CucumberRule(final RuntimeOptions runtimeOptions) {
        this.runtimeOptions = runtimeOptions;
    }

    public CucumberRule(final String defaultPackage, final String... args) {
        final List<String> options = new ArrayList<String>(args == null ? Collections.<String>emptyList() : asList(args));
        if (defaultPackage != null) {
            if (options.isEmpty()) {
                options.add(MultiLoader.CLASSPATH_SCHEME + defaultPackage.replace('.', '/'));
            }
            if (!hasOption(options, "--glue")) {
                options.add("--glue");
                options.add(defaultPackage);
            }
            if (!hasOption(options, "--plugin")) {
                options.add("--plugin");
                options.add("pretty");
            }
        }
        this.runtimeOptions = new RuntimeOptions(options);
    }

    private boolean hasOption(List<String> options, String optName) {
        boolean hasPlugin = false;
        for (final String opt : options) {
            if (opt.equals(optName)) {
                hasPlugin = true;
                break;
            }
        }
        return hasPlugin;
    }

    public CucumberRule(final String[] args) {
        this(null, args);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                final ResourceLoader resourceLoader = new MultiLoader(classLoader);
                final ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
                final Runtime runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
                final List<CucumberFeature> cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader);
                final JUnitReporter jUnitReporter = new JUnitReporter(runtimeOptions.reporter(classLoader), runtimeOptions.formatter(classLoader), runtimeOptions.isStrict());
                try {
                    for (final CucumberFeature feature : cucumberFeatures) {
                        new FeatureRunner(feature, runtime, jUnitReporter).run(new RunNotifier());
                    }
                    base.evaluate();
                } finally {
                    jUnitReporter.done();
                    jUnitReporter.close();
                    runtime.printSummary();
                }
            }
        };
    }

    public static class ForceRunner extends BlockJUnit4ClassRunner {
        private static final FrameworkMethod FAKE_METHOD = new FrameworkMethod(placeholderMethod()) {
            @Override
            public Object invokeExplosively(Object target, Object... params) throws Throwable {
                return null; // placeholder
            }
        };

        private static Method placeholderMethod() {
            try {
                return CucumberRule.class.getDeclaredMethod("apply", Statement.class, Description.class);
            } catch (final NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }

        public ForceRunner(final Class<?> klass) throws InitializationError {
            super(klass);
        }

        @Override
        protected List<FrameworkMethod> computeTestMethods() {
            final List<FrameworkMethod> frameworkMethods = super.computeTestMethods();
            if (frameworkMethods.isEmpty()) { // to pass validations
                return new ArrayList<FrameworkMethod>() {{ add(FAKE_METHOD); }};
            }
            return frameworkMethods;
        }

        @Override
        protected Statement childrenInvoker(final RunNotifier notifier) {
            if (super.getChildren().isEmpty()) {
                return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        runLeaf(methodBlock(FAKE_METHOD), Description.createTestDescription(getTestClass().getName(), "cucumber"), notifier);
                    }
                };
            }
            return super.childrenInvoker(notifier);
        }
    }
}
