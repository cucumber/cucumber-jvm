package io.cucumber.core.options;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.datatable.DataTable;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.gherkin.IGherkinDialectProvider;
import io.cucumber.tagexpressions.TagExpressionParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.cucumber.core.cli.CommandlineOptions.COUNT;
import static io.cucumber.core.cli.CommandlineOptions.DRY_RUN;
import static io.cucumber.core.cli.CommandlineOptions.DRY_RUN_SHORT;
import static io.cucumber.core.cli.CommandlineOptions.GLUE;
import static io.cucumber.core.cli.CommandlineOptions.GLUE_SHORT;
import static io.cucumber.core.cli.CommandlineOptions.HELP;
import static io.cucumber.core.cli.CommandlineOptions.HELP_SHORT;
import static io.cucumber.core.cli.CommandlineOptions.I18N;
import static io.cucumber.core.cli.CommandlineOptions.MONOCHROME;
import static io.cucumber.core.cli.CommandlineOptions.MONOCHROME_SHORT;
import static io.cucumber.core.cli.CommandlineOptions.NAME;
import static io.cucumber.core.cli.CommandlineOptions.NAME_SHORT;
import static io.cucumber.core.cli.CommandlineOptions.NO_DRY_RUN;
import static io.cucumber.core.cli.CommandlineOptions.NO_MONOCHROME;
import static io.cucumber.core.cli.CommandlineOptions.NO_STRICT;
import static io.cucumber.core.cli.CommandlineOptions.OBJECT_FACTORY;
import static io.cucumber.core.cli.CommandlineOptions.ORDER;
import static io.cucumber.core.cli.CommandlineOptions.PLUGIN;
import static io.cucumber.core.cli.CommandlineOptions.PLUGIN_SHORT;
import static io.cucumber.core.cli.CommandlineOptions.PUBLISH;
import static io.cucumber.core.cli.CommandlineOptions.SNIPPETS;
import static io.cucumber.core.cli.CommandlineOptions.STRICT;
import static io.cucumber.core.cli.CommandlineOptions.STRICT_SHORT;
import static io.cucumber.core.cli.CommandlineOptions.TAGS;
import static io.cucumber.core.cli.CommandlineOptions.TAGS_SHORT;
import static io.cucumber.core.cli.CommandlineOptions.THREADS;
import static io.cucumber.core.cli.CommandlineOptions.VERSION;
import static io.cucumber.core.cli.CommandlineOptions.VERSION_SHORT;
import static io.cucumber.core.cli.CommandlineOptions.WIP;
import static io.cucumber.core.cli.CommandlineOptions.WIP_SHORT;
import static io.cucumber.core.options.ObjectFactoryParser.parseObjectFactory;
import static io.cucumber.core.options.OptionsFileParser.parseFeatureWithLinesFile;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public final class CommandlineOptionsParser {

    private static final Logger log = LoggerFactory.getLogger(CommandlineOptionsParser.class);

    private static final String CORE_VERSION = ResourceBundle.getBundle("io.cucumber.core.version")
            .getString("cucumber-jvm.version");
    // IMPORTANT! Make sure USAGE.txt is always uptodate if this class changes.
    private static final String USAGE_RESOURCE = "/io/cucumber/core/options/USAGE.txt";

    private final PrintWriter out;
    private Byte exitCode = null;

    public CommandlineOptionsParser(OutputStream outputStream) {
        out = new PrintWriter(outputStream, true);
    }

    public Optional<Byte> exitStatus() {
        return Optional.ofNullable(exitCode);
    }

    public RuntimeOptionsBuilder parse(String... args) {
        return parse(Arrays.asList(args));
    }

    private RuntimeOptionsBuilder parse(List<String> args) {
        args = new ArrayList<>(args);
        RuntimeOptionsBuilder parsedOptions = new RuntimeOptionsBuilder();

        while (!args.isEmpty()) {
            String arg = args.remove(0).trim();

            if (arg.equals(HELP) || arg.equals(HELP_SHORT)) {
                printUsage();
                exitCode = 0;
                return parsedOptions;
            } else if (arg.equals(VERSION) || arg.equals(VERSION_SHORT)) {
                out.println(CORE_VERSION);
                exitCode = 0;
                return parsedOptions;
            } else if (arg.equals(I18N)) {
                String nextArg = removeArgFor(arg, args);
                exitCode = printI18n(nextArg);
                return parsedOptions;
            } else if (arg.equals(THREADS)) {
                int threads = Integer.parseInt(removeArgFor(arg, args));
                if (threads < 1) {
                    out.println("--threads must be > 0");
                    exitCode = 1;
                    return parsedOptions;
                }
                parsedOptions.setThreads(threads);
            } else if (arg.equals(GLUE) || arg.equals(GLUE_SHORT)) {
                String gluePath = removeArgFor(arg, args);
                URI parse = GluePath.parse(gluePath);
                parsedOptions.addGlue(parse);
            } else if (arg.equals(TAGS) || arg.equals(TAGS_SHORT)) {
                parsedOptions.addTagFilter(TagExpressionParser.parse(removeArgFor(arg, args)));
            } else if (arg.equals(PUBLISH)) {
                parsedOptions.setPublish(true);
            } else if (arg.equals(PLUGIN) || arg.equals(PLUGIN_SHORT)) {
                parsedOptions.addPluginName(removeArgFor(arg, args));
            } else if (arg.equals(DRY_RUN) || arg.equals(DRY_RUN_SHORT)) {
                parsedOptions.setDryRun(true);
            } else if (arg.equals(NO_DRY_RUN)) {
                parsedOptions.setDryRun(false);
            } else if (arg.equals(NO_STRICT)) {
                out.println("--no-strict is no longer effective");
                exitCode = 1;
                return parsedOptions;
            } else if (arg.equals(STRICT) || arg.equals(STRICT_SHORT)) {
                log.warn(() -> "--strict is enabled by default. This option will be removed in a future release.");
            } else if (arg.equals(MONOCHROME) || arg.equals(MONOCHROME_SHORT)) {
                parsedOptions.setMonochrome(true);
            } else if (arg.equals(NO_MONOCHROME)) {
                parsedOptions.setMonochrome(false);
            } else if (arg.equals(SNIPPETS)) {
                String nextArg = removeArgFor(arg, args);
                parsedOptions.setSnippetType(SnippetTypeParser.parseSnippetType(nextArg));
            } else if (arg.equals(NAME) || arg.equals(NAME_SHORT)) {
                String nextArg = removeArgFor(arg, args);
                Pattern pattern = Pattern.compile(nextArg);
                parsedOptions.addNameFilter(pattern);
            } else if (arg.equals(WIP) || arg.equals(WIP_SHORT)) {
                parsedOptions.setWip(true);
            } else if (arg.equals(ORDER)) {
                parsedOptions.setPickleOrder(PickleOrderParser.parse(removeArgFor(arg, args)));
            } else if (arg.equals(COUNT)) {
                int count = Integer.parseInt(removeArgFor(arg, args));
                if (count < 1) {
                    out.println("--count must be > 0");
                    exitCode = 1;
                    return parsedOptions;
                }
                parsedOptions.setCount(count);
            } else if (arg.equals(OBJECT_FACTORY)) {
                String objectFactoryClassName = removeArgFor(arg, args);
                parsedOptions.setObjectFactoryClass(parseObjectFactory(objectFactoryClassName));
            } else if (arg.startsWith("-")) {
                out.println("Unknown option: " + arg);
                printUsage();
                exitCode = 1;
                return parsedOptions;
            } else if (!arg.isEmpty()) {
                if (arg.startsWith("@")) {
                    Path rerunFile = Paths.get(arg.substring(1));
                    parsedOptions.addRerun(parseFeatureWithLinesFile(rerunFile));
                } else {
                    FeatureWithLines featureWithLines = FeatureWithLines.parse(arg);
                    parsedOptions.addFeature(featureWithLines);
                }
            }
        }

        return parsedOptions;
    }

    private void printUsage() {
        out.println(loadUsageText());
    }

    private String removeArgFor(String arg, List<String> args) {
        if (!args.isEmpty()) {
            return args.remove(0);
        }
        printUsage();
        throw new CucumberException("Missing argument for " + arg);
    }

    private byte printI18n(String language) {
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
                return 0x0;
            }
        }
        if (languages.contains(language)) {
            return printKeywordsFor(dialectProvider.getDialect(language, null));
        }

        out.println("Unrecognised ISO language code");
        return 0x1;
    }

    private String loadUsageText() {
        try (
                InputStream usageResourceStream = CommandlineOptionsParser.class.getResourceAsStream(USAGE_RESOURCE);
                BufferedReader br = new BufferedReader(new InputStreamReader(usageResourceStream, UTF_8))) {
            return br.lines().collect(joining(System.lineSeparator()));
        } catch (Exception e) {
            return "Could not load usage text: " + e.toString();
        }
    }

    private int findWidest(List<GherkinDialect> dialects, Function<GherkinDialect, String> getNativeName) {
        return dialects.stream()
                .map(getNativeName)
                .mapToInt(String::length)
                .max()
                .orElse(0);
    }

    private void printDialect(GherkinDialect dialect, int widestLanguage, int widestName, int widestNativeName) {
        String langCode = rightPad(dialect.getLanguage(), widestLanguage);
        String name = rightPad(dialect.getName(), widestName);
        String nativeName = rightPad(dialect.getNativeName(), widestNativeName);

        out.println(langCode + name + nativeName);
    }

    private byte printKeywordsFor(GherkinDialect dialect) {
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
        out.println(builder.toString());
        return 0x0;
    }

    private String rightPad(String text, int maxWidth) {
        int padding = 7;
        int width = maxWidth + padding;

        return String.format("%" + -width + "s", text);
    }

    private void addKeywordRow(List<List<String>> table, String key, List<String> keywords) {
        table.add(asList(key, keywords.stream().map(o -> '"' + o + '"').collect(joining(", "))));
    }

    private void addCodeKeywordRow(List<List<String>> table, String key, List<String> keywords) {
        List<String> codeKeywordList = new ArrayList<>(keywords);
        codeKeywordList.remove("* ");

        List<String> codeWords = codeKeywordList.stream()
                .map(keyword -> keyword.replaceAll("[\\s',!]", ""))
                .collect(Collectors.toList());

        addKeywordRow(table, key + " (code)", codeWords);
    }

}
