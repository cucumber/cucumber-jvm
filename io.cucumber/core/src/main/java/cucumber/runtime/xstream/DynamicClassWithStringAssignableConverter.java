package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverterWrapper;

import java.lang.reflect.Constructor;

class DynamicClassWithStringAssignableConverter extends DynamicClassBasedSingleValueConverter {

    @Override
    public SingleValueConverterWrapper converterForClass(Class type) {
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
