package cucumber.runtime.transformers;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.runtime.CucumberException;

import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;

public abstract class TransformerWithFormat<T> implements SingleValueConverter {

    private final Locale locale;
    private final Class[] convertibleTypes;

    public TransformerWithFormat(Locale locale, Class[] convertibleTypes) {
        this.locale = locale;
        this.convertibleTypes = convertibleTypes;
    }

    public T fromString(String string) {
        return transform(getFormat(locale), string);
    }

    /**
     * @param locale The locale used to parse
     * @return A Format to parse the argument
     */
    public abstract Format getFormat(Locale locale);

    /**
     * Parses a value using one of the java.util.text format classes.
     *
     * @param format   The format to use
     * @param argument The object to parse
     * @return The object
     */
    @SuppressWarnings("unchecked")
    protected T transform(final Format format, final String argument) {
        ParsePosition position = new ParsePosition(0);
        Object result = format.parseObject(argument, position);
        if (position.getErrorIndex() != -1) {
            throw new CucumberException("Can't parse '" + argument + "' using format " + format);
        }
        return (T) result;
    }

    @Override
    public String toString(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canConvert(Class type) {
        for (Class convertibleType : convertibleTypes) {
            if(convertibleType.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }
}
