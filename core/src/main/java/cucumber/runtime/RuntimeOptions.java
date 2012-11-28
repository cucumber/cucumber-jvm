package cucumber.runtime;

import cucumber.runtime.formatter.ColorAware;
import cucumber.runtime.formatter.FormatterFactory;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.util.FixJava;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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

    public final List<String> glue = new ArrayList<String>();
    public final List<Object> filters = new ArrayList<Object>();
    public final List<Formatter> formatters = new ArrayList<Formatter>();
    public final List<String> featurePaths = new ArrayList<String>();
    private final FormatterFactory formatterFactory = new FormatterFactory();
    public File dotCucumber;
    public boolean dryRun;
    public boolean strict = false;
    public boolean monochrome = false;

    public RuntimeOptions(Properties properties, String... argv) {
        /* IMPORTANT! Make sure USAGE.txt is always uptodate if this class changes */

        parse(new ArrayList<String>(asList(argv)));
        if (properties.containsKey("cucumber.options")) {
            parse(cucumberOptionsSplit(properties.getProperty("cucumber.options")));
        }

        if (formatters.isEmpty()) {
            formatters.add(formatterFactory.create("progress"));
        }
        for (Formatter formatter : formatters) {
            if (formatter instanceof ColorAware) {
                ColorAware colorAware = (ColorAware) formatter;
                colorAware.setMonochrome(monochrome);
            }
        }
    }

    private List<String> cucumberOptionsSplit(String property) {
        List<String> matchList = new ArrayList<String>();
        Matcher shellwordsMatcher = SHELLWORDS_PATTERN.matcher(property);
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
            String arg = args.remove(0);

            if (arg.equals("--help") || arg.equals("-h")) {
                System.out.println(USAGE);
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
                dotCucumber = new File(args.remove(0));
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
}
