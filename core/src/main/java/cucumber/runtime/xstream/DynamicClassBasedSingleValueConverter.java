package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.converters.Converter;
import cucumber.deps.com.thoughtworks.xstream.converters.MarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverterWrapper;
import cucumber.deps.com.thoughtworks.xstream.converters.UnmarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamWriter;

abstract class DynamicClassBasedSingleValueConverter implements Converter {
    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {
        converterForClass(o.getClass()).marshal(o, writer, context);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final Class targetClass = context.getRequiredType();
        return converterForClass(targetClass).unmarshal(reader, context);
    }

    public abstract SingleValueConverterWrapper converterForClass(Class type);
}
