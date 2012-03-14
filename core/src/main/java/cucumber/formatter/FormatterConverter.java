package cucumber.formatter;

import com.beust.jcommander.IStringConverter;
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

public class FormatterConverter implements IStringConverter<Formatter> {
    private Class[] CTOR_ARGS = new Class[]{Appendable.class, File.class};
    
    private static final Map<String,Class<? extends Formatter>> FORMATTER_CLASSES = new HashMap<String, Class<? extends Formatter>>() {{
        put("junit", JUnitFormatter.class);
        put("html", HTMLFormatter.class);
        put("pretty", CucumberPrettyFormatter.class);
        put("progress", ProgressFormatter.class);
        put("json", JSONFormatter.class);
        put("json-pretty", JSONPrettyFormatter.class);
    }};
    private static final Pattern FORMATTER_WITH_FILE_PATTERN = Pattern.compile("([^:]+):(.*)");
    private Appendable defaultOut = System.out;

    @Override
    public Formatter convert(String formatterString) {
        Matcher formatterWithFile = FORMATTER_WITH_FILE_PATTERN.matcher(formatterString);
        String formatterName;
        Object ctorArg;
        if(formatterWithFile.matches()) {
            formatterName = formatterWithFile.group(1);
            ctorArg = new File(formatterWithFile.group(2));
        } else {
            formatterName = formatterString;
            ctorArg = defaultOutIfAvailable();
        }
        Class<? extends Formatter> formatterClass = formatterClass(formatterName);
        try {
            return instantiate(formatterClass, ctorArg);
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    private Formatter instantiate(Class<? extends Formatter> formatterClass, Object ctorArg) throws IOException {
        Constructor<? extends Formatter> constructor;

        for (Class ctorArgClass : CTOR_ARGS) {
            constructor=findConstructor(formatterClass, ctorArgClass);
            if(constructor != null) {
                ctorArg = convert(ctorArg, ctorArgClass);
                if(ctorArg != null) {
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
        throw new CucumberException(String.format("%s must have a single-argument constructor that takes one of the following: %s", formatterClass, asList(CTOR_ARGS)));
    }

    private Object convert(Object ctorArg, Class ctorArgClass) throws IOException {
        if(ctorArgClass.isAssignableFrom(ctorArg.getClass())) {
            return ctorArg;
        }
        if(ctorArgClass.equals(File.class) && ctorArg instanceof File) {
            return ctorArg;
        }
        if(ctorArgClass.equals(Appendable.class) && ctorArg instanceof File) {
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
        if(formatterClass == null) {
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
            if(defaultOut != null) {
                return defaultOut;
            } else {
                throw new CucumberException("Only one formatter can use STDOUT. If you use more than one formatter you must specify output path with FORMAT:PATH");
            }
        } finally {
            defaultOut = null;
        }
    }
}
