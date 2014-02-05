package cucumber.runtime;

import cucumber.api.SnippetType;
import cucumber.runtime.formatter.ColorAware;
import cucumber.runtime.formatter.FormatterFactory;
import cucumber.runtime.formatter.StrictAware;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.TagExpression;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Tag;
import gherkin.util.FixJava;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static cucumber.runtime.model.CucumberFeature.load;

// IMPORTANT! Make sure USAGE.txt is always uptodate if this class changes.
public class RuntimeOptions {
    public static final String VERSION = ResourceBundle.getBundle("cucumber.version").getString("cucumber-jvm.version");
    public static final String USAGE = FixJava.readResource("/cucumber/runtime/USAGE.txt");

    private final List<String> glue = new ArrayList<String>();
    private final List<Object> filters = new ArrayList<Object>();
    private final List<Object> lineFilters = new ArrayList<Object>();
    private final List<Formatter> formatters = new ArrayList<Formatter>();
    private final List<String> featurePaths = new ArrayList<String>();
    private final List<String> skipTags = new ArrayList<String>();
    private final FormatterFactory formatterFactory;
    private URL dotCucumber;
    private boolean dryRun;
    private boolean strict = false;
    private boolean monochrome = false;
    private SnippetType snippetType = SnippetType.UNDERSCORE;
    private TagExpression skipTagExpression;

    /**
     * Create a new instance from a string of options, for example:
     *
     * <pre<{@code "--name 'the fox' --format pretty --strict"}</pre>
     *
     * @param argv the arguments
     */
    public RuntimeOptions(String argv) {
        this(new FormatterFactory(), Shellwords.parse(argv));
    }

    /**
     * Create a new instance from a list of options, for example:
     *
     * <pre<{@code Arrays.asList("--name", "the fox", "--format", "pretty", "--strict");}</pre>
     *
     * @param argv the arguments
     */
    public RuntimeOptions(List<String> argv) {
        this(new FormatterFactory(), argv);
    }

    public RuntimeOptions(Env env, List<String> argv) {
        this(env, new FormatterFactory(), argv);
    }

    public RuntimeOptions(FormatterFactory formatterFactory, List<String> argv) {
        this(new Env("cucumber-jvm"), formatterFactory, argv);
    }

    public RuntimeOptions(Env env, FormatterFactory formatterFactory, List<String> argv) {
        this.formatterFactory = formatterFactory;

        argv = new ArrayList<String>(argv); // in case the one passed in is unmodifiable.
        parse(argv);

        String cucumberOptionsFromEnv = env.get("cucumber.options");
        if (cucumberOptionsFromEnv != null) {
            parse(Shellwords.parse(cucumberOptionsFromEnv));
        }
        filters.addAll(lineFilters);

        if (formatters.isEmpty()) {
            formatters.add(formatterFactory.create("progress"));
        }
        setFormatterOptions();
    }

    private void parse(List<String> args) {
        List<Object> parsedFilters = new ArrayList<Object>();
        List<Object> parsedLineFilters = new ArrayList<Object>();
        List<String> parsedFeaturePaths = new ArrayList<String>();
        List<String> parsedGlue = new ArrayList<String>();

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
                parsedGlue.add(gluePath);
            } else if (arg.equals("--tags") || arg.equals("-t")) {
                parsedFilters.add(args.remove(0));
            } else if (arg.equals("--skip-tags") || arg.equals("-k")) {
                skipTags.add(args.remove(0));
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
            } else if (arg.equals("--snippets")) {
                String nextArg = args.remove(0);
                snippetType = SnippetType.fromString(nextArg);
            } else if (arg.equals("--name") || arg.equals("-n")) {
                String nextArg = args.remove(0);
                Pattern patternFilter = Pattern.compile(nextArg);
                parsedFilters.add(patternFilter);
            } else if (arg.startsWith("-")) {
                printUsage();
                throw new CucumberException("Unknown option: " + arg);
            } else {
                PathWithLines pathWithLines = new PathWithLines(arg);
                parsedFeaturePaths.add(pathWithLines.path);
                parsedLineFilters.addAll(pathWithLines.lines);
            }
        }
        if (!parsedFilters.isEmpty()) {
            filters.clear();
            filters.addAll(parsedFilters);
        }
        if (!parsedFeaturePaths.isEmpty()) {
            featurePaths.clear();
            lineFilters.clear();
            featurePaths.addAll(parsedFeaturePaths);
            lineFilters.addAll(parsedLineFilters);
        }
        if (!parsedGlue.isEmpty()) {
            glue.clear();
            glue.addAll(parsedGlue);
        }

        if (skipTags.isEmpty()) {
            skipTagExpression = new TagExpression(Collections.EMPTY_LIST) {
                @Override
                public boolean evaluate(Collection<Tag> tags) {
                    return false;
                }
            };
        } else {
            //Create a tag expression where normal tags are considered and 'OR' match
            //And negated tags are 'AND' matched
            List<String> tags = new ArrayList<String>();
            StringBuilder sb = new StringBuilder();
            for (Iterator<String> iter = skipTags.iterator(); iter.hasNext();) {
                final String tag = iter.next();
                if (tag.startsWith("~")) {
                    tags.add(tag);
                } else {
                    if (sb.length() != 0) {
                        sb.append(',');
                    }
                    sb.append(tag);
                }
            }
            if (sb.length() > 0) {
                tags.add(sb.toString());
            }
            skipTagExpression = new TagExpression(tags);
        }
    }

    private void printUsage() {
        System.out.println(USAGE);
    }

    public List<CucumberFeature> cucumberFeatures(ResourceLoader resourceLoader) {
        return load(resourceLoader, featurePaths, filters, System.out);
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

    public boolean isMonochrome() {
        return monochrome;
    }

    public SnippetType getSnippetType() {
        return snippetType;
    }

    public TagExpression getSkipTagsExpression() {
        return skipTagExpression;
    }

    public List<String> getSkipTags() {
        return skipTags;
    }
}
