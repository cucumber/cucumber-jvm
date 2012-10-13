package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.runtime.CucumberException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

class ClassWithStringAssignableConstructorConverter implements SingleValueConverter {
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
        for (Constructor constructor : type.getConstructors()) {
            if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0].isAssignableFrom(String.class)) {
                this.ctor = constructor;
                return true;
            }
        }
        return false;
    }
}
