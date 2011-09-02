package cucumber.runtime.transformers;

import java.util.Locale;

public class ShortTransformer extends TransformerWithNumberFormat<Short> {

    public ShortTransformer(Locale locale) {
        super(locale, new Class[]{Short.class, Short.TYPE});
    }

    @Override
    protected Short doTransform(Number argument) {
        return argument.shortValue();
    }

}
