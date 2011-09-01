package cucumber.runtime.transformers;

import java.util.Locale;

public interface Transformer<T> {
    public T transform(Locale locale, String string) throws TransformationException;
}
