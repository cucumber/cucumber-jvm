package cucumber.runtime.transformers;

import java.util.Locale;

public class FloatTransformer extends TransformerWithNumberFormat<Float> {

    public FloatTransformer(Locale locale) {
        super(locale, new Class[]{Float.class, Float.TYPE});
    }

    @Override
    protected Float doTransform(Number argument) {
        return argument.floatValue();
    }

}
