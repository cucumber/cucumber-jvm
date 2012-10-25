package cucumber.runtime.formatter;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Utils;
import cucumber.runtime.io.UTF8FileWriter;
import gherkin.formatter.Formatter;
import gherkin.formatter.JSONFormatter;
import gherkin.formatter.JSONPrettyFormatter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class creates {@link Formatter} instances (that may also implement {@link gherkin.formatter.Reporter} from
 * a String.
 * <p/>
 * The String is of the form name[:output] where name is either a fully qualified class name or one of the built-in short names.
 * output is optional for some formatters (and mandatory for some) and must refer to a path on the file system.
 * <p/>
 * The formatter class must have a constructor that is either empty or takes a single {@link Appendable} or a {@link File}.
 */
public class FormatterFactory {
    private final Class[] CTOR_ARGS = new Class[]{null, Appendable.class, File.class};

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
        String path = null;
        if (formatterWithFile.matches()) {
            formatterName = formatterWithFile.group(1);
            path = formatterWithFile.group(2);
        } else {
            formatterName = formatterString;
        }
        Class<? extends Formatter> formatterClass = formatterClass(formatterName);
        try {
            return instantiate(formatterString, formatterClass, path);
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    private Formatter instantiate(String formatterString, Class<? extends Formatter> formatterClass, String path) throws IOException {
        for (Class ctorArgClass : CTOR_ARGS) {
            Constructor<? extends Formatter> constructor = findConstructor(formatterClass, ctorArgClass);
            if (constructor != null) {
                Object ctorArg = convertOrNull(path, ctorArgClass);
                try {
                    if (ctorArgClass == null) {
                        return constructor.newInstance();
                    } else {
                        if (ctorArg == null) {
                            throw new CucumberException(String.format("You must supply an output argument to %s. Like so: %s:output", formatterString, formatterString));
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
        throw new CucumberException(String.format("%s must have a constructor that is either empty or takes a %s or %s", formatterClass, Appendable.class.getName(), File.class.getName()));
    }

    private Object convertOrNull(String path, Class ctorArgClass) throws IOException {
        if (ctorArgClass == null) {
            return null;
        }
        if (ctorArgClass.equals(File.class)) {
            if (path != null) {
                File file = new File(path);
                Utils.ensureParentDirExists(file);
                return file;
            }
        }
        if (ctorArgClass.equals(Appendable.class)) {
            if (path != null) {
                File file = new File(path);
                Utils.ensureParentDirExists(file);
                return new UTF8FileWriter(file);
            } else {
                return defaultOutOrFailIfAlreadyUsed();
            }
        }
        return null;
    }

    private Constructor<? extends Formatter> findConstructor(Class<? extends Formatter> formatterClass, Class<?> ctorArgClass) {
        try {
            if (ctorArgClass == null) {
                return formatterClass.getConstructor();
            } else {
                return formatterClass.getConstructor(ctorArgClass);
            }
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

    private Appendable defaultOutOrFailIfAlreadyUsed() {
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
