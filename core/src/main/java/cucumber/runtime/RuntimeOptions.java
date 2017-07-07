package cucumber.runtime;

import cucumber.api.SnippetType;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.SummaryPrinter;
import cucumber.api.event.TestRunStarted;
import cucumber.api.formatter.ColorAware;
import cucumber.api.formatter.Formatter;
import cucumber.api.formatter.StrictAware;
import cucumber.runner.EventBus;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.PathWithLines;
import cucumber.runtime.table.TablePrinter;
import cucumber.util.FixJava;
import cucumber.util.Mapper;
import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import gherkin.IGherkinDialectProvider;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static cucumber.runtime.model.CucumberFeature.load;
import static cucumber.util.FixJava.join;
import static cucumber.util.FixJava.map;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

// IMPORTANT! Make sure USAGE.txt is always uptodate if this class changes.
public class RuntimeOptions {
    public static final String VERSION = ResourceBundle.getBundle("cucumber.version").getString("cucumber-jvm.version");
    public static final String USAGE_RESOURCE = "/cucumber/api/cli/USAGE.txt";

    static String usageText;

    private static final Mapper<String, String> QUOTE_MAPPER = new Mapper<String, String>() {
        @Override
        public String map(String o) {
            return '"' + o + '"';
        }
    };
    private static final Mapper<String, String> CODE_KEYWORD_MAPPER = new Mapper<String, String>() {
        @Override
        public String map(String keyword) {
            return keyword.replaceAll("[\\s',!]", "");
        }
    };

    private final List<String> glue = new ArrayList<String>();
    private final List<String> tagFilters = new ArrayList<String>();
    private final List<Pattern> nameFilters = new ArrayList<Pattern>();
    private final Map<String, List<Long>> lineFilters = new HashMap<String, List<Long>>();
    private final List<String> featurePaths = new ArrayList<String>();
    private final List<String> pluginFormatterNames = new ArrayList<String>();
    private final List<String> pluginStepDefinitionReporterNames = new ArrayList<String>();
    private final List<String> pluginSummaryPrinterNames = new ArrayList<String>();
    private final List<String> junitOptions = new ArrayList<String>();
    private final PluginFactory pluginFactory;
    private final List<Object> plugins = new ArrayList<Object>();
    private final List<XStreamConverter> converters = new ArrayList<XStreamConverter>();
    private boolean dryRun;
    private boolean strict = false;
    private boolean monochrome = false;
    private SnippetType snippetType = SnippetType.UNDERSCORE;
    private boolean pluginNamesInstantiated;
    private EventBus bus;

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
        this(Env.INSTANCE, pluginFactory, argv);
    }

    public RuntimeOptions(Env env, PluginFactory pluginFactory, List<String> argv) {
        this.pluginFactory = pluginFactory;

        argv = new ArrayList<String>(argv); // in case the one passed in is unmodifiable.
        parse(argv);

        String cucumberOptionsFromEnv = env.get("cucumber.options");
        if (cucumberOptionsFromEnv != null) {
            parse(Shellwords.parse(cucumberOptionsFromEnv));
        }

        if (pluginFormatterNames.isEmpty()) {
            pluginFormatterNames.add("progress");
        }
        if (pluginSummaryPrinterNames.isEmpty()) {
            pluginSummaryPrinterNames.add("default_summary");
        }
    }

    private void parse(List<String> args) {
        List<String> parsedTagFilters = new ArrayList<String>();
        List<Pattern> parsedNameFilters = new ArrayList<Pattern>();
        Map<String, List<Long>> parsedLineFilters = new HashMap<String, List<Long>>();
        List<String> parsedFeaturePaths = new ArrayList<String>();
        List<String> parsedGlue = new ArrayList<String>();
        ParsedPluginData parsedPluginData = new ParsedPluginData();
        List<String> parsedJunitOptions = new ArrayList<String>();

        while (!args.isEmpty()) {
            String arg = args.remove(0).trim();

            if (arg.equals("--help") || arg.equals("-h")) {
                printUsage();
                System.exit(0);
            } else if (arg.equals("--version") || arg.equals("-v")) {
                System.out.println(VERSION);
                System.exit(0);
            } else if (arg.equals("--i18n")) {
                String nextArg = args.remove(0);
                System.exit(printI18n(nextArg));
            } else if (arg.equals("--glue") || arg.equals("-g")) {
                String gluePath = args.remove(0);
                parsedGlue.add(gluePath);
            } else if (arg.equals("--tags") || arg.equals("-t")) {
                parsedTagFilters.add(args.remove(0));
            } else if (arg.equals("--plugin") || arg.equals("--add-plugin") || arg.equals("-p")) {
                parsedPluginData.addPluginName(args.remove(0), arg.equals("--add-plugin"));
            } else if (arg.equals("--format") || arg.equals("-f")) {
                System.err.println("WARNING: Cucumber-JVM's --format option is deprecated. Please use --plugin instead.");
                parsedPluginData.addPluginName(args.remove(0), true);
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
                parsedNameFilters.add(patternFilter);
            } else if (arg.startsWith("--junit,")) {
                for (String option : arg.substring("--junit,".length()).split(",")) {
                    parsedJunitOptions.add(option);
                }
            } else if (arg.startsWith("-")) {
                printUsage();
                throw new CucumberException("Unknown option: " + arg);
            } else {
                PathWithLines pathWithLines = new PathWithLines(arg);
                parsedFeaturePaths.add(pathWithLines.path);
                if (!pathWithLines.lines.isEmpty()) {
                    String key = pathWithLines.path.replace("classpath:", "");
                    addLineFilters(parsedLineFilters, key, pathWithLines.lines);
                }
            }
        }
        if (!parsedTagFilters.isEmpty() || !parsedNameFilters.isEmpty() || !parsedLineFilters.isEmpty() || haveLineFilters(parsedFeaturePaths)) {
            tagFilters.clear();
            tagFilters.addAll(parsedTagFilters);
            nameFilters.clear();
            nameFilters.addAll(parsedNameFilters);
            lineFilters.clear();
            for (String path : parsedLineFilters.keySet()) {
                lineFilters.put(path, parsedLineFilters.get(path));
            }
        }
        if (!parsedFeaturePaths.isEmpty()) {
            featurePaths.clear();
            featurePaths.addAll(parsedFeaturePaths);
        }

        if (!parsedGlue.isEmpty()) {
            glue.clear();
            glue.addAll(parsedGlue);
        }
        if (!parsedJunitOptions.isEmpty()) {
            junitOptions.clear();
            junitOptions.addAll(parsedJunitOptions);
        }

        parsedPluginData.updatePluginFormatterNames(pluginFormatterNames);
        parsedPluginData.updatePluginStepDefinitionReporterNames(pluginStepDefinitionReporterNames);
        parsedPluginData.updatePluginSummaryPrinterNames(pluginSummaryPrinterNames);
    }

    RuntimeOptions withConverters(List<XStreamConverter> converters) {
        this.converters.addAll(converters);
        return this;
    }

    private void addLineFilters(Map<String, List<Long>> parsedLineFilters, String key, List<Long> lines) {
        if (parsedLineFilters.containsKey(key)) {
            parsedLineFilters.get(key).addAll(lines);
        } else {
            parsedLineFilters.put(key, lines);
        }
    }

    private boolean haveLineFilters(List<String> parsedFeaturePaths) {
        for (String pathName : parsedFeaturePaths) {
            if (pathName.startsWith("@") || PathWithLines.hasLineFilters(pathName)) {
                return true;
            }
        }
        return false;
    }

    private void printUsage() {
        loadUsageTextIfNeeded();
        System.out.println(usageText);
    }

    static void loadUsageTextIfNeeded() {
        if (usageText == null) {
            try {
                Reader reader = new InputStreamReader(FixJava.class.getResourceAsStream(USAGE_RESOURCE), "UTF-8");
                usageText = FixJava.readReader(reader);
            } catch (Exception e) {
                usageText = "Could not load usage text: " + e.toString();
            }
        }
    }

    private int printI18n(String language) {
        IGherkinDialectProvider dialectProvider = new GherkinDialectProvider();
        List<String> languages = dialectProvider.getLanguages();

        if (language.equalsIgnoreCase("help")) {
            for (String code : languages) {
                System.out.println(code);
            }
            return 0;
        }
        if (languages.contains(language)) {
            return printKeywordsFor(dialectProvider.getDialect(language, null));
        }

        System.err.println("Unrecognised ISO language code");
        return 1;
    }

    private int printKeywordsFor(GherkinDialect dialect) {
        StringBuilder builder = new StringBuilder();
        TablePrinter printer = new TablePrinter();
        List<List<String>> table = new ArrayList<List<String>>();
        addKeywordRow(table, "feature", dialect.getFeatureKeywords());
        addKeywordRow(table, "background", dialect.getBackgroundKeywords());
        addKeywordRow(table, "scenario", dialect.getScenarioKeywords());
        addKeywordRow(table, "scenario outline", dialect.getScenarioOutlineKeywords());
        addKeywordRow(table, "examples", dialect.getExamplesKeywords());
        addKeywordRow(table, "given", dialect.getGivenKeywords());
        addKeywordRow(table, "when", dialect.getWhenKeywords());
        addKeywordRow(table, "then", dialect.getThenKeywords());
        addKeywordRow(table, "and", dialect.getAndKeywords());
        addKeywordRow(table, "but", dialect.getButKeywords());
        addCodeKeywordRow(table, "given", dialect.getGivenKeywords());
        addCodeKeywordRow(table, "when", dialect.getWhenKeywords());
        addCodeKeywordRow(table, "then", dialect.getThenKeywords());
        addCodeKeywordRow(table, "and", dialect.getAndKeywords());
        addCodeKeywordRow(table, "but", dialect.getButKeywords());
        printer.printTable(table, builder);
        System.out.println(builder.toString());
        return 0;
    }

    private void addCodeKeywordRow(List<List<String>> table, String key, List<String> keywords) {
        List<String> codeKeywordList = new ArrayList<String>(keywords);
        codeKeywordList.remove("* ");
        addKeywordRow(table, key + " (code)", map(codeKeywordList, CODE_KEYWORD_MAPPER));
    }

    private void addKeywordRow(List<List<String>> table, String key, List<String> keywords) {
        List<String> cells = asList(key, join(map(keywords, QUOTE_MAPPER), ", "));
        table.add(cells);
    }

    public List<CucumberFeature> cucumberFeatures(ResourceLoader resourceLoader, EventBus bus) {
        List<CucumberFeature> features = load(resourceLoader, featurePaths, System.out);
        getPlugins(); // to create the formatter objects
        bus.send(new TestRunStarted(bus.getTime()));
        for (CucumberFeature feature : features) {
            feature.sendTestSourceRead(bus);
        }
        return features;
    }

    public List<Object> getPlugins() {
        if (!pluginNamesInstantiated) {
            for (String pluginName : pluginFormatterNames) {
                Object plugin = pluginFactory.create(pluginName);
                plugins.add(plugin);
                setMonochromeOnColorAwarePlugins(plugin);
                setStrictOnStrictAwarePlugins(plugin);
                setEventBusFormatterPlugins(plugin);
            }
            for (String pluginName : pluginStepDefinitionReporterNames) {
                Object plugin = pluginFactory.create(pluginName);
                plugins.add(plugin);
            }
            for (String pluginName : pluginSummaryPrinterNames) {
                Object plugin = pluginFactory.create(pluginName);
                plugins.add(plugin);
            }
            pluginNamesInstantiated = true;
        }
        return plugins;
    }

    List<XStreamConverter> getConverters() {
        return unmodifiableList(converters);
    }

    public Formatter formatter(ClassLoader classLoader) {
        return pluginProxy(classLoader, Formatter.class);
    }

    public StepDefinitionReporter stepDefinitionReporter(ClassLoader classLoader) {
        return pluginProxy(classLoader, StepDefinitionReporter.class);
    }

    public SummaryPrinter summaryPrinter(ClassLoader classLoader) {
        return pluginProxy(classLoader, SummaryPrinter.class);
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
                        try {
                            Utils.invoke(plugin, method, 0, args);
                        } catch (Throwable t) {
                            if (!method.getName().equals("startOfScenarioLifeCycle") && !method.getName().equals("endOfScenarioLifeCycle")) {
                                // IntelliJ has its own formatter which doesn't yet implement these methods.
                                throw t;
                            }
                        }
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

    private void setEventBusFormatterPlugins(Object plugin) {
        if (plugin instanceof Formatter && bus != null) {
            Formatter formatter = (Formatter) plugin;
            formatter.setEventPublisher(bus);
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
        if (plugin instanceof Formatter) {
            setEventBusFormatterPlugins(plugin);
        }
    }

    public List<Pattern> getNameFilters() {
        return nameFilters;
    }

    public List<String> getTagFilters() {
        return tagFilters;
    }

    public Map<String, List<Long>> getLineFilters(ResourceLoader resourceLoader) {
        processRerunFiles(resourceLoader);
        return lineFilters;
    }

    private void processRerunFiles(ResourceLoader resourceLoader) {
        for (String featurePath : featurePaths) {
            if (featurePath.startsWith("@")) {
                for (String path : CucumberFeature.loadRerunFile(resourceLoader, featurePath.substring(1))) {
                    PathWithLines pathWithLines = new PathWithLines(path);
                    addLineFilters(lineFilters, pathWithLines.path, pathWithLines.lines);
                }
            }
        }
    }

    public boolean isMonochrome() {
        return monochrome;
    }

    public SnippetType getSnippetType() {
        return snippetType;
    }

    public List<String> getJunitOptions() {
        return junitOptions;
    }

    class ParsedPluginData {
        ParsedOptionNames formatterNames = new ParsedOptionNames();
        ParsedOptionNames stepDefinitionReporterNames = new ParsedOptionNames();
        ParsedOptionNames summaryPrinterNames = new ParsedOptionNames();

        public void addPluginName(String name, boolean isAddPlugin) {
            if (PluginFactory.isFormatterName(name)) {
                formatterNames.addName(name, isAddPlugin);
            } else if (PluginFactory.isStepDefinitionResporterName(name)) {
                stepDefinitionReporterNames.addName(name, isAddPlugin);
            } else if (PluginFactory.isSummaryPrinterName(name)) {
                summaryPrinterNames.addName(name, isAddPlugin);
            } else {
                throw new CucumberException("Unrecognized plugin: " + name);
            }
        }

        public void updatePluginFormatterNames(List<String> pluginFormatterNames) {
            formatterNames.updateNameList(pluginFormatterNames);
        }

        public void updatePluginStepDefinitionReporterNames(List<String> pluginStepDefinitionReporterNames) {
            stepDefinitionReporterNames.updateNameList(pluginStepDefinitionReporterNames);
        }

        public void updatePluginSummaryPrinterNames(List<String> pluginSummaryPrinterNames) {
            summaryPrinterNames.updateNameList(pluginSummaryPrinterNames);
        }
    }

    class ParsedOptionNames {
        private List<String> names = new ArrayList<String>();
        private boolean clobber = false;

        public void addName(String name, boolean isAddOption) {
            names.add(name);
            if (!isAddOption) {
                clobber = true;
            }
        }

        public void updateNameList(List<String> nameList) {
            if (!names.isEmpty()) {
                if (clobber) {
                    nameList.clear();
                }
                nameList.addAll(names);
            }
        }
    }

    void setEventBus(EventBus bus) {
        this.bus = bus;
    }
}
