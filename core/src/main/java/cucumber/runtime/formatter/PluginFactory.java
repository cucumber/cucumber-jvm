package cucumber.runtime.formatter;

import cucumber.api.StepDefinitionReporter;
import cucumber.api.SummaryPrinter;
import cucumber.api.formatter.Formatter;
import cucumber.runtime.CucumberException;
import cucumber.runtime.DefaultSummaryPrinter;
import cucumber.runtime.NullSummaryPrinter;
import cucumber.runtime.io.URLOutputStream;
import cucumber.runtime.io.UTF8OutputStreamWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cucumber.runtime.Utils.toURL;
import static java.util.Arrays.asList;

/**
 * This class creates plugin instances from a String.
 * <p/>
 * The String is of the form name[:output] where name is either a fully qualified class name or one of the built-in short names.
 * output is optional for some plugin (and mandatory for some) and must refer to a path on the file system.
 * <p/>
 * The plugin class must have a constructor that is either empty or takes a single argument of one of the following types:
 * <ul>
 * <li>{@link Appendable}</li>
 * <li>{@link File}</li>
 * <li>{@link URL}</li>
 * <li>{@link URI}</li>
 * </ul>
 * Plugins must implement one of the following interfaces:
 * <ul>
 * <li>{@link cucumber.api.StepDefinitionReporter}</li>
 * </ul>
 */
public class PluginFactory {
    private final Class[] CTOR_ARGS = new Class[]{null, Appendable.class, URI.class, URL.class, File.class};

    private static final Map<String, Class> PLUGIN_CLASSES = new HashMap<String, Class>() {{
        put("null", NullFormatter.class);
        put("junit", JUnitFormatter.class);
        put("testng", TestNGFormatter.class);
        put("html", HTMLFormatter.class);
        put("pretty", PrettyFormatter.class);
        put("progress", ProgressFormatter.class);
        put("json", JSONFormatter.class);
        put("usage", UsageFormatter.class);
        put("rerun", RerunFormatter.class);
        put("default_summary", DefaultSummaryPrinter.class);
        put("null_summary", NullSummaryPrinter.class);
    }};
    private static final Pattern PLUGIN_WITH_FILE_PATTERN = Pattern.compile("([^:]+):(.*)");
    private String defaultOutFormatter = null;

    private Appendable defaultOut = new PrintStream(System.out) {
        @Override
        public void close() {
            // We have no intention to close System.out
        }
    };

    public Object create(String pluginString) {
        Matcher pluginWithFile = PLUGIN_WITH_FILE_PATTERN.matcher(pluginString);
        String pluginName;
        String path = null;
        if (pluginWithFile.matches()) {
            pluginName = pluginWithFile.group(1);
            path = pluginWithFile.group(2);
        } else {
            pluginName = pluginString;
        }
        Class pluginClass = pluginClass(pluginName);
        try {
            return instantiate(pluginString, pluginClass, path);
        } catch (IOException e) {
            throw new CucumberException(e);
        } catch (URISyntaxException e) {
            throw new CucumberException(e);
        }
    }

    private <T> T instantiate(String pluginString, Class<T> pluginClass, String pathOrUrl) throws IOException, URISyntaxException {
        for (Class ctorArgClass : CTOR_ARGS) {
            Constructor<T> constructor = findConstructor(pluginClass, ctorArgClass);
            if (constructor != null) {
                Object ctorArg = convertOrNull(pathOrUrl, ctorArgClass, pluginString);
                try {
                    if (ctorArgClass == null) {
                        return constructor.newInstance();
                    } else {
                        if (ctorArg == null) {
                            throw new CucumberException(String.format("You must supply an output argument to %s. Like so: %s:output", pluginString, pluginString));
                        }
                        return constructor.newInstance(ctorArg);
                    }
                } catch (InstantiationException e) {
                    throw new CucumberException(e);
                } catch (IllegalAccessException e) {
                    throw new CucumberException(e);
                } catch (InvocationTargetException e) {
                    throw new CucumberException(e.getTargetException());
                }
            }
        }
        throw new CucumberException(String.format("%s must have a constructor that is either empty or a single arg of one of: %s", pluginClass, asList(CTOR_ARGS)));
    }

    private Object convertOrNull(String pathOrUrl, Class ctorArgClass, String formatterString) throws IOException, URISyntaxException {
        if (ctorArgClass == null) {
            return null;
        }
        if (ctorArgClass.equals(URI.class)) {
            if (pathOrUrl != null) {
                return new URI(pathOrUrl);
            }
        }
        if (ctorArgClass.equals(URL.class)) {
            if (pathOrUrl != null) {
                return toURL(pathOrUrl);
            }
        }
        if (ctorArgClass.equals(File.class)) {
            if (pathOrUrl != null) {
                return new File(pathOrUrl);
            }
        }
        if (ctorArgClass.equals(Appendable.class)) {
            if (pathOrUrl != null) {
                return new UTF8OutputStreamWriter(new URLOutputStream(toURL(pathOrUrl)));
            } else {
                return defaultOutOrFailIfAlreadyUsed(formatterString);
            }
        }
        return null;
    }

    private <T> Constructor<T> findConstructor(Class<T> pluginClass, Class<?> ctorArgClass) {
        try {
            if (ctorArgClass == null) {
                return pluginClass.getConstructor();
            } else {
                return pluginClass.getConstructor(ctorArgClass);
            }
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static <T> Class<T> pluginClass(String pluginName) {
        Class<T> pluginClass = (Class<T>) PLUGIN_CLASSES.get(pluginName);
        if (pluginClass == null) {
            pluginClass = loadClass(pluginName);
        }
        return pluginClass;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> loadClass(String className) {
        try {
            return (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new CucumberException("Couldn't load plugin class: " + className, e);
        }
    }

    private Appendable defaultOutOrFailIfAlreadyUsed(String formatterString) {
        try {
            if (defaultOut != null) {
                defaultOutFormatter = formatterString;
                return defaultOut;
            } else {
                throw new CucumberException("Only one formatter can use STDOUT, now both " +
                        defaultOutFormatter + " and " + formatterString + " use it. " +
                        "If you use more than one formatter you must specify output path with PLUGIN:PATH_OR_URL");
            }
        } finally {
            defaultOut = null;
        }
    }

    public static boolean isFormatterName(String name) {
        Class pluginClass = getPluginClass(name);
        return Formatter.class.isAssignableFrom(pluginClass);
    }

    public static boolean isStepDefinitionResporterName(String name) {
        Class pluginClass = getPluginClass(name);
        return StepDefinitionReporter.class.isAssignableFrom(pluginClass);
    }

    public static boolean isSummaryPrinterName(String name) {
        Class pluginClass = getPluginClass(name);
        return SummaryPrinter.class.isAssignableFrom(pluginClass);
    }

    private static Class getPluginClass(String name) {
        Matcher pluginWithFile = PLUGIN_WITH_FILE_PATTERN.matcher(name);
        String pluginName;
        if (pluginWithFile.matches()) {
            pluginName = pluginWithFile.group(1);
        } else {
            pluginName = name;
        }
        return pluginClass(pluginName);
    }
}
