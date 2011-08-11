package cucumber.runtime.transformers;

import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;

public abstract class TransformableWithFormat<T> implements Transformable<T> {

	public T transform(String argument, Locale locale) {
		return transform(getFormat(locale), argument, locale);
	}

	/**
	 * 
	 * @param locale
	 *            The locale used to parse
	 * @return A Format to parse the argument
	 */
	public abstract Format getFormat(Locale locale);

	/**
	 * Parses a value using one of the java.util.text format classes.
	 * 
	 * @param format
	 *            The format to use
	 * @param argument
	 *            The object to parse
	 * @param locale
	 *            The locale used to parse
	 * @return The object
	 * @throws TransformationException
	 *             Thrown if parsing fails
	 */
	@SuppressWarnings("unchecked")
	protected T transform(final Format format, final String argument,
			Locale locale) {
		ParsePosition position = new ParsePosition(0);
		Object result = format.parseObject(argument, position);
		if (position.getErrorIndex() != -1) {
			throw new TransformationException("Can't parse '" + argument
					+ "' using format " + format);
		}
		return (T) result;
	}
}
