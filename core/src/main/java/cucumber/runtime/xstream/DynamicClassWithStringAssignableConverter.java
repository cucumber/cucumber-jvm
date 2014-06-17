package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.converters.Converter;
import cucumber.deps.com.thoughtworks.xstream.converters.MarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.converters.UnmarshallingContext;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.lang.reflect.Constructor;

public class DynamicClassWithStringAssignableConverter implements Converter {
    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {
        wrappedConvertor(o.getClass()).marshal(o, writer, context);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final Class targetClass = context.getRequiredType();
        return wrappedConvertor(targetClass).unmarshal(reader, context);
    }

    private Converter wrappedConvertor(Class type) {
        final Constructor assignableConstructor = findAssignableConstructor(type);
        return new SingleValueConverterWrapperExt(new ClassWithStringAssignableConstructorConverter(assignableConstructor));
    }


    @Override
    public boolean canConvert(Class type) {
        return null != findAssignableConstructor(type);
    }

    private static Constructor findAssignableConstructor(Class type) {
        for (Constructor constructor : type.getConstructors()) {
            if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0].isAssignableFrom(String.class)) {
                return constructor;
            }
        }
        return null;
    }
}
