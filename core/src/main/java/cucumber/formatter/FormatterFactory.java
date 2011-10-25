package cucumber.formatter;

import cucumber.runtime.CucumberException;
import gherkin.formatter.Formatter;
import gherkin.formatter.JSONFormatter;
import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.Reporter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FormatterFactory {

    private static final String CUCUMBER_MONOCHROME_PROPERTY = "cucumber.monochrome";
    private static final String PROGRESS_FORMATTER = "progress";
    private static final String HTML_FORMATTER = "html";
    private static final String JSON_FORMATTER = "json";
    private static final String PRETTY_FORMATTER = "pretty";

    public Formatter createFormatter(String formatterName, Appendable appendable) {
        if (PROGRESS_FORMATTER.equals(formatterName)) {
            return new ProgressFormatter(appendable, isMonochrome());
        } else if (PRETTY_FORMATTER.equals(formatterName)) {
            return new PrettyFormatter(appendable, isMonochrome(), true);
        } else if (JSON_FORMATTER.equals(formatterName)) {
            return new JSONFormatter(appendable);
        } else if (HTML_FORMATTER.equals(formatterName)) {
            return new HTMLFormatter();
        }
        return createFormatterFromClassName(formatterName, appendable);
    }

    /**
     * @param formatter
     * @return reporter if the formatter also implements Reporter else returns
     *         {@link NullReporter}
     */
    public Reporter reporter(Formatter formatter) {
        if (formatter instanceof Reporter) {
            return (Reporter) formatter;
        } else {
            return new NullReporter();
        }
    }

    private Formatter createFormatterFromClassName(String formatterString, Appendable appendable) {
        Formatter formatter;
        try {
            Class<Formatter> formatterClass = getFormatterClass(formatterString);
            Constructor<Formatter> constructor = getConstructorWithAppendableIfExists(formatterClass);
            if (constructor == null) {
                formatter = getDefaultConstructor(formatterClass).newInstance();
            } else {
                formatter = constructor.newInstance(appendable);
            }
        } catch (IllegalArgumentException e) {
            throw new CucumberException("Error creating instance of: " + formatterString, e);
        } catch (InstantiationException e) {
            throw new CucumberException("Error creating instance of: " + formatterString, e);
        } catch (IllegalAccessException e) {
            throw new CucumberException("Error creating instance of: " + formatterString, e);
        } catch (InvocationTargetException e) {
            throw new CucumberException("Error creating instance of: " + formatterString, e);
        }
        return formatter;
    }

    private Constructor<Formatter> getDefaultConstructor(Class<Formatter> formatterClass) {
        try {
            return formatterClass.getDeclaredConstructor();
        } catch (SecurityException e) {
            throw new CucumberException("Error while getting default Constructor from formatter class: "
                    + formatterClass.getName(), e);
        } catch (NoSuchMethodException e) {
            throw new CucumberException("Error while getting default Constructor from formatter class: "
                    + formatterClass.getName(), e);
        }
    }

    private Constructor<Formatter> getConstructorWithAppendableIfExists(Class<Formatter> formatterClass) {
        try {
            return formatterClass.getDeclaredConstructor(Appendable.class);
        } catch (SecurityException e) {
            throw new CucumberException("Error while getting Appendable Constructor from formatter class: "
                    + formatterClass.getName(), e);
        } catch (NoSuchMethodException e) {
            // No constructor with Appendable
            return null;
        }
    }

    private Class<Formatter> getFormatterClass(String formatterString) {
        try {
            return (Class<Formatter>) Class.forName(formatterString);
        } catch (ClassNotFoundException e) {
            throw new CucumberException("Formatter class not found: " + formatterString, e);
        }
    }

    private boolean isMonochrome() {
        String value = System.getProperty(CUCUMBER_MONOCHROME_PROPERTY, "false");
        return Boolean.valueOf(value);
    }
}
