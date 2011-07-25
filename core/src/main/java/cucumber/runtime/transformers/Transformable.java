package cucumber.runtime.transformers;

import java.util.Locale;

public interface Transformable<T> {
	public T transform(String argument, Locale locale);
}
