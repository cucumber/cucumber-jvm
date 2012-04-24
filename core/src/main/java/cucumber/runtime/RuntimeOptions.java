package cucumber.runtime;

import cucumber.formatter.ColorAware;
import cucumber.formatter.FormatterConverter;
import cucumber.formatter.ProgressFormatter;
import cucumber.io.ResourceLoader;
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
import java.util.ResourceBundle;

import static cucumber.runtime.model.CucumberFeature.load;
import static java.util.Arrays.asList;

public class RuntimeOptions {
    public static final String VERSION = ResourceBundle.getBundle("cucumber.version").getString("cucumber-jvm.version");
    public static final String USAGE = FixJava.readResource("/cucumber/runtime/USAGE.txt");

    public List<String> glue = new ArrayList<String>();
    public File dotCucumber;
    public boolean dryRun;
    public boolean strict = false;
    public List<Object> filters = new ArrayList<Object>();
    public List<Formatter> formatters = new ArrayList<Formatter>();
    public List<String> featurePaths = new ArrayList<String>();
    private boolean monochrome = false;

    public RuntimeOptions(String... argv) {
        parse(new ArrayList<String>(asList(argv)));

        if (formatters.isEmpty()) {
            formatters.add(new ProgressFormatter(System.out));
        }
        for (Formatter formatter : formatters) {
            if (formatter instanceof ColorAware) {
                ColorAware colorAware = (ColorAware) formatter;
                colorAware.setMonochrome(monochrome);
            }
        }
    }

    private void parse(ArrayList<String> args) {
        FormatterConverter formatterConverter = new FormatterConverter();

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
                filters.add(args.remove(0));
            } else if (arg.equals("--format") || arg.equals("-f")) {
                formatters.add(formatterConverter.convert(args.remove(0)));
            } else if (arg.equals("--dotcucumber")) {
                dotCucumber = new File(args.remove(0));
            } else if (arg.equals("--dry-run") || arg.equals("-d")) {
                dryRun = true;
            } else if (arg.equals("--strict") || arg.equals("-s")) {
                strict = true;
            } else if (arg.equals("--monochrome") || arg.equals("-m")) {
                monochrome = true;
            } else {
                PathWithLines pathWithLines = new PathWithLines(arg);
                featurePaths.add(pathWithLines.path);
                filters.addAll(pathWithLines.lines);
            }
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
                    Utils.invoke(formatter, method, args);
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
                        Utils.invoke(formatter, method, args);
                    }
                }
                return null;
            }
        });
    }
}
