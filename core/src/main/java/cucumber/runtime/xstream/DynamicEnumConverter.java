package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverterWrapper;

import java.util.Locale;

/**
 * Creates an instance of needed {@link cucumber.runtime.xstream.ConverterWithEnumFormat} dynamically based on required type
 */
class DynamicEnumConverter extends DynamicClassBasedSingleValueConverter {

    private final Locale locale;

    DynamicEnumConverter(Locale locale) {
        this.locale = locale;
    }

    @Override
    public SingleValueConverterWrapper converterForClass(Class type) {
        return new SingleValueConverterWrapperExt(new ConverterWithEnumFormat(locale, type));
    }

    @Override
    public boolean canConvert(Class type) {
        return type.isEnum();
    }
}
