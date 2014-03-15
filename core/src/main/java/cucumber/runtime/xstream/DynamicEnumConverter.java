package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.converters.Converter;
import cucumber.deps.com.thoughtworks.xstream.converters.MarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.converters.UnmarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.Locale;

/**
 * Creates an instance of needed {@link cucumber.runtime.xstream.ConverterWithEnumFormat} dynamically based on required type
 */
public class DynamicEnumConverter implements Converter {

    private final Locale locale;

    public DynamicEnumConverter(Locale locale) {
        this.locale = locale;
    }

    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {
        wrappedConvertor(o.getClass()).marshal(o, writer, context);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Class enumClass = context.getRequiredType();
        return wrappedConvertor(enumClass).unmarshal(reader, context);
    }

    private Converter wrappedConvertor(Class enumClass) {
        return new SingleValueConverterWrapperExt(new ConverterWithEnumFormat(locale, enumClass));
    }

    @Override
    public boolean canConvert(Class type) {
        return type.isEnum();
    }
}
