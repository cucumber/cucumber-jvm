package cucumber.runtime.transformers;

import java.util.Locale;

public class ByteTransformer extends TransformerWithNumberFormat<Byte> {

    public ByteTransformer(Locale locale) {
        super(locale, new Class[]{Byte.class, Byte.TYPE});
    }

    @Override
    protected Byte doTransform(Number argument) {
        return argument.byteValue();
    }

}
