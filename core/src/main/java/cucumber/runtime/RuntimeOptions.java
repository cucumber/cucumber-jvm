package cucumber.runtime;

import cucumber.api.SnippetType;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.order.OrderType;
import cucumber.runtime.order.OrderTypeFactory;
import io.cucumber.core.model.FeaturePath;
import io.cucumber.core.model.FeatureWithLines;
import cucumber.util.FixJava;
import cucumber.util.Mapper;
import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import gherkin.IGherkinDialectProvider;
import io.cucumber.core.model.GluePath;
import io.cucumber.core.model.RerunLoader;
import io.cucumber.core.options.FeatureOptions;
import io.cucumber.core.options.FilterOptions;
import io.cucumber.core.options.PluginOptions;
import io.cucumber.core.options.RunnerOptions;
import io.cucumber.datatable.DataTable;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static cucumber.util.FixJava.join;
import static cucumber.util.FixJava.map;
import static java.util.Arrays.asList;

// IMPORTANT! Make sure USAGE.txt is always uptodate if this class changes.
public class RuntimeOptions implements FeatureOptions, FilterOptions, PluginOptions, RunnerOptions {
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
    private final List<URI> glue = new ArrayList<>();
    private final List<String> tagFilters = new ArrayList<String>();
    private final List<Pattern> nameFilters = new ArrayList<Pattern>();
    private final Map<URI, Set<Integer>> lineFilters = new HashMap<>();
    private final SortedSet<URI> featurePaths = new TreeSet<>();

    private final List<String> junitOptions = new ArrayList<String>();
    private final RerunLoader rerunLoader;
    private boolean dryRun;
    private boolean strict = false;
    private boolean monochrome = false;
    private boolean wip = false;
    private SnippetType snippetType = SnippetType.UNDERSCORE;
    private int threads = 1;
    private OrderType orderType = OrderTypeFactory.createNoneOrderType();

    private final List<String> pluginFormatterNames = new ArrayList<String>();
    private final List<String> pluginStepDefinitionReporterNames = new ArrayList<String>();
    private final List<String> pluginSummaryPrinterNames = new ArrayList<String>();


    /**
     * Create a new instance from a string of options, for example:
     * <p/>
     * <pre<{@code "--name 'the fox' --plugin pretty --strict"}</pre>
     *
     * @param argv the arguments
     */
    public RuntimeOptions(String argv) {
        this(Shellwords.parse(argv));
    }

    /**
     * Create a new instance from a list of options, for example:
     * <p/>
     * <pre<{@code Arrays.asList("--name", "the fox", "--plugin", "pretty", "--strict");}</pre>
     *
     * @param argv the arguments
     */
    public RuntimeOptions(List<String> argv) {
        this(Env.INSTANCE, argv);
    }

    public RuntimeOptions(Env env, List<String> argv) {
        this(new MultiLoader(RuntimeOptions.class.getClassLoader()), env, argv);
    }


    RuntimeOptions(ResourceLoader resourceLoader, Env env, List<String> argv) {
        this.rerunLoader= new RerunLoader(resourceLoader);
        argv = new ArrayList<>(argv); // in case the one passed in is unmodifiable.
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

    public boolean isMultiThreaded() {
        return threads > 1;
    }

    public RuntimeOptions noSummaryPrinter() {
        pluginSummaryPrinterNames.clear();
        return this;
    }

    private void parse(List<String> args) {
        List<String> parsedTagFilters = new ArrayList<String>();
        List<Pattern> parsedNameFilters = new ArrayList<Pattern>();
        Map<URI, Set<Integer>> parsedLineFilters = new HashMap<>();
        List<URI> parsedFeaturePaths = new ArrayList<>();
        List<URI> parsedGlue = new ArrayList<>();
        ParsedPluginData parsedPluginData = new ParsedPluginData();
        List<String> parsedJunitOptions = new ArrayList<String>();
        Map<String, String> orderTypeData = new HashMap<>();

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
            } else if (arg.equals("--threads")) {
                String threads = args.remove(0);
                this.threads = Integer.parseInt(threads);
                if (this.threads < 1) {
                    throw new CucumberException("--threads must be > 0");
                }
            } else if (arg.equals("--glue") || arg.equals("-g")) {
                String gluePath = args.remove(0);
                parsedGlue.add(GluePath.parse(gluePath));
            } else if (arg.equals("--tags") || arg.equals("-t")) {
                parsedTagFilters.add(args.remove(0));
            } else if (arg.equals("--plugin") || arg.equals("--add-plugin") || arg.equals("-p")) {
                parsedPluginData.addPluginName(args.remove(0), arg.equals("--add-plugin"));
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
                parsedJunitOptions.addAll(asList(arg.substring("--junit,".length()).split(",")));
            } else if (arg.equals("--wip") || arg.equals("-w")) {
                wip = true;
            } else if (arg.equals("--order")) {
            	orderTypeData.put(OrderType.TYPE_NAME, args.remove(0));
            } else if (arg.equals("--count")) {
            	orderTypeData.put(OrderType.PROPERTY_COUNT, args.remove(0));
            } else if (arg.startsWith("-")) {
                printUsage();
                throw new CucumberException("Unknown option: " + arg);
            } else if (arg.startsWith("@")) {
                URI rerunFile = FeaturePath.parse(arg.substring(1));
                processPathWitheLinesFromRerunFile(parsedLineFilters, parsedFeaturePaths, rerunFile);
            } else if (!arg.isEmpty()){
                FeatureWithLines featureWithLines = FeatureWithLines.parse(arg);
                processFeatureWithLines(parsedLineFilters, parsedFeaturePaths, featureWithLines);
            }
        }
        if (!parsedTagFilters.isEmpty() || !parsedNameFilters.isEmpty() || !parsedLineFilters.isEmpty()) {
            tagFilters.clear();
            tagFilters.addAll(parsedTagFilters);
            nameFilters.clear();
            nameFilters.addAll(parsedNameFilters);
            lineFilters.clear();
            for (URI path : parsedLineFilters.keySet()) {
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
        if(orderTypeData.containsKey(OrderType.TYPE_NAME)) {
        	orderType = OrderTypeFactory.getOrderType(orderTypeData);
        	orderType.checkVariableValues(orderTypeData);
        }

        parsedPluginData.updatePluginFormatterNames(pluginFormatterNames);
        parsedPluginData.updatePluginStepDefinitionReporterNames(pluginStepDefinitionReporterNames);
        parsedPluginData.updatePluginSummaryPrinterNames(pluginSummaryPrinterNames);
    }

    private void addLineFilters(Map<URI, Set<Integer>> parsedLineFilters, URI key, Set<Integer> lines) {
        if(lines.isEmpty()){
            return;
        }
        if (parsedLineFilters.containsKey(key)) {
            parsedLineFilters.get(key).addAll(lines);
        } else {
            parsedLineFilters.put(key, new TreeSet<>(lines));
        }
    }

    private void processFeatureWithLines(Map<URI, Set<Integer>> parsedLineFilters, List<URI> parsedFeaturePaths, FeatureWithLines featureWithLines) {
        parsedFeaturePaths.add(featureWithLines.uri());
        addLineFilters(parsedLineFilters, featureWithLines.uri(), featureWithLines.lines());
    }

    private void processPathWitheLinesFromRerunFile(Map<URI, Set<Integer>> parsedLineFilters, List<URI> parsedFeaturePaths, URI rerunPath) {
        for (FeatureWithLines featureWithLines : rerunLoader.load(rerunPath)) {
            processFeatureWithLines(parsedLineFilters, parsedFeaturePaths, featureWithLines);
        }
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
        DataTable.create(table).print(builder);
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

    @Override
    public List<String> getPluginNames() {
        List<String> pluginNames = new ArrayList<>();
        pluginNames.addAll(pluginFormatterNames);
        pluginNames.addAll(pluginStepDefinitionReporterNames);
        pluginNames.addAll(pluginSummaryPrinterNames);
        return pluginNames;
    }

    @Override
    public List<URI> getGlue() {
        return glue;
    }

    @Override
    public boolean isStrict() {
        return strict;
    }

    @Override
    public boolean isDryRun() {
        return dryRun;
    }

    public boolean isWip() {
        return wip;
    }

    @Override
    public List<URI> getFeaturePaths() {
        return new ArrayList<>(featurePaths);
    }

    @Override
    public List<Pattern> getNameFilters() {
        return nameFilters;
    }

    @Override
    public List<String> getTagFilters() {
        return tagFilters;
    }

    @Override
    public Map<URI, Set<Integer>> getLineFilters() {
        return lineFilters;
    }

    @Override
    public boolean isMonochrome() {
        return monochrome;
    }

    @Override
    public SnippetType getSnippetType() {
        return snippetType;
    }

    public List<String> getJunitOptions() {
        return junitOptions;
    }

    public int getThreads() {
        return threads;
    }
    
    public OrderType getOrderType() {
    	return orderType;
    }

    class ParsedPluginData {
        ParsedOptionNames formatterNames = new ParsedOptionNames();
        ParsedOptionNames stepDefinitionReporterNames = new ParsedOptionNames();
        ParsedOptionNames summaryPrinterNames = new ParsedOptionNames();

        public void addPluginName(String name, boolean isAddPlugin) {
            if (PluginFactory.isStepDefinitionReporterName(name)) {
                stepDefinitionReporterNames.addName(name, isAddPlugin);
            } else if (PluginFactory.isSummaryPrinterName(name)) {
                summaryPrinterNames.addName(name, isAddPlugin);
            } else if (PluginFactory.isFormatterName(name)) {
                formatterNames.addName(name, isAddPlugin);
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

}
