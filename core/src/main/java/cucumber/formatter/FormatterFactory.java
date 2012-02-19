package cucumber.formatter;

import cucumber.formatter.usage.AverageUsageStatisticStrategy;
import cucumber.formatter.usage.MedianUsageStatisticStrategy;
import cucumber.runtime.CucumberException;
import gherkin.formatter.Formatter;
import gherkin.formatter.JSONFormatter;
import gherkin.formatter.JSONPrettyFormatter;
import gherkin.formatter.PrettyFormatter;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class FormatterFactory {

    private final ClassLoader classLoader;
    
    private static final Map<String, String> BUILTIN_FORMATTERS = new HashMap<String, String>() {{
        put("progress", ProgressFormatter.class.getName());
        put("html", HTMLFormatter.class.getName());
        put("json", JSONFormatter.class.getName());
        put("json-pretty", JSONPrettyFormatter.class.getName());
        put("pretty", PrettyFormatter.class.getName());
        put("usage", UsageFormatter.class.getName());
    }};

    public FormatterFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Formatter createFormatter(String formatterName, Object out) {
        String className = BUILTIN_FORMATTERS.containsKey(formatterName) ? BUILTIN_FORMATTERS.get(formatterName) : formatterName;
        return createFormatterFromClassName(className, out);
    }

    private Formatter createFormatterFromClassName(String className, Object out) {
        try {
            Class ctorArgClass = Appendable.class;
            if (out instanceof File) {
                File file = (File) out;
                if (file.isDirectory()) {
                    out = file;
                    ctorArgClass = File.class;
                } else {
                    out = new FileWriter(file);
                }
            }
            Class<Formatter> formatterClass = getFormatterClass(className);
            // TODO: Remove these if statements. We should fix PrettyFormatter and ProgressFormatter to only take a single Appendable arg.
            // Whether or not to use Monochrome is tricky. Maybe always enforce another 2nd argument for that
            if (PrettyFormatter.class.isAssignableFrom(formatterClass)) {
                return formatterClass.getConstructor(ctorArgClass, Boolean.TYPE, Boolean.TYPE).newInstance(out, false, true);
            } else if (ProgressFormatter.class.isAssignableFrom(formatterClass)) {
                return formatterClass.getConstructor(ctorArgClass, Boolean.TYPE).newInstance(out, false);
            } else if (UsageFormatter.class.isAssignableFrom(formatterClass)) {
                return createUsageFormatter(out, ctorArgClass, formatterClass);
            } else {
                return formatterClass.getConstructor(ctorArgClass).newInstance(out);
            }
        } catch (Exception e) {
            throw new CucumberException(String.format("Error creating instance of: %s outputting to %s", className, out), e);
        }
    }

    private Class<Formatter> getFormatterClass(String className) {
        try {
            return (Class<Formatter>) classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new CucumberException("Formatter class not found: " + className, e);
        }
    }

    private UsageFormatter createUsageFormatter(Object out, Class ctorArgClass, Class<Formatter> formatterClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        UsageFormatter formatter = (UsageFormatter)formatterClass.getConstructor(ctorArgClass).newInstance(out);
        formatter.addUsageStatisticStrategy("average", new AverageUsageStatisticStrategy());
        formatter.addUsageStatisticStrategy("median", new MedianUsageStatisticStrategy());
        return formatter;
    }
}
