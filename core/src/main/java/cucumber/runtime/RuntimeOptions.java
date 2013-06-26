package cucumber.runtime;

import cucumber.runtime.formatter.ColorAware;
import cucumber.runtime.formatter.FormatterFactory;
import cucumber.runtime.formatter.StrictAware;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.util.FixJava;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cucumber.runtime.model.CucumberFeature.load;
import static java.util.Arrays.asList;

public class RuntimeOptions {
    public static final String VERSION = ResourceBundle.getBundle("cucumber.version").getString("cucumber-jvm.version");
    public static final String USAGE = FixJava.readResource("/cucumber/runtime/USAGE.txt");
    private static final Pattern SHELLWORDS_PATTERN = Pattern.compile("[^\\s']+|'([^']*)'");

    private final List<String> glue = new ArrayList<String>();
    private final List<Object> filters = new ArrayList<Object>();
    private final List<Formatter> formatters = new ArrayList<Formatter>();
    private final List<String> featurePaths = new ArrayList<String>();
    private final FormatterFactory formatterFactory;
    private URL dotCucumber;
    private boolean dryRun;
    private boolean strict = false;
    private boolean monochrome = false;

    public RuntimeOptions(Properties properties, String... argv) {
        /* IMPORTANT! Make sure USAGE.txt is always uptodate if this class changes */
        this(properties, new FormatterFactory(), argv);
    }

    RuntimeOptions(Properties properties, FormatterFactory formatterFactory, String... argv) {
        this.formatterFactory = formatterFactory;

        parse(new ArrayList<String>(asList(argv)));
        if (properties.containsKey("cucumber.options")) {
            parse(shellWords(properties.getProperty("cucumber.options")));
        }

        if (formatters.isEmpty()) {
            formatters.add(formatterFactory.create("progress"));
        }
        setFormatterOptions();
    }

    private List<String> shellWords(String cmdline) {
        List<String> matchList = new ArrayList<String>();
        Matcher shellwordsMatcher = SHELLWORDS_PATTERN.matcher(cmdline);
        while (shellwordsMatcher.find()) {
            if (shellwordsMatcher.group(1) != null) {
                matchList.add(shellwordsMatcher.group(1));
            } else {
                matchList.add(shellwordsMatcher.group());
            }
        }
        return matchList;
    }

    private void parse(List<String> args) {
        List<Object> parsedFilters = new ArrayList<Object>();
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
                glue.add(gluePath);
            } else if (arg.equals("--tags") || arg.equals("-t")) {
                parsedFilters.add(args.remove(0));
            } else if (arg.equals("--format") || arg.equals("-f")) {
                formatters.add(formatterFactory.create(args.remove(0)));
            } else if (arg.equals("--dotcucumber")) {
                String urlOrPath = args.remove(0);
                dotCucumber = Utils.toURL(urlOrPath);
            } else if (arg.equals("--no-dry-run") || arg.equals("--dry-run") || arg.equals("-d")) {
                dryRun = !arg.startsWith("--no-");
            } else if (arg.equals("--no-strict") || arg.equals("--strict") || arg.equals("-s")) {
                strict = !arg.startsWith("--no-");
            } else if (arg.equals("--no-monochrome") || arg.equals("--monochrome") || arg.equals("-m")) {
                monochrome = !arg.startsWith("--no-");
            } else if (arg.equals("--name") || arg.equals("-n")) {
                String nextArg = args.remove(0);
                Pattern patternFilter = Pattern.compile(nextArg);
                parsedFilters.add(patternFilter);
            } else if (arg.startsWith("-")) {
                printUsage();
                throw new CucumberException("Unknown option: " + arg);
            } else {
                PathWithLines pathWithLines = new PathWithLines(arg);
                featurePaths.add(pathWithLines.path);
                parsedFilters.addAll(pathWithLines.lines);
            }
        }
        if (!parsedFilters.isEmpty()) {
            filters.clear();
            filters.addAll(parsedFilters);
        }
    }

    private void printUsage() {
        System.out.println(USAGE);
    }

    public List<CucumberFeature> cucumberFeatures(ResourceLoader resourceLoader) {
        return load(resourceLoader, featurePaths, filters);
    }

    public Formatter formatter(ClassLoader classLoader) {
        return (Formatter) Proxy.newProxyInstance(classLoader, new Class<?>[]{Formatter.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object target, Method method, Object[] args) throws Throwable {
                for (Formatter formatter : formatters) {
                    Utils.invoke(formatter, method, 0, args);
                }
                return null;
            }
        });
    }

    public Reporter reporter(ClassLoader classLoader) {
        return (Reporter) Proxy.newProxyInstance(classLoader, new Class<?>[]{Reporter.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object target, Method method, Object[] args) throws Throwable {
                for (Formatter formatter : formatters) {
                    if (formatter instanceof Reporter) {
                        Utils.invoke(formatter, method, 0, args);
                    }
                }
                return null;
            }
        });
    }

    private void setFormatterOptions() {
        for (Formatter formatter : formatters) {
            setMonochromeOnColorAwareFormatters(formatter);
            setStrictOnStrictAwareFormatters(formatter);
        }
    }

    private void setMonochromeOnColorAwareFormatters(Formatter formatter) {
        if (formatter instanceof ColorAware) {
            ColorAware colorAware = (ColorAware) formatter;
            colorAware.setMonochrome(monochrome);
        }
    }

    private void setStrictOnStrictAwareFormatters(Formatter formatter) {
        if (formatter instanceof StrictAware) {
            StrictAware strictAware = (StrictAware) formatter;
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

    public URL getDotCucumber() {
        return dotCucumber;
    }

    public List<Formatter> getFormatters() {
        return formatters;
    }

    public List<Object> getFilters() {
        return filters;
    }
}
