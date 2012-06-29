package cucumber.runtime.converters;

import cucumber.runtime.CucumberException;
import cucumber.runtime.xstream.converters.SingleValueConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ClassWithStringConstructorConverter implements SingleValueConverter {
    private Constructor ctor;

    @Override
    public String toString(Object obj) {
        return obj.toString();
    }

    @Override
    public Object fromString(String str) {
        try {
            return ctor.newInstance(str);
        } catch (InstantiationException e) {
            throw new CucumberException(e);
        } catch (IllegalAccessException e) {
            throw new CucumberException(e);
        } catch (InvocationTargetException e) {
            throw new CucumberException(e.getTargetException());
        }
    }

    @Override
    public boolean canConvert(Class type) {
        try {
            ctor = type.getConstructor(String.class);
            return true;
        } catch (NoSuchMethodException e) {
            ctor = null;
            return false;
        }
    }
}
