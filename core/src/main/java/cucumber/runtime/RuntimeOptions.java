package cucumber.runtime;

import cucumber.api.SnippetType;
import cucumber.api.StepDefinitionReporter;
import cucumber.runtime.formatter.ColorAware;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.StrictAware;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.util.FixJava;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static cucumber.runtime.model.CucumberFeature.load;

// IMPORTANT! Make sure USAGE.txt is always uptodate if this class changes.
public class RuntimeOptions {
    public static final String VERSION = ResourceBundle.getBundle("cucumber.version").getString("cucumber-jvm.version");
    public static final String USAGE = FixJava.readResource("/cucumber/api/cli/USAGE.txt");

    private final List<String> glue = new ArrayList<String>();
    private final List<Object> filters = new ArrayList<Object>();
    private final List<String> featurePaths = new ArrayList<String>();
    private final List<String> pluginNames = new ArrayList<String>();
    private final PluginFactory pluginFactory;
    private final List<Object> plugins = new ArrayList<Object>();
    private boolean dryRun;
    private boolean strict = false;
    private boolean monochrome = false;
    private SnippetType snippetType = SnippetType.UNDERSCORE;
    private boolean pluginNamesInstantiated;

    /**
     * Create a new instance from a string of options, for example:
     * <p/>
     * <pre<{@code "--name 'the fox' --plugin pretty --strict"}</pre>
     *
     * @param argv the arguments
     */
    public RuntimeOptions(String argv) {
        this(new PluginFactory(), Shellwords.parse(argv));
    }

    /**
     * Create a new instance from a list of options, for example:
     * <p/>
     * <pre<{@code Arrays.asList("--name", "the fox", "--plugin", "pretty", "--strict");}</pre>
     *
     * @param argv the arguments
     */
    public RuntimeOptions(List<String> argv) {
        this(new PluginFactory(), argv);
    }

    public RuntimeOptions(Env env, List<String> argv) {
        this(env, new PluginFactory(), argv);
    }

    public RuntimeOptions(PluginFactory pluginFactory, List<String> argv) {
        this(new Env("cucumber"), pluginFactory, argv);
    }

    public RuntimeOptions(Env env, PluginFactory pluginFactory, List<String> argv) {
        this.pluginFactory = pluginFactory;

        argv = new ArrayList<String>(argv); // in case the one passed in is unmodifiable.
        parse(argv);

        String cucumberOptionsFromEnv = env.get("cucumber.options");
        if (cucumberOptionsFromEnv != null) {
            parse(Shellwords.parse(cucumberOptionsFromEnv));
        }

        if (pluginNames.isEmpty()) {
            pluginNames.add("progress");
        }
    }

    private void parse(List<String> args) {
        List<Object> parsedFilters = new ArrayList<Object>();
        List<String> parsedFeaturePaths = new ArrayList<String>();
        List<String> parsedGlue = new ArrayList<String>();

        while (!args.isEmpty()) {
            String arg = args.remove(0).trim();

            if (arg.equals("--help") || arg.equals("-h")) {
                printUsage();
                System.exit(0);
            } else if (arg.equals("--version") || arg.equals("-v")) {
                System.out.println(VERSION);
                System.exit(0);
            } else if (arg.equals("--glue") || arg.equals("-g")) {
                String gluePath = args.remove(0);
                parsedGlue.add(gluePath);
            } else if (arg.equals("--tags") || arg.equals("-t")) {
                parsedFilters.add(args.remove(0));
            } else if (arg.equals("--plugin") || arg.equals("-p")) {
                pluginNames.add(args.remove(0));
            } else if (arg.equals("--format") || arg.equals("-f")) {
                System.err.println("WARNING: Cucumber-JVM's --format option is deprecated. Please use --plugin instead.");
                pluginNames.add(args.remove(0));
            } else if (arg.equals("--no-dry-run") || arg.equals("--dry-run") || arg.equals("-d")) {
                dryRun = !arg.startsWith("--no-");
            } else if (arg.equals("--no-strict") || arg.equals("--strict") || arg.equals("-s")) {
                strict = !arg.startsWith("--no-");
            } else if (arg.equals("--no-monochrome") || arg.equals("--monochrome") || arg.equals("-m")) {
                monochrome = !arg.startsWith("--no-");
            } else if (arg.equals("--snippets")) {
                String nextArg = args.remove(0);
                snippetType = SnippetType.fromString(nextArg);
            } else if (arg.equals("--name") || arg.equals("-n")) {
                String nextArg = args.remove(0);
                Pattern patternFilter = Pattern.compile(nextArg);
                parsedFilters.add(patternFilter);
            } else if (arg.startsWith("-")) {
                printUsage();
                throw new CucumberException("Unknown option: " + arg);
            } else {
                parsedFeaturePaths.add(arg);
            }
        }
        if (!parsedFilters.isEmpty()) {
            filters.clear();
            filters.addAll(parsedFilters);
        }
        if (!parsedFeaturePaths.isEmpty()) {
            featurePaths.clear();
            featurePaths.addAll(parsedFeaturePaths);
        }
        if (!parsedGlue.isEmpty()) {
            glue.clear();
            glue.addAll(parsedGlue);
        }
    }

    private void printUsage() {
        System.out.println(USAGE);
    }

    public List<CucumberFeature> cucumberFeatures(ResourceLoader resourceLoader) {
        return load(resourceLoader, featurePaths, filters, System.out);
    }

    List<Object> getPlugins() {
        if (!pluginNamesInstantiated) {
            for (String pluginName : pluginNames) {
                Object plugin = pluginFactory.create(pluginName);
                plugins.add(plugin);
                setMonochromeOnColorAwarePlugins(plugin);
                setStrictOnStrictAwarePlugins(plugin);
            }
            pluginNamesInstantiated = true;
        }
        return plugins;
    }

    public Formatter formatter(ClassLoader classLoader) {
        return pluginProxy(classLoader, Formatter.class);
    }

    public Reporter reporter(ClassLoader classLoader) {
        return pluginProxy(classLoader, Reporter.class);
    }

    public StepDefinitionReporter stepDefinitionReporter(ClassLoader classLoader) {
        return pluginProxy(classLoader, StepDefinitionReporter.class);
    }

    /**
     * Creates a dynamic proxy that multiplexes method invocations to all plugins of the same type.
     *
     * @param classLoader used to create the proxy
     * @param type        proxy type
     * @param <T>         generic proxy type
     * @return a proxy
     */
    public <T> T pluginProxy(ClassLoader classLoader, final Class<T> type) {
        Object proxy = Proxy.newProxyInstance(classLoader, new Class<?>[]{type}, new InvocationHandler() {
            @Override
            public Object invoke(Object target, Method method, Object[] args) throws Throwable {
                for (Object plugin : getPlugins()) {
                    if (type.isInstance(plugin)) {
                        Utils.invoke(plugin, method, 0, args);
                    }
                }
                return null;
            }
        });
        return type.cast(proxy);
    }

    private void setMonochromeOnColorAwarePlugins(Object plugin) {
        if (plugin instanceof ColorAware) {
            ColorAware colorAware = (ColorAware) plugin;
            colorAware.setMonochrome(monochrome);
        }
    }

    private void setStrictOnStrictAwarePlugins(Object plugin) {
        if (plugin instanceof StrictAware) {
            StrictAware strictAware = (StrictAware) plugin;
            strictAware.setStrict(strict);
        }
    }

    public List<String> getGlue() {
        return glue;
    }

    public boolean isStrict() {
        return strict;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public List<String> getFeaturePaths() {
        return featurePaths;
    }

    public void addPlugin(Object plugin) {
        plugins.add(plugin);
    }

    public List<Object> getFilters() {
        return filters;
    }

    public boolean isMonochrome() {
        return monochrome;
    }

    public SnippetType getSnippetType() {
        return snippetType;
    }
}
