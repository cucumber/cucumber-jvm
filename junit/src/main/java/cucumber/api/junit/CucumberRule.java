package cucumber.api.junit;

import cucumber.runner.EventBus;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitOptions;
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
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

public class CucumberRule implements TestRule {
    private final List<String> runtimeOptions = new ArrayList<String>();
    private final List<String> junitOptions = new ArrayList<String>();
    private boolean featureAdded = false;
    private final Collection<String> potentialFeatures = new ArrayList<String>();

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

    //
    // shared options
    //

    public CucumberRule withHelp() {
        junitOptions.add("--help");
        runtimeOptions.add("--help");
        return this;
    }

    //
    // junit options
    //

    public CucumberRule filenameCompatibleNames(final boolean value) {
        junitOptions.add(optionPrefix(value) + "filename-compatible-names");
        return this;
    }

    public CucumberRule stepNotifications(final boolean value) {
        junitOptions.add(optionPrefix(value) + "step-notifications");
        return this;
    }

    //
    // runtime options
    //

    public CucumberRule feature(final String value) {
        runtimeOptions.add(value);
        featureAdded = true;
        return this;
    }

    public CucumberRule glue(final Package value) {
        runtimeOptions.addAll(asList("--glue", value.getName()));
        potentialFeatures.add(MultiLoader.CLASSPATH_SCHEME + value.getName().replace('.', '/'));
        return this;
    }

    public CucumberRule glue(final String value) {
        runtimeOptions.addAll(asList("--glue", value));
        potentialFeatures.add(MultiLoader.CLASSPATH_SCHEME + value.replace('.', '/'));
        return this;
    }

    public CucumberRule tag(final String value) {
        runtimeOptions.addAll(asList("--tag", value));
        return this;
    }

    public CucumberRule plugin(final String value) {
        runtimeOptions.addAll(asList("--plugin", value));
        return this;
    }

    public CucumberRule snippets(final String value) {
        runtimeOptions.addAll(asList("--snippets", value));
        return this;
    }

    public CucumberRule name(final String value) {
        runtimeOptions.addAll(asList("--name", value));
        return this;
    }

    public CucumberRule dryRun(final boolean value) {
        runtimeOptions.add(optionPrefix(value) + "dry-run");
        return this;
    }

    public CucumberRule strict(final boolean value) {
        runtimeOptions.add(optionPrefix(value) + "strict");
        return this;
    }

    public CucumberRule monochrome(final boolean value) {
        runtimeOptions.add(optionPrefix(value) + "monochrome");
        return this;
    }

    // passthrough
    public CucumberRule options(final String... values) {
        runtimeOptions.addAll(asList(values));
        featureAdded = true; // we pass in "expert" mode and ignore the feature "guess" logic
        return this;
    }

    private String optionPrefix(final boolean value) {
        return "--" + (!value ? "no-" : "");
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                synchronized (CucumberRule.this) {
                    if (!hasOption(runtimeOptions, "--plugin")) {
                        runtimeOptions.add("--plugin");
                        runtimeOptions.add("pretty");
                    }
                    if (!featureAdded) {
                        runtimeOptions.addAll(potentialFeatures);
                        potentialFeatures.clear();
                        featureAdded = true;
                    }
                }

                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                final ResourceLoader resourceLoader = new MultiLoader(classLoader);
                final ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
                final RuntimeOptions runtimeOptions = new RuntimeOptions(CucumberRule.this.runtimeOptions);
                final Runtime runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
                final EventBus bus = runtime.getEventBus();
                final List<CucumberFeature> cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader, bus);
                final JUnitReporter jUnitReporter = new JUnitReporter(bus, runtimeOptions.isStrict(), new JUnitOptions(junitOptions));
                try {
                    for (final CucumberFeature feature : cucumberFeatures) {
                        new FeatureRunner(feature, runtime, jUnitReporter).run(new RunNotifier());
                    }
                    base.evaluate();
                } finally {
                    runtime.printSummary();
                }
            }
        };
    }

    public static class NoTestRunner extends BlockJUnit4ClassRunner {
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

        public NoTestRunner(final Class<?> klass) throws InitializationError {
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
