package cucumber.runtime.transformers;

import java.util.Locale;

public class LongTransformer extends TransformerWithNumberFormat<Long> {

    public LongTransformer(Locale locale) {
        super(locale, new Class[]{Long.class, Long.TYPE});
    }

    @Override
    protected Long doTransform(Number argument) {
        return argument.longValue();
    }

}
