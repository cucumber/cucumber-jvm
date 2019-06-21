package cucumber.runtime;

import cucumber.api.SnippetType;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.order.PickleOrder;
import cucumber.runtime.order.StandardPickleOrders;
import cucumber.util.FixJava;
import cucumber.util.Mapper;
import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import gherkin.IGherkinDialectProvider;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.model.FeaturePath;
import io.cucumber.core.model.FeatureWithLines;
import io.cucumber.core.model.GluePath;
import io.cucumber.core.model.RerunLoader;
import io.cucumber.datatable.DataTable;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cucumber.util.FixJava.join;
import static cucumber.util.FixJava.map;
import static java.util.Arrays.asList;

class RuntimeOptionsParser {
    private static final Logger log = LoggerFactory.getLogger(RuntimeOptionsParser.class);

    static final String VERSION = ResourceBundle.getBundle("cucumber.version").getString("cucumber-jvm.version");
    private static final Pattern RANDOM_AND_SEED_PATTERN = Pattern.compile("random(?::(\\d+))?");

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
    // IMPORTANT! Make sure USAGE.txt is always uptodate if this class changes.
    private static final String USAGE_RESOURCE = "/cucumber/api/cli/USAGE.txt";
    static String usageText;
    private final RerunLoader rerunLoader;

    public RuntimeOptionsParser(RerunLoader rerunLoader) {
        this.rerunLoader = rerunLoader;
    }

    ParsedOptions parse(List<String> args) {
        ParsedOptions parsedOptions = new ParsedOptions();

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
                parsedOptions.parsedThreads = Integer.parseInt(args.remove(0));
                if (parsedOptions.parsedThreads < 1) {
                    throw new CucumberException("--threads must be > 0");
                }
            } else if (arg.equals("--glue") || arg.equals("-g")) {
                String gluePath = args.remove(0);
                URI parse = GluePath.parse(gluePath);
                parsedOptions.addGlue(parse);
            } else if (arg.equals("--tags") || arg.equals("-t")) {
                parsedOptions.addTagFilter(args.remove(0));
            } else if (arg.equals("--plugin") || arg.equals("--add-plugin") || arg.equals("-p")) {
                parsedOptions.addPluginName(args.remove(0), arg.equals("--add-plugin"));
            } else if (arg.equals("--no-dry-run") || arg.equals("--dry-run") || arg.equals("-d")) {
                parsedOptions.setDryRun(!arg.startsWith("--no-"));
            } else if (arg.equals("--no-strict") || arg.equals("--strict") || arg.equals("-s")) {
                parsedOptions.parsedStrict = !arg.startsWith("--no-");
            } else if (arg.equals("--no-monochrome") || arg.equals("--monochrome") || arg.equals("-m")) {
                parsedOptions.setMonochrome(!arg.startsWith("--no-"));
            } else if (arg.equals("--snippets")) {
                String nextArg = args.remove(0);
                parsedOptions.setSnippetType(SnippetType.fromString(nextArg));
            } else if (arg.equals("--name") || arg.equals("-n")) {
                String nextArg = args.remove(0);
                Pattern pattern = Pattern.compile(nextArg);
                parsedOptions.addNameFilter(pattern);
            } else if (arg.startsWith("--junit,")) {
                parsedOptions.parsedJunitOptions.addAll(asList(arg.substring("--junit,".length()).split(",")));
            } else if (arg.equals("--wip") || arg.equals("-w")) {
                parsedOptions.parsedWip = true;
            } else if (arg.equals("--order")) {
                parsedOptions.parsedPickleOrder = parsePickleOrder(args.remove(0));
            } else if (arg.equals("--count")) {
                parsedOptions.parsedCount = Integer.parseInt(args.remove(0));
                if (parsedOptions.parsedCount < 1) {
                    throw new CucumberException("--count must be > 0");
                }
            } else if (arg.startsWith("-")) {
                printUsage();
                throw new CucumberException("Unknown option: " + arg);
            } else if (arg.startsWith("@")) {
                parsedOptions.parsedIsRerun = true;
                URI rerunFile = FeaturePath.parse(arg.substring(1));
                processPathWitheLinesFromRerunFile(parsedOptions, rerunFile);
            } else if (!arg.isEmpty()) {
                FeatureWithLines featureWithLines = FeatureWithLines.parse(arg);
                parsedOptions.addFeature(featureWithLines);
            }
        }
        return parsedOptions;
    }


    private void processPathWitheLinesFromRerunFile(ParsedOptions parsedOptions, URI rerunPath) {
        for (FeatureWithLines featureWithLines : rerunLoader.load(rerunPath)) {
            parsedOptions.addFeature(featureWithLines);
        }
    }

    private static PickleOrder parsePickleOrder(String argument) {

        if ("reverse".equals(argument)) {
            return StandardPickleOrders.reverseLexicalUriOrder();
        }

        Matcher matcher = RANDOM_AND_SEED_PATTERN.matcher(argument);
        if (matcher.matches()) {
            long seed = Math.abs(new Random().nextLong());
            String seedString = matcher.group(1);
            if (seedString != null) {
                seed = Long.parseLong(seedString);
            } else {
                log.info("Using random scenario order. Seed: " + seed);
            }

            return StandardPickleOrders.random(seed);
        }

        throw new CucumberException("Invalid order. Must be either reverse, random or random:<long>");
    }

    private static void printUsage() {
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

    private static int printI18n(String language) {
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




    private static int printKeywordsFor(GherkinDialect dialect) {
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

    private static void addCodeKeywordRow(List<List<String>> table, String key, List<String> keywords) {
        List<String> codeKeywordList = new ArrayList<String>(keywords);
        codeKeywordList.remove("* ");
        addKeywordRow(table, key + " (code)", map(codeKeywordList, CODE_KEYWORD_MAPPER));
    }

    private static void addKeywordRow(List<List<String>> table, String key, List<String> keywords) {
        List<String> cells = asList(key, join(map(keywords, QUOTE_MAPPER), ", "));
        table.add(cells);
    }


    static class ParsedOptions {
        private List<String> parsedTagFilters = new ArrayList<>();
        private List<Pattern> parsedNameFilters = new ArrayList<>();
        private Map<URI, Set<Integer>> parsedLineFilters = new HashMap<>();
        private List<URI> parsedFeaturePaths = new ArrayList<>();
        private List<URI> parsedGlue = new ArrayList<>();
        private ParsedPluginData parsedPluginData = new ParsedPluginData();
        private List<String> parsedJunitOptions = new ArrayList<>();
        private boolean parsedIsRerun = false;
        private Integer parsedThreads = null;
        private Boolean parsedDryRun = null;
        private Boolean parsedStrict = null;
        private Boolean parsedMonochrome = null;
        private SnippetType parsedSnippetType = null;
        private Boolean parsedWip = null;
        private PickleOrder parsedPickleOrder = null;
        private Integer parsedCount = null;

        public void addFeature(FeatureWithLines featureWithLines) {
            parsedFeaturePaths.add(featureWithLines.uri());
            addLineFilters(featureWithLines);
        }

        public void addGlue(URI glue) {
            parsedGlue.add(glue);
        }

        public void addJunitOption(String junitOption) {
            this.parsedJunitOptions.add(junitOption);
        }

        private void addLineFilters(FeatureWithLines featureWithLines) {
            URI key = featureWithLines.uri();
            Set<Integer> lines = featureWithLines.lines();
            if (lines.isEmpty()) {
                return;
            }
            if (this.parsedLineFilters.containsKey(key)) {
                this.parsedLineFilters.get(key).addAll(lines);
            } else {
                this.parsedLineFilters.put(key, new TreeSet<>(lines));
            }
        }

        public void addNameFilter(Pattern pattern) {
            this.parsedNameFilters.add(pattern);
        }

        public void addPluginName(String name, boolean isAddPlugin) {
            this.parsedPluginData.addPluginName(name, isAddPlugin);
        }

        public void addTagFilter(String tagExpression) {
            this.parsedTagFilters.add(tagExpression);
        }

        void apply(RuntimeOptions runtimeOptions) {
            if (this.parsedThreads != null) {
                runtimeOptions.setThreads(this.parsedThreads);
            }

            if (this.parsedDryRun != null) {
                runtimeOptions.setDryRun(this.parsedDryRun);
            }

            if (this.parsedStrict != null) {
                runtimeOptions.setStrict(this.parsedStrict);
            }

            if (this.parsedMonochrome != null) {
                runtimeOptions.setMonochrome(this.parsedMonochrome);
            }

            if (this.parsedSnippetType != null) {
                runtimeOptions.setSnippetType(this.parsedSnippetType);
            }

            if (this.parsedWip != null) {
                runtimeOptions.setWip(this.parsedWip);
            }

            if (this.parsedPickleOrder != null) {
                runtimeOptions.setPickleOrder(this.parsedPickleOrder);
            }

            if (this.parsedCount != null) {
                runtimeOptions.setCount(this.parsedCount);
            }

            if (this.parsedIsRerun || !this.parsedFeaturePaths.isEmpty()) {
                runtimeOptions.setFeaturePaths(Collections.<URI>emptyList());
                runtimeOptions.setLineFilters(Collections.<URI, Set<Integer>>emptyMap());
            }
            if (!this.parsedTagFilters.isEmpty() || !this.parsedNameFilters.isEmpty() || !this.parsedLineFilters.isEmpty()) {
                runtimeOptions.setTagFilters(this.parsedTagFilters);
                runtimeOptions.setNameFilters(this.parsedNameFilters);
                runtimeOptions.setLineFilters(this.parsedLineFilters);
            }
            if (!this.parsedFeaturePaths.isEmpty()) {
                runtimeOptions.setFeaturePaths(this.parsedFeaturePaths);
            }

            if (!this.parsedGlue.isEmpty()) {
                runtimeOptions.setGlue(this.parsedGlue);
            }
            if (!this.parsedJunitOptions.isEmpty()) {
                runtimeOptions.setJunitOptions(this.parsedJunitOptions);
            }

            this.parsedPluginData.updatePluginFormatterNames(runtimeOptions.getPluginFormatterNames());
            this.parsedPluginData.updatePluginStepDefinitionReporterNames(runtimeOptions.getPluginStepDefinitionReporterNames());
            this.parsedPluginData.updatePluginSummaryPrinterNames(runtimeOptions.getPluginSummaryPrinterNames());
        }

        public void setDryRun(boolean dryRun) {
            this.parsedDryRun = dryRun;
        }

        public void setMonochrome(boolean monochrome) {
            this.parsedMonochrome = monochrome;
        }


        public void setSnippetType(SnippetType snippetType) {
            this.parsedSnippetType = snippetType;
        }

        public void setStrict(boolean strict) {
            this.parsedStrict = true;
        }
    }

    static class ParsedPluginData {
        ParsedOptionNames formatterNames = new ParsedOptionNames();
        ParsedOptionNames stepDefinitionReporterNames = new ParsedOptionNames();
        ParsedOptionNames summaryPrinterNames = new ParsedOptionNames();

        void addPluginName(String name, boolean isAddPlugin) {
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

        void updatePluginFormatterNames(List<String> pluginFormatterNames) {
            formatterNames.updateNameList(pluginFormatterNames);
        }

        void updatePluginStepDefinitionReporterNames(List<String> pluginStepDefinitionReporterNames) {
            stepDefinitionReporterNames.updateNameList(pluginStepDefinitionReporterNames);
        }

        void updatePluginSummaryPrinterNames(List<String> pluginSummaryPrinterNames) {
            summaryPrinterNames.updateNameList(pluginSummaryPrinterNames);
        }
    }

    static class ParsedOptionNames {
        private List<String> names = new ArrayList<>();
        private boolean clobber = false;

        void addName(String name, boolean isAddOption) {
            names.add(name);
            if (!isAddOption) {
                clobber = true;
            }
        }

        void updateNameList(List<String> nameList) {
            if (!names.isEmpty()) {
                if (clobber) {
                    nameList.clear();
                }
                nameList.addAll(names);
            }
        }
    }
}
