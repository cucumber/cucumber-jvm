package io.cucumber.core.options;

import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import gherkin.IGherkinDialectProvider;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;
import io.cucumber.datatable.DataTable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.cucumber.core.options.ObjectFactoryParser.parseObjectFactory;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

final class RuntimeOptionsParser {

    static final String VERSION = ResourceBundle.getBundle("io.cucumber.core.version").getString("cucumber-jvm.version");

    // IMPORTANT! Make sure USAGE.txt is always uptodate if this class changes.
    private static final String USAGE_RESOURCE = "/io/cucumber/core/options/USAGE.txt";
    static String usageText;

    RuntimeOptionsParser() {
    }

    private static void printUsage() {
        loadUsageTextIfNeeded();
        System.out.println(usageText);
    }

    static void loadUsageTextIfNeeded() {
        if (usageText == null) {
            InputStream usageResourceStream = RuntimeOptionsParser.class.getResourceAsStream(USAGE_RESOURCE);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(usageResourceStream, UTF_8))) {
                usageText = br.lines().collect(joining(System.lineSeparator()));
            } catch (Exception e) {
                usageText = "Could not load usage text: " + e.toString();
            }
        }
    }

    private static int printI18n(String language) {
        IGherkinDialectProvider dialectProvider = new GherkinDialectProvider();
        List<String> languages = dialectProvider.getLanguages();

        if (language.equalsIgnoreCase("help")) {
            if (language.equalsIgnoreCase("help")) {
                List<GherkinDialect> dialects = new ArrayList<>();
                for (String code : languages) {
                    GherkinDialect dialect = dialectProvider.getDialect(code, null);
                    dialects.add(dialect);
                }

                int widestLanguage = findWidest(dialects, GherkinDialect::getLanguage);
                int widestName = findWidest(dialects, GherkinDialect::getName);
                int widestNativeName = findWidest(dialects, GherkinDialect::getNativeName);

                for (GherkinDialect dialect : dialects) {
                    printDialect(dialect, widestLanguage, widestName, widestNativeName);
                }
                return 0;
            }
        }
        if (languages.contains(language)) {
            return printKeywordsFor(dialectProvider.getDialect(language, null));
        }

        System.err.println("Unrecognised ISO language code");
        return 1;
    }

    private static int findWidest(List<GherkinDialect> dialects, Function<GherkinDialect, String> getNativeName) {
        return dialects.stream()
            .map(getNativeName)
            .mapToInt(String::length)
            .max()
            .orElse(0);
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

        List<String> codeWords = codeKeywordList.stream()
            .map(keyword -> keyword.replaceAll("[\\s',!]", ""))
            .collect(Collectors.toList());

        addKeywordRow(table, key + " (code)", codeWords);
    }

    private static void addKeywordRow(List<List<String>> table, String key, List<String> keywords) {
        table.add(asList(key, keywords.stream().map(o -> '"' + o + '"').collect(joining(", "))));
    }

    private static void printDialect(GherkinDialect dialect, int widestLanguage, int widestName, int widestNativeName) {
        String langCode = rightPad(dialect.getLanguage(), widestLanguage);
        String name = rightPad(dialect.getName(), widestName);
        String nativeName = rightPad(dialect.getNativeName(), widestNativeName);

        System.out.println(langCode + name + nativeName);
    }

    private static String rightPad(String text, int maxWidth) {
        int padding = 7;
        int width = maxWidth + padding;

        return String.format("%" + -width + "s", text);
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
                String nextArg = removeArgFor(arg, args);
                System.exit(printI18n(nextArg));
            } else if (arg.equals("--threads")) {
                int threads = Integer.parseInt(removeArgFor(arg, args));
                if (threads < 1) {
                    throw new CucumberException("--threads must be > 0");
                }
                parsedOptions.setThreads(threads);
            } else if (arg.equals("--glue") || arg.equals("-g")) {
                String gluePath = removeArgFor(arg, args);
                URI parse = GluePath.parse(gluePath);
                parsedOptions.addGlue(parse);
            } else if (arg.equals("--tags") || arg.equals("-t")) {
                parsedOptions.addTagFilter(removeArgFor(arg, args));
            } else if (arg.equals("--plugin") || arg.equals("--add-plugin") || arg.equals("-p")) {
                parsedOptions.addPluginName(removeArgFor(arg, args), arg.equals("--add-plugin"));
            } else if (arg.equals("--no-dry-run") || arg.equals("--dry-run") || arg.equals("-d")) {
                parsedOptions.setDryRun(!arg.startsWith("--no-"));
            } else if (arg.equals("--no-strict") || arg.equals("--strict") || arg.equals("-s")) {
                parsedOptions.setStrict(!arg.startsWith("--no-"));
            } else if (arg.equals("--no-monochrome") || arg.equals("--monochrome") || arg.equals("-m")) {
                parsedOptions.setMonochrome(!arg.startsWith("--no-"));
            } else if (arg.equals("--snippets")) {
                String nextArg = removeArgFor(arg, args);
                parsedOptions.setSnippetType(SnippetTypeParser.parseSnippetType(nextArg));
            } else if (arg.equals("--name") || arg.equals("-n")) {
                String nextArg = removeArgFor(arg, args);
                Pattern pattern = Pattern.compile(nextArg);
                parsedOptions.addNameFilter(pattern);
            } else if (arg.equals("--wip") || arg.equals("-w")) {
                parsedOptions.setWip(true);
            } else if (arg.equals("--order")) {
                parsedOptions.setPickleOrder(PickleOrderParser.parse(removeArgFor(arg, args)));
            } else if (arg.equals("--count")) {
                int count = Integer.parseInt(removeArgFor(arg, args));
                if (count < 1) {
                    throw new CucumberException("--count must be > 0");
                }
                parsedOptions.setCount(count);
            } else if (arg.equals("--object-factory")) {
                String objectFactoryClassName = removeArgFor(arg, args);
                parsedOptions.setObjectFactoryClass(parseObjectFactory(objectFactoryClassName));
            } else if (arg.startsWith("-")) {
                printUsage();
                throw new CucumberException("Unknown option: " + arg);
            } else if (!arg.isEmpty()) {
                if (arg.startsWith("@")) {
                    parsedOptions.setIsRerun(true);
                    Path rerunFile = Paths.get(arg.substring(1));
                    OptionsFileParser.parseFeatureWithLinesFile(rerunFile).forEach(parsedOptions::addFeature);
                } else {
                    FeatureWithLines featureWithLines = FeatureWithLines.parse(arg);
                    parsedOptions.addFeature(featureWithLines);
                }
            }
        }
        return parsedOptions;
    }

    private String removeArgFor(String arg, List<String> args) {
        if (!args.isEmpty()) {
            return args.remove(0);
        }
        printUsage();
        throw new CucumberException("Missing argument for " + arg);
    }

}
