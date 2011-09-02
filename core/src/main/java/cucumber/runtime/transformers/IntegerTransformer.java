package cucumber.runtime.transformers;

import java.util.Locale;

public class IntegerTransformer extends TransformerWithNumberFormat<Integer> {

    public IntegerTransformer(Locale locale) {
        super(locale, new Class[]{Integer.class, Integer.TYPE});
    }

    @Override
    protected Integer doTransform(Number argument) {
        return argument.intValue();
    }

}
