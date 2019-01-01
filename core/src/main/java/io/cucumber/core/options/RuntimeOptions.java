package io.cucumber.core.options;

import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import gherkin.IGherkinDialectProvider;
import io.cucumber.core.api.options.SnippetType;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.Resource;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.model.PathWithLines;
import io.cucumber.core.util.FixJava;
import io.cucumber.core.util.Mapper;
import io.cucumber.datatable.DataTable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.cucumber.core.util.FixJava.join;
import static io.cucumber.core.util.FixJava.map;
import static java.util.Arrays.asList;

// IMPORTANT! Make sure USAGE.txt is always uptodate if this class changes.
public class RuntimeOptions implements FeatureOptions, FilterOptions, PluginOptions, RunnerOptions {

    static final String VERSION = ResourceBundle.getBundle("io.cucumber.core.version").getString("cucumber-jvm.version");
    private static final String USAGE_RESOURCE = "/io/cucumber/core/api/cli/USAGE.txt";
    private static final Pattern RERUN_PATH_SPECIFICATION = Pattern.compile("(?m:^| |)(.*?\\.feature(?:(?::\\d+)*))");

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
    private final List<String> tagExpressions = new ArrayList<String>();
    private final List<Pattern> nameFilters = new ArrayList<Pattern>();
    private final Map<String, List<Long>> lineFilters = new HashMap<String, List<Long>>();
    private final List<String> featurePaths = new ArrayList<String>();

    private final List<String> junitOptions = new ArrayList<String>();
    private boolean dryRun;
    private boolean strict = false;
    private boolean monochrome = false;
    private boolean wip = false;
    private SnippetType snippetType = SnippetType.UNDERSCORE;
    private int threads = 1;

    private final List<Plugin> formatters = new ArrayList<>();
    private final List<Plugin> stepDefinitionReporters = new ArrayList<>();
    private final List<Plugin> summaryPrinters = new ArrayList<>();

    private final ResourceLoader resourceLoader;

    /**
     * Create a new instance from a list of options, for example:
     * <pre>{@code Arrays.asList("--name", "the fox", "--plugin", "pretty", "--strict");}</pre>
     *
     * @param resourceLoader used to load rerun files
     * @param argv           the arguments
     */
    public RuntimeOptions(ResourceLoader resourceLoader, List<String> argv) {
        this(resourceLoader, Env.INSTANCE, argv);
    }

    public RuntimeOptions(ResourceLoader resourceLoader, Env env, List<String> argv) {
        this.resourceLoader = resourceLoader;
        argv = new ArrayList<String>(argv); // in case the one passed in is unmodifiable.
        parse(argv);

        String cucumberOptionsFromEnv = env.get("cucumber.options");
        if (cucumberOptionsFromEnv != null) {
            parse(ShellWords.parse(cucumberOptionsFromEnv));
        }

        if (formatters.isEmpty()) {
            formatters.add(PluginOption.parse("progress"));
        }
        if (summaryPrinters.isEmpty()) {
            summaryPrinters.add(PluginOption.parse("default_summary"));
        }
    }

    public boolean isMultiThreaded() {
        return threads > 1;
    }

    public RuntimeOptions noSummaryPrinter() {
        summaryPrinters.clear();
        return this;
    }

    private void parse(List<String> args) {
        List<String> parsedTagExpressions = new ArrayList<String>();
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
            } else if (arg.equals("--threads")) {
                String threads = args.remove(0);
                this.threads = Integer.parseInt(threads);
                if (this.threads < 1) {
                    throw new CucumberException("--threads must be > 0");
                }
            } else if (arg.equals("--glue") || arg.equals("-g")) {
                String gluePath = args.remove(0);
                parsedGlue.add(gluePath);
            } else if (arg.equals("--tags") || arg.equals("-t")) {
                parsedTagExpressions.add(args.remove(0));
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
            } else if (arg.startsWith("-")) {
                printUsage();
                throw new CucumberException("Unknown option: " + arg);
            } else if (arg.startsWith("@")) {
                processPathWitheLinesFromRerunFile(parsedLineFilters, parsedFeaturePaths, arg.substring(1));
            } else {
                processPathWithLines(parsedLineFilters, parsedFeaturePaths, new PathWithLines(arg));
            }
        }
        if (!parsedTagExpressions.isEmpty() || !parsedNameFilters.isEmpty() || !parsedLineFilters.isEmpty() || haveLineFilters(parsedFeaturePaths)) {
            tagExpressions.clear();
            tagExpressions.addAll(parsedTagExpressions);
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

        parsedPluginData.updateFormatters(formatters);
        parsedPluginData.updateStepDefinitionReporters(stepDefinitionReporters);
        parsedPluginData.updateSummaryPrinters(summaryPrinters);
    }

    private void addLineFilters(Map<String, List<Long>> parsedLineFilters, String key, List<Long> lines) {
        if (parsedLineFilters.containsKey(key)) {
            parsedLineFilters.get(key).addAll(lines);
        } else {
            parsedLineFilters.put(key, lines);
        }
    }

    private void processPathWithLines(Map<String, List<Long>> parsedLineFilters, List<String> parsedFeaturePaths, PathWithLines pathWithLines) {
        parsedFeaturePaths.add(pathWithLines.path);
        if (!pathWithLines.lines.isEmpty()) {
            String key = pathWithLines.path.replace("classpath:", "");
            addLineFilters(parsedLineFilters, key, pathWithLines.lines);
        }
    }

    private void processPathWitheLinesFromRerunFile(Map<String, List<Long>> parsedLineFilters, List<String> parsedFeaturePaths, String rerunPath) {
        for (PathWithLines pathWithLines : loadRerunFile(rerunPath)) {
            processPathWithLines(parsedLineFilters, parsedFeaturePaths, pathWithLines);
        }
    }

    private List<PathWithLines> loadRerunFile(String rerunPath) {
        List<PathWithLines> featurePaths = new ArrayList<>();
        Iterable<Resource> resources = resourceLoader.resources(rerunPath, null);
        for (Resource resource : resources) {
            String source = read(resource);
            if (!source.isEmpty()) {
                Matcher matcher = RERUN_PATH_SPECIFICATION.matcher(source);
                while (matcher.find()) {
                    featurePaths.add(new PathWithLines(matcher.group(1)));
                }
            }
        }
        return featurePaths;
    }

    private static String read(Resource resource) {
        try {
            return FixJava.readReader(new InputStreamReader(resource.getInputStream()));
        } catch (IOException e) {
            throw new CucumberException("Failed to read resource:" + resource.getPath(), e);
        }
    }

    private boolean haveLineFilters(List<String> parsedFeaturePaths) {
        for (String pathName : parsedFeaturePaths) {
            if (PathWithLines.hasLineFilters(pathName)) {
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
    public List<String> getGlue() {
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
    public List<String> getFeaturePaths() {
        return featurePaths;
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
    public Map<String, List<Long>> getLineFilters() {
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

    static final class ParsedPluginData {
        private ParsedPlugins formatters = new ParsedPlugins();
        private ParsedPlugins stepDefinitionReporters = new ParsedPlugins();
        private ParsedPlugins summaryPrinters = new ParsedPlugins();

        void addPluginName(String name, boolean isAddPlugin) {
            PluginOption pluginOption = PluginOption.parse(name);
            if (pluginOption.isStepDefinitionReporter()) {
                stepDefinitionReporters.addName(pluginOption, isAddPlugin);
            } else if (pluginOption.isSummaryPrinter()) {
                summaryPrinters.addName(pluginOption, isAddPlugin);
            } else if (pluginOption.isFormatter()) {
                formatters.addName(pluginOption, isAddPlugin);
            } else {
                throw new CucumberException("Unrecognized plugin: " + name);
            }
        }

        void updateFormatters(List<Plugin> formatter) {
            this.formatters.updateNameList(formatter);
        }

        void updateStepDefinitionReporters(List<Plugin> stepDefintionReporter) {
            stepDefinitionReporters.updateNameList(stepDefintionReporter);
        }

        void updateSummaryPrinters(List<Plugin> pluginSummaryPrinterNames) {
            summaryPrinters.updateNameList(pluginSummaryPrinterNames);
        }

        private static class ParsedPlugins {
            private List<Plugin> names = new ArrayList<>();
            private boolean clobber = false;

            void addName(Plugin name, boolean isAddOption) {
                names.add(name);
                if (!isAddOption) {
                    clobber = true;
                }
            }

            void updateNameList(List<Plugin> nameList) {
                if (!names.isEmpty()) {
                    if (clobber) {
                        nameList.clear();
                    }
                    nameList.addAll(names);
                }
            }
        }
    }
}
