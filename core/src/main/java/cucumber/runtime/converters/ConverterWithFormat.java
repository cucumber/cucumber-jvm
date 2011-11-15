package cucumber.runtime.converters;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.runtime.CucumberException;

import java.text.Format;
import java.text.ParsePosition;
import java.util.List;

public abstract class ConverterWithFormat<T> implements SingleValueConverter {
    private final Class[] convertibleTypes;

    public ConverterWithFormat(Class[] convertibleTypes) {
        this.convertibleTypes = convertibleTypes;
    }

    public T fromString(String string) {
        for (Format format : getFormats()) {
            try {
                return transform(format, string);
            } catch (Exception ignore) {
                // no worries, let's try the next format.
            }
        }
        throw new ConversionException("Cannot parse " + string);

    }

    /**
     * @return A Format to parse the argument
     */
    protected abstract List<? extends Format> getFormats();

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
            if (convertibleType.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }
}
