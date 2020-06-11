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
import io.cucumber.tagexpressions.TagExpressionException;
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

import static io.cucumber.core.options.ObjectFactoryParser.parseObjectFactory;
import static io.cucumber.core.options.OptionsFileParser.parseFeatureWithLinesFile;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public final class CommandlineOptionsParser {

    private static final Logger log = LoggerFactory.getLogger(CommandlineOptionsParser.class);

    private static final String VERSION = ResourceBundle.getBundle("io.cucumber.core.version")
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

            if (arg.equals("--help") || arg.equals("-h")) {
                printUsage();
                exitCode = 0;
                return parsedOptions;
            } else if (arg.equals("--version") || arg.equals("-v")) {
                out.println(VERSION);
                exitCode = 0;
                return parsedOptions;
            } else if (arg.equals("--i18n")) {
                String nextArg = removeArgFor(arg, args);
                exitCode = printI18n(nextArg);
                return parsedOptions;
            } else if (arg.equals("--threads")) {
                int threads = Integer.parseInt(removeArgFor(arg, args));
                if (threads < 1) {
                    out.println("--threads must be > 0");
                    exitCode = 1;
                    return parsedOptions;
                }
                parsedOptions.setThreads(threads);
            } else if (arg.equals("--glue") || arg.equals("-g")) {
                String gluePath = removeArgFor(arg, args);
                URI parse = GluePath.parse(gluePath);
                parsedOptions.addGlue(parse);
            } else if (arg.equals("--tags") || arg.equals("-t")) {
		parsedOptions.addTagFilter(TagExpressionParser.parse(removeArgFor(arg, args)));
            } else if (arg.equals("--plugin") || arg.equals("-p")) {
                parsedOptions.addPluginName(removeArgFor(arg, args));
            } else if (arg.equals("--no-dry-run") || arg.equals("--dry-run") || arg.equals("-d")) {
                parsedOptions.setDryRun(!arg.startsWith("--no-"));
            } else if (arg.equals("--no-strict")) {
                out.println("--no-strict is no longer effective");
                exitCode = 1;
                return parsedOptions;
            } else if (arg.equals("--strict") || arg.equals("-s")) {
                log.warn(() -> "--strict is enabled by default. This option will be removed in a future release.");
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
                    out.println("--count must be > 0");
                    exitCode = 1;
                    return parsedOptions;
                }
                parsedOptions.setCount(count);
            } else if (arg.equals("--object-factory")) {
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
