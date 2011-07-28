package cucumber.runtime.transformers;

import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

public abstract class TransformableWithNumberFormat<T extends Number> extends
		TransformableWithFormat<T> {

	@Override
	public T transform(String argument, Locale locale) {
		return doTransform(super.transform(argument, locale));
	}
	
	@Override
	public Format getFormat(Locale locale) {
		return NumberFormat.getNumberInstance(locale);
	}

	protected abstract T doTransform(Number argument);

}
