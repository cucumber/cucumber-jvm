package cucumber.formatter;

import cucumber.runtime.CucumberException;
import gherkin.formatter.Formatter;
import gherkin.formatter.JSONFormatter;
import gherkin.formatter.JSONPrettyFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

/**
 * This class creates {@link Formatter} instances (that may also implement {@link gherkin.formatter.Reporter} from
 * a String.
 *
 * The String is of the form name[:output] where name is either a fully qualified class name or one of the built-in short names.
 * output is optional for some formatters (and mandatory for some) and must refer to a path on the file system.
 *
 * The formatter class must have a single argument constructor that takes either an {@link Appendable} or a {@link File}.
 */
public class FormatterFactory {
    private final Class[] CTOR_ARGS = new Class[]{Appendable.class, File.class};

    private static final Map<String, Class<? extends Formatter>> FORMATTER_CLASSES = new HashMap<String, Class<? extends Formatter>>() {{
        put("null", NullFormatter.class);
        put("junit", JUnitFormatter.class);
        put("html", HTMLFormatter.class);
        put("pretty", CucumberPrettyFormatter.class);
        put("progress", ProgressFormatter.class);
        put("json", JSONFormatter.class);
        put("json-pretty", JSONPrettyFormatter.class);
        put("usage", UsageFormatter.class);
    }};
    private static final Pattern FORMATTER_WITH_FILE_PATTERN = Pattern.compile("([^:]+):(.*)");
    private Appendable defaultOut = System.out;

    public Formatter create(String formatterString) {
        Matcher formatterWithFile = FORMATTER_WITH_FILE_PATTERN.matcher(formatterString);
        String formatterName;
        Object ctorArg;
        if (formatterWithFile.matches()) {
            formatterName = formatterWithFile.group(1);
            ctorArg = new File(formatterWithFile.group(2));
        } else {
            formatterName = formatterString;
            ctorArg = defaultOutIfAvailable();
        }
        Class<? extends Formatter> formatterClass = formatterClass(formatterName);
        try {
            return instantiate(formatterString, formatterClass, ctorArg);
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    private Formatter instantiate(String formatterString, Class<? extends Formatter> formatterClass, Object ctorArg) throws IOException {
        for (Class ctorArgClass : CTOR_ARGS) {
            Constructor<? extends Formatter> constructor = findConstructor(formatterClass, ctorArgClass);
            if (constructor != null) {
                ctorArg = convertOrNull(ctorArg, ctorArgClass);
                if (ctorArg != null) {
                    try {
                        return constructor.newInstance(ctorArg);
                    } catch (InstantiationException e) {
                        throw new CucumberException(e);
                    } catch (IllegalAccessException e) {
                        throw new CucumberException(e);
                    } catch (InvocationTargetException e) {
                        throw new CucumberException(e.getTargetException());
                    }
                }
            }
        }
        if (ctorArg == null) {
            throw new CucumberException(String.format("You must supply an output argument to %s. Like so: %s:output", formatterString, formatterString));
        }
        throw new CucumberException(String.format("%s must have a single-argument constructor that takes one of the following: %s", formatterClass, asList(CTOR_ARGS)));
    }

    private Object convertOrNull(Object ctorArg, Class ctorArgClass) throws IOException {
        if (ctorArgClass.isAssignableFrom(ctorArg.getClass())) {
            return ctorArg;
        }
        if (ctorArgClass.equals(File.class) && ctorArg instanceof File) {
            return ctorArg;
        }
        if (ctorArgClass.equals(Appendable.class) && ctorArg instanceof File) {
            return new FileWriter((File) ctorArg);
        }
        return null;
    }

    private Constructor<? extends Formatter> findConstructor(Class<? extends Formatter> formatterClass, Class<?> ctorArgClass) {
        try {
            return formatterClass.getConstructor(ctorArgClass);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Class<? extends Formatter> formatterClass(String formatterName) {
        Class<? extends Formatter> formatterClass = FORMATTER_CLASSES.get(formatterName);
        if (formatterClass == null) {
            formatterClass = loadClass(formatterName);
        }
        return formatterClass;
    }

    private Class<? extends Formatter> loadClass(String className) {
        try {
            return (Class<? extends Formatter>) Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new CucumberException("Couldn't load formatter class: " + className, e);
        }
    }

    private Appendable defaultOutIfAvailable() {
        try {
            if (defaultOut != null) {
                return defaultOut;
            } else {
                throw new CucumberException("Only one formatter can use STDOUT. If you use more than one formatter you must specify output path with FORMAT:PATH");
            }
        } finally {
            defaultOut = null;
        }
    }
}
