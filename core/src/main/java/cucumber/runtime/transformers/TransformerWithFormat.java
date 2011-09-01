package cucumber.runtime.transformers;

import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;

public abstract class TransformerWithFormat<T> implements Transformer<T> {

    public T transform(Locale locale, String string) throws TransformationException {
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
     * @throws TransformationException Thrown if parsing fails
     */
    @SuppressWarnings("unchecked")
    protected T transform(final Format format, final String argument) throws TransformationException {
        ParsePosition position = new ParsePosition(0);
        Object result = format.parseObject(argument, position);
        if (position.getErrorIndex() != -1) {
            throw new TransformationException("Can't parse '" + argument + "' using format " + format);
        }
        return (T) result;
    }
}
