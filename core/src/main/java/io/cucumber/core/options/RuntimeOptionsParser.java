package io.cucumber.core.options;

import cucumber.api.SnippetType;
import cucumber.runtime.CucumberException;
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
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cucumber.util.FixJava.join;
import static cucumber.util.FixJava.map;
import static java.util.Arrays.asList;

final class RuntimeOptionsParser {
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
    private static final String USAGE_RESOURCE = "/io/cucumber/core/options/USAGE.txt";
    static String usageText;
    private final RerunLoader rerunLoader;

    RuntimeOptionsParser(RerunLoader rerunLoader) {
        this.rerunLoader = rerunLoader;
    }

    RuntimeOptionsBuilder parse(List<String> args) {
        args = new ArrayList<>(args);
        RuntimeOptionsBuilder parsedOptions = new RuntimeOptionsBuilder();

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
                int threads = Integer.parseInt(args.remove(0));
                if (threads < 1) {
                    throw new CucumberException("--threads must be > 0");
                }
                parsedOptions.setThreads(threads);
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
                parsedOptions.setStrict(!arg.startsWith("--no-"));
            } else if (arg.equals("--no-monochrome") || arg.equals("--monochrome") || arg.equals("-m")) {
                parsedOptions.setMonochrome(!arg.startsWith("--no-"));
            } else if (arg.equals("--snippets")) {
                String nextArg = args.remove(0);
                if("underscore".equals(nextArg)){
                    parsedOptions.setSnippetType(SnippetType.UNDERSCORE);
                } else if ("camelcase".equals(nextArg)) {
                    parsedOptions.setSnippetType(SnippetType.CAMELCASE);
                } else {
                    throw new CucumberException("Unrecognized SnippetType " + nextArg);
                }
            } else if (arg.equals("--name") || arg.equals("-n")) {
                String nextArg = args.remove(0);
                Pattern pattern = Pattern.compile(nextArg);
                parsedOptions.addNameFilter(pattern);
            } else if (arg.startsWith("--junit,")) {
                for (String parsedOption : arg.substring("--junit,".length()).split(",")) {
                    parsedOptions.addJunitOption(parsedOption);
                }
            } else if (arg.equals("--wip") || arg.equals("-w")) {
                parsedOptions.setWip(true);
            } else if (arg.equals("--order")) {
                parsedOptions.setPickleOrder(parsePickleOrder(args.remove(0)));
            } else if (arg.equals("--count")) {
                int count = Integer.parseInt(args.remove(0));
                if (count < 1) {
                    throw new CucumberException("--count must be > 0");
                }
                parsedOptions.setCount(count);
            } else if (arg.startsWith("-")) {
                printUsage();
                throw new CucumberException("Unknown option: " + arg);
            } else if (arg.startsWith("@")) {
                parsedOptions.setIsRerun(true);
                URI rerunFile = FeaturePath.parse(arg.substring(1));
                for (FeatureWithLines featureWithLines : rerunLoader.load(rerunFile)) {
                    parsedOptions.addFeature(featureWithLines);
                }
            } else if (!arg.isEmpty()) {
                FeatureWithLines featureWithLines = FeatureWithLines.parse(arg);
                parsedOptions.addFeature(featureWithLines);
            }
        }
        return parsedOptions;
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
        List<List<String>> table = new ArrayList<>();
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
        List<String> codeKeywordList = new ArrayList<>(keywords);
        codeKeywordList.remove("* ");
        addKeywordRow(table, key + " (code)", map(codeKeywordList, CODE_KEYWORD_MAPPER));
    }

    private static void addKeywordRow(List<List<String>> table, String key, List<String> keywords) {
        List<String> cells = asList(key, join(map(keywords, QUOTE_MAPPER), ", "));
        table.add(cells);
    }


}
