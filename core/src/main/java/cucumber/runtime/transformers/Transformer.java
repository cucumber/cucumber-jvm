package cucumber.runtime.transformers;

import java.util.Locale;

public interface Transformer<T> {
    public T transform(String argument, Locale locale);
}
